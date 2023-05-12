"""Search URL Configuration

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

from Search.common_routes import search_data, health
from Search.saga_pattern.saga_routes import get_saga_urls
from Search.cv_routes import CV_View
from Search.job_routes import Job_View
from Search.serviceJWTAuthentication import initialize_token

urlpatterns = [
    path('admin/', admin.site.urls),

    path('cv/', CV_View.as_view(), name='cv_view'),
    path('job/', Job_View.as_view(), name='job_view'),

    path('search/job/', search_data, name='search_job', kwargs={'search_type': 'job'}),
    path('search/cv/', search_data, name='search_cv_metadata', kwargs={'search_type': 'cv'}),

    path('health', health, name='health'),

    path('', include(get_saga_urls()))
]

initialize_token()
