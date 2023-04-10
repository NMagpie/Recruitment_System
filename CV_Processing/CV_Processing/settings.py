"""
Django settings for CV_Processing project.

Generated by 'django-admin startproject' using Django 4.1.7.

For more information on this file, see
https://docs.djangoproject.com/en/4.1/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/4.1/ref/settings/
"""
import json
import logging
import os
from pathlib import Path

from django.core.management import execute_from_command_line
from dotenv import load_dotenv

from django.core.management.commands.runserver import Command as runserver
from kazoo.client import KazooClient

from kafka import KafkaConsumer

from django.conf import settings

import ssl

# Build paths inside the project like this: BASE_DIR / 'subdir'.
BASE_DIR = Path(__file__).resolve().parent.parent

# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/4.1/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = 'django-insecure-(no=f04hpv1*t1*l+u*q(h%jg$8)k0h8j3lvlw4mc6v6-dj41p'

# SECURITY WARNING: don't run with debug turned on in production!
load_dotenv()

DEBUG = True

ALLOWED_HOSTS = []

SSL_PASSWORD = os.environ.get('SSL_PASSWORD')
#
# ZOOKEEPER = os.environ.get('ZOOKEEPER')

# Database
# https://docs.djangoproject.com/en/4.1/ref/settings/#databases

# DATABASES = {
#     'default': {
#         'ENGINE': 'django.db.backends.mysql',
#         'NAME': os.environ.get('DB_NAME'),
#         'USER': os.environ.get('DB_USER'),
#         'PASSWORD': os.environ.get('DB_PASSWORD'),
#         'HOST': os.environ.get('DB_HOST'),
#         'PORT': os.environ.get('DB_PORT'),
#         'OPTIONS': {'init_command': "SET sql_mode='STRICT_TRANS_TABLES'"},
#     }
# }

caRootLocation = './secrets/CARoot.pem'
certLocation = './secrets/certificate.pem'
keyLocation = './secrets/key.pem'
certKey = './secrets/cert_key.pem'

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

# logging.basicConfig()
# logger = logging.getLogger()
# logger.setLevel(logging.DEBUG)

# # Create a connection to ZooKeeper
# zk = KazooClient(hosts=ZOOKEEPER,
#                  use_ssl=True,
#                  keyfile=keyLocation,
#                  verify_certs=False,
#                  certfile=certLocation,
#                  ca=caRootLocation,
#                  keyfile_password=SSL_PASSWORD,
#                  )
# zk.start()
#
# # Create the root node for the CV Processing Service
# zk.ensure_path('/cv-processing')
#
# # Create an ephemeral node for the CV Processing Service
# service_node = zk.create('/cv-processing/cv-processing-service-', ephemeral=True, sequence=True)
#
# # Set the data for the service node to the host and port of the CV Processing Service
# service_data = {
#     'host': 'localhost',
#     'port': runserver.default_port
# }
#
# zk.set(service_node, bytes(json.dumps(service_data), 'utf-8'))

# Application definition

INSTALLED_APPS = [
    'sslserver',
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'CV_Processing'
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

ROOT_URLCONF = 'CV_Processing.urls'

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

WSGI_APPLICATION = 'CV_Processing.wsgi.application'

# Password validation
# https://docs.djangoproject.com/en/4.1/ref/settings/#auth-password-validators

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
# https://docs.djangoproject.com/en/4.1/topics/i18n/

LANGUAGE_CODE = 'en-us'

TIME_ZONE = 'UTC'

USE_I18N = True

USE_TZ = True

# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/4.1/howto/static-files/

STATIC_URL = 'static/'

# Default primary key field type
# https://docs.djangoproject.com/en/4.1/ref/settings/#default-auto-field

DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'
