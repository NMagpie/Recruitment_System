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
import threading

import schedule
from django.contrib import admin
from django.urls import path, include

from .routes import upload_cv, cv_details, cv_download, delete_cv, health
from .saga_routes_file_wrapper import get_file_saga_urls
from .serviceJWTAuthentication import initialize_token, schedule_loop, refresh_token
import py_eureka_client.eureka_client as eureka_client
from CV_Processing.settings import SERVICE_NAME, APP_PORT_VAR, EUREKA_URL_DEFAULT_ZONE

urlpatterns = [
    path('admin/', admin.site.urls),
    path('cv/', upload_cv, name='upload_cv'),
    path('cv/delete', delete_cv, name='delete_cv'),
    path('cv/info/<str:id>', cv_details, name='cv_details'),
    path('cv/download/<str:filename>', cv_download, name='cv_download'),
    path('health', health, name='health'),

    path('', include(get_file_saga_urls()))
]

# initialize_token()
#
# schedule_thread = threading.Thread(target=schedule_loop)
# schedule_thread.start()
#
schedule.every(14).minutes.do(refresh_token)

eureka_client.init(eureka_server=EUREKA_URL_DEFAULT_ZONE,
                   app_name=SERVICE_NAME,
                   instance_port=int(APP_PORT_VAR),
                   instance_ip=socket.gethostbyname(socket.gethostname()),
                   instance_host=socket.gethostbyname(socket.gethostname()))