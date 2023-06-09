"""
URL configuration for Job_Posting project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/4.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
import socket

import schedule
from django.contrib import admin
from django.urls import path, include
from .routes import upload_job, rud_job, health, get_user_jobs
from .saga_pattern.saga_routes import get_saga_urls
from .serviceJWTAuthentication import refresh_token, schedule_loop, initialize_token
import py_eureka_client.eureka_client as eureka_client
from Job_Posting.settings import SERVICE_NAME, APP_PORT_VAR, EUREKA_URL_DEFAULT_ZONE

urlpatterns = [
    path('admin/', admin.site.urls),
    path('jobs/', upload_job, name='upload_job'),
    path('jobs/<str:id>', rud_job, name='rud_job'),

    path('user/job/<str:userId>', get_user_jobs, name='get_user_jobs'),

    path('health', health, name='health'),

    path('', include(get_saga_urls()))
]

initialize_token()
