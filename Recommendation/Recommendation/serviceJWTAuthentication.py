import time

import requests
import schedule
from requests.adapters import HTTPAdapter
from rest_framework_simplejwt.authentication import JWTAuthentication
from rest_framework.authentication import get_authorization_header
from urllib3 import PoolManager

from Recommendation.settings import SHARED_SECRET_KEY, USER_AUTH_SECRET_KEY, SERVICE_AUTH_SECRET_KEY, SERVICE_NAME, \
    REGISTER_URL, REFRESH_URL, APP_PORT_VAR


class AuthorizationJWTAuthentication(JWTAuthentication):
    JWT_SECRET_KEY = USER_AUTH_SECRET_KEY

    def get_header(self, request):
        return get_authorization_header(request).split()[1]


class ServiceAuthJWTAuthentication(JWTAuthentication):
    JWT_SECRET_KEY = SERVICE_AUTH_SECRET_KEY

    def get_header(self, request):
        return request.META.get('Service-Auth').split()[1]


register_url = REGISTER_URL

refresh_url = REFRESH_URL

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

    global refresh_url

    global data

    global token

    s = requests.Session()
    s.mount('https://', HostNameIgnoringAdapter())

    headers = {'Service-Auth': token}

    response = s.post(refresh_url, headers=headers, json=data, verify='./secrets/ca-cert').json()

    s.close()

    token = response['token']


def schedule_loop():
    while True:
        schedule.run_pending()
        time.sleep(1)
