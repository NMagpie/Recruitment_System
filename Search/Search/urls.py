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
from django.contrib import admin
from django.urls import path

from Search.cv_routes import save_cv_metadata, search_cv_metadata, delete_cv_metadata
from Search.job_routes import save_job, search_job, delete_job

urlpatterns = [
    path('admin/', admin.site.urls),

    path('upload/cv/', save_cv_metadata, name='save_cv_metadata'),
    path('search/cv/', search_cv_metadata, name='search_cv_metadata'),
    path('rollback/cv/', delete_cv_metadata, name='delete_cv_metadata'),

    path('upload/job/', save_job, name='save_job'),
    path('search/job/', search_job, name='search_job'),
    path('rollback/job/', delete_job, name='delete_job'),
]
