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

from django.contrib import admin
from django.urls import path, include

from .routes import upload_cv, cv_details, cv_download, delete_cv
from .saga_routes_file_wrapper import get_file_saga_urls

urlpatterns = [
    path('admin/', admin.site.urls),
    path('cv/', upload_cv, name='upload_cv'),
    path('cv/delete', delete_cv, name='delete_cv'),
    path('cv/info/<str:id>', cv_details, name='cv_details'),
    path('cv/download/<str:filename>', cv_download, name='cv_download'),

    path('', include(get_file_saga_urls()))
]