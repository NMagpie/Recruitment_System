import time

import requests
import schedule
from rest_framework_simplejwt.authentication import JWTAuthentication
from rest_framework.authentication import get_authorization_header

from Recommendation.settings import SHARED_SECRET_KEY, USER_AUTH_SECRET_KEY, SERVICE_AUTH_SECRET_KEY, SERVICE_NAME


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


def initialize_token():
    global register_url

    global data

    global token

    initial_data = {
        'serviceName': data['serviceName'],
        'sharedSecretKey': SHARED_SECRET_KEY
    }

    response = requests.post(register_url, json=initial_data, verify='./secrets/ca-cert').json()

    data['serviceUUID'] = response['serviceUUID']

    token = response['token']

    print(data['serviceUUID'])

    print(token)


def refresh_token():
    print("refreshing token...")

    global refresh_url

    global data

    global token

    headers = {'Service-Auth': token}

    response = requests.post(refresh_url, headers=headers, json=data, verify='./secrets/ca-cert').json()

    token = response['token']


def schedule_loop():
    while True:
        schedule.run_pending()
        time.sleep(1)
