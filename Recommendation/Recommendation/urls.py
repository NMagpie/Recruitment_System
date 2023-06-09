"""
URL configuration for Recommendation project.

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

import schedule
from django.contrib import admin
from django.urls import path, include
import socket

from Recommendation.cv_routes import CV_View
from Recommendation.job_routes import Job_View
from Recommendation.routes import upload_user, upload_tags, upload_searches, get_recommendations, health
from Recommendation.saga_pattern.saga_routes import get_saga_urls
from Recommendation.serviceJWTAuthentication import refresh_token, schedule_loop, initialize_token
import py_eureka_client.eureka_client as eureka_client
from Recommendation.settings import SERVICE_NAME, APP_PORT_VAR, EUREKA_URL_DEFAULT_ZONE

urlpatterns = [
    path('admin/', admin.site.urls),

    path('upload/', upload_user, name='upload_user'),
    path('tags/', upload_tags, name='upload_tags'),
    path('searches/', upload_searches, name='upload_searches'),
    path('recommendation/', get_recommendations, name='get_recommendations'),
    path('health', health, name='health'),

    path('cv/', CV_View.as_view(), name='cv_view'),
    path('job/', Job_View.as_view(), name='job_view'),

    path('', include(get_saga_urls())),
]

initialize_token()
