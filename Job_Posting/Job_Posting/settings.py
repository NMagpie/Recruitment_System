"""
Django settings for Job_Posting project.

Generated by 'django-admin startproject' using Django 4.2.

For more information on this file, see
https://docs.djangoproject.com/en/4.2/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/4.2/ref/settings/
"""
import os
import ssl
from pathlib import Path


from dotenv import load_dotenv

# Build paths inside the project like this: BASE_DIR / 'subdir'.
BASE_DIR = Path(__file__).resolve().parent.parent


# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/4.2/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = 'django-insecure-u!)0pk_(dwu3vf6hui8s6vqz9i0m*8t)7-$j)q511lah$(2x-s'

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = True

ALLOWED_HOSTS = ['*']

load_dotenv()

# SAGA CONFIGURATION

app_name = "Job_Posting"

# SECURITY CONFIGURATION

SSL_PASSWORD = os.environ.get('SSL_PASSWORD')

caRootLocation = './secrets/CARoot.pem'
certLocation = './secrets/certificate.pem'
keyLocation = './secrets/key.pem'
certKey = './secrets/cert_key.pem'

# Application definition

INSTALLED_APPS = [
    'sslserver',
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'Job_Posting',
]

MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]

ROOT_URLCONF = 'Job_Posting.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [BASE_DIR / 'templates']
        ,
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'Job_Posting.wsgi.application'

# Authorization

SERVICE_NAME = 'job-posting'

REGISTER_URL = os.environ.get("REGISTER_URL")

REFRESH_URL = os.environ.get("REFRESH_URL")

APP_PORT_VAR = os.environ.get("APP_PORT_VAR")

APP_HOST_VAR = os.environ.get("APP_HOST_VAR")

EUREKA_URL_DEFAULT_ZONE = os.environ.get("EUREKA_URL_DEFAULT_ZONE")

EUREKA_URL = os.environ.get("EUREKA_URL")

# REST_FRAMEWORK = {
#     'DEFAULT_AUTHENTICATION_CLASSES': [
#         #'Recommendation.serviceJWTAuthentication.AuthorizationJWTAuthentication',
#         'Recommendation.serviceJWTAuthentication.ServiceAuthJWTAuthentication',
#     ],
#     'DEFAULT_PERMISSION_CLASSES': [
#         'rest_framework.permissions.IsAuthenticated',
#     ],
# }

# Initial Service Authentication
SHARED_SECRET_KEY = os.environ.get('SHARED_SECRET_KEY')

# Validation
SERVICE_AUTH_SECRET_KEY = os.environ.get('SERVICE_AUTH_SECRET_KEY')

USER_AUTH_SECRET_KEY = os.environ.get('USER_AUTH_SECRET_KEY')


# Database
# https://docs.djangoproject.com/en/4.2/ref/settings/#databases

DATABASES = {
    'default': {
        'ENGINE': 'djongo',
        'NAME': os.environ.get('DB_NAME'),
        'CLIENT': {
            'host': os.environ.get('DB_HOST'),
            'port': int(os.environ.get('DB_PORT')),
            'authSource': 'admin',
            'SSL': True,
            'tlscertificatekeyfile': certKey,
            'tlscertificatekeyfilepassword': SSL_PASSWORD,
            'tlsallowinvalidhostnames': True,
            'tlsallowinvalidcertificates': True,
        }
    }
}


# Password validation
# https://docs.djangoproject.com/en/4.2/ref/settings/#auth-password-validators

AUTH_PASSWORD_VALIDATORS = [
    {
        'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',
    },
]


# Internationalization
# https://docs.djangoproject.com/en/4.2/topics/i18n/

LANGUAGE_CODE = 'en-us'

TIME_ZONE = 'UTC'

USE_I18N = True

USE_TZ = True


# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/4.2/howto/static-files/

STATIC_URL = 'static/'

# Default primary key field type
# https://docs.djangoproject.com/en/4.2/ref/settings/#default-auto-field

DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'
