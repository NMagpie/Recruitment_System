from django.db import models
from djongo.models import ObjectIdField
from Job_Posting.array_field import FieldsArrayField


class Job(models.Model):
    _id = ObjectIdField(primary_key=True, max_length=64)
    user_id = models.CharField(max_length=64)
    title = models.CharField(max_length=255)
    description = models.TextField()
    location = models.CharField(max_length=255)
    tags = FieldsArrayField(models.CharField(max_length=255), blank=True)

    class Meta:
        db_table = 'uploaded_jobs_info'
