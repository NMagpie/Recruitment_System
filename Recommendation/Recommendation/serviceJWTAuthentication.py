import json
import time
from base64 import b64decode

import requests
import schedule
import jwt
from jwt import DecodeError
from requests.adapters import HTTPAdapter
from rest_framework_simplejwt import exceptions
from rest_framework_simplejwt.authentication import JWTAuthentication
from rest_framework.authentication import get_authorization_header
from urllib3 import PoolManager

from Recommendation.settings import SHARED_SECRET_KEY, USER_AUTH_SECRET_KEY, SERVICE_AUTH_SECRET_KEY, SERVICE_NAME, \
    REGISTER_URL, REFRESH_URL, APP_PORT_VAR, EUREKA_URL


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
            if request.META.get('REQUEST_METHOD') == 'POST' and request.META.get('CONTENT_TYPE').startswith('application/json'):
                body = json.loads(request.body)
            else:
                body = dict()

            body['real_id'] = decoded_token['sub']

            request.body = json.dumps(body)
        except DecodeError:
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
            # raise exceptions.AuthenticationFailed('Invalid Service-Auth header')
            return None

        # Validate token using the secret key
        try:
            decoded_token = jwt.decode(token, algorithms=['HS256'], verify=True, key=b64decode(SERVICE_AUTH_SECRET_KEY))
        except DecodeError:
            # raise DecodeError('Invalid token')
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
    'serviceUUID': None
}

token = None


class HostNameIgnoringAdapter(HTTPAdapter):
    def init_poolmanager(self, connections, maxsize, block=False, **pool_kwargs):
        self.poolmanager = PoolManager(num_pools=connections,
                                       maxsize=maxsize,
                                       block=block,
                                       assert_hostname=False, **pool_kwargs)


def initialize_token():
    global register_url

    global data

    global token

    s = requests.Session()
    s.mount('https://', HostNameIgnoringAdapter())

    initial_data = {
        'serviceName': data['serviceName'],
        'sharedSecretKey': SHARED_SECRET_KEY,
        'port': APP_PORT_VAR
    }

    response = s.post(register_url, json=initial_data, verify='./secrets/ca-cert').json()

    s.close()

    data['serviceUUID'] = response['serviceUUID']

    token = response['token']


def refresh_token():
    print("refreshing token...")

    global eureka_url

    global data

    global token

    s = requests.Session()
    s.mount('https://', HostNameIgnoringAdapter())

    headers = {'Service-Auth': token}

    response = s.post(eureka_url + '/refresh_token', headers=headers, json=data, verify='./secrets/ca-cert').json()

    s.close()

    token = response['token']


def schedule_loop():
    while True:
        schedule.run_pending()
        time.sleep(1)
