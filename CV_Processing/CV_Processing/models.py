from django.db import models
from djongo.models import ObjectIdField


class FileMetadata(models.Model):
    _id = ObjectIdField(primary_key=True, max_length=64)
    filename = models.CharField(max_length=255)
    filetype = models.CharField(max_length=100)
    candidate_name = models.CharField(max_length=255)
    user_id = models.CharField(max_length=64)

    class Meta:
        db_table = 'uploaded_cv_files_info'
