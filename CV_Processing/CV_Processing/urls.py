"""CV_Processing URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/4.1/topics/http/urls/
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

from .routes import upload_cv, cv_details, cv_download, delete_cv, health, get_user_cvs
from .saga_pattern.saga_routes import get_saga_urls
from .serviceJWTAuthentication import refresh_token, schedule_loop, initialize_token
import py_eureka_client.eureka_client as eureka_client
from CV_Processing.settings import SERVICE_NAME, APP_PORT_VAR, EUREKA_URL_DEFAULT_ZONE

urlpatterns = [
    path('admin/', admin.site.urls),
    path('cv/', upload_cv, name='upload_cv'),
    path('cv/delete/<str:id>', delete_cv, name='delete_cv'),
    path('cv/info/<str:id>', cv_details, name='cv_details'),
    path('cv/download/<str:id>', cv_download, name='cv_download'),

    path('user/cv/<str:userId>', get_user_cvs, name='get_user_cvs'),

    path('health', health, name='health'),

    path('', include(get_saga_urls()))
]

initialize_token()
