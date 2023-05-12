import json
import sys
import time
from base64 import b64decode

import jwt
import requests
import schedule as schedule
from jwt import DecodeError
import socket
import py_eureka_client.eureka_client as eureka_client
from requests.adapters import HTTPAdapter
from rest_framework_simplejwt.authentication import JWTAuthentication
from rest_framework.authentication import get_authorization_header
from urllib3 import PoolManager

from Search.settings import SHARED_SECRET_KEY, USER_AUTH_SECRET_KEY, SERVICE_AUTH_SECRET_KEY, SERVICE_NAME, REFRESH_URL, \
    REGISTER_URL, APP_PORT_VAR, EUREKA_URL, EUREKA_URL_DEFAULT_ZONE


class AuthorizationJWTAuthentication(JWTAuthentication):
    def authenticate(self, request):
        # Check if Authorization header is present
        auth_header = request.headers.get('Authorization', None)
        if not auth_header:
            return None

        # Extract token from Authorization header
        try:
            token = auth_header.split(' ')[1]
        except IndexError:
            return None

        # Validate token using the secret key
        try:
            decoded_token = jwt.decode(token, algorithms=['HS512'], verify=True, key=b64decode(USER_AUTH_SECRET_KEY))
            if request.META.get('REQUEST_METHOD') == 'POST' and request.META.get('CONTENT_TYPE').startswith(
                    'application/json'):
                body = json.loads(request.body)
            else:
                body = dict()

            body['real_id'] = decoded_token['sub']

            request.body = json.dumps(body)
        except Exception:
            return None

        user = type('test', (), {})()

        user.is_authenticated = True

        return user, decoded_token

    def get_header(self, request):
        print(get_authorization_header(request))
        return get_authorization_header(request).split()[1]


class ServiceAuthJWTAuthentication(JWTAuthentication):
    def authenticate(self, request):
        # Check if Service-Auth header is present
        auth_header = request.headers.get('Service-Auth', None)
        if not auth_header:
            return None

        # Extract token from Service-Auth header
        try:
            token = auth_header.split(' ')[1]
        except IndexError:
            return None

        # Validate token using the secret key
        try:
            decoded_token = jwt.decode(token, algorithms=['HS256'], verify=True, key=b64decode(SERVICE_AUTH_SECRET_KEY))
        except Exception:
            return None

        user = type('test', (), {})()

        user.is_authenticated = True

        return user, decoded_token

    def get_header(self, request):
        return request.META.get('HTTP_SERVICE_AUTH').split()[1]


register_url = REGISTER_URL

refresh_url = REFRESH_URL

eureka_url = EUREKA_URL

data = {
    'serviceName': SERVICE_NAME,
    'secretKey': SHARED_SECRET_KEY,
}

token = None


class HostNameIgnoringAdapter(HTTPAdapter):
    def init_poolmanager(self, connections, maxsize, block=False, **pool_kwargs):
        self.poolmanager = PoolManager(num_pools=connections,
                                       maxsize=maxsize,
                                       block=block,
                                       assert_hostname=False, **pool_kwargs)


retry_count = 0
max_retries = 5


def refresh_token():
    try:
        print('Refreshing token...')
        global eureka_url, retry_count

        global data

        global token

        s = requests.Session()
        s.mount('https://', HostNameIgnoringAdapter())

        response = s.post(eureka_url + '/refresh_token', json=data, verify='./secrets/ca-cert')

        token = response

        s.close()

    except Exception:
        if retry_count == -1:
            print('Failed to retrieve token after 14 minutes. Retry after another 14 minutes.')
        return


def initialize_token():
    global retry_count, token, max_retries
    retry_count += 1
    refresh_token()
    if token is None:
        if retry_count >= max_retries:
            print('Maximum number of token retrieval tries has exceeded')
            sys.exit(1)
        else:
            delay = 2 ** retry_count
            print(f'Retrying token retrieval in {delay} seconds. Attempt {retry_count}')
            time.sleep(delay)
            initialize_token()
    else:
        print('Token retrieval successful')

        retry_count = -1

        schedule.every(14).minutes.do(refresh_token)

        eureka_client.init(eureka_server=EUREKA_URL_DEFAULT_ZONE,
                           app_name=SERVICE_NAME,
                           instance_port=int(APP_PORT_VAR),
                           instance_ip=socket.gethostbyname(socket.gethostname()),
                           instance_host=socket.gethostbyname(socket.gethostname()),
                           instance_secure_port_enabled=True,
                           instance_secure_port=int(APP_PORT_VAR)
                           )


def schedule_loop():
    while True:
        schedule.run_pending()
        time.sleep(1)
