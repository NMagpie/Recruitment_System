from django.db import models
from djongo.models import ObjectIdField


class Job(models.Model):
    _id = ObjectIdField(primary_key=True, max_length=64)
    user_id = models.CharField(max_length=64)
    title = models.CharField(max_length=255)
    description = models.TextField()
    location = models.CharField(max_length=255)

    class Meta:
        db_table = 'uploaded_jobs_info'
