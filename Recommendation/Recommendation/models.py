from djongo import models
from djongo.models import ObjectIdField

from Recommendation.array_field import FieldsArrayField


class UserData(models.Model):
    _id = ObjectIdField(primary_key=True, max_length=64)
    type = models.CharField(max_length=255)
    location = models.CharField(max_length=255)

    tags = FieldsArrayField(models.CharField(max_length=255), blank=True)

    searches = FieldsArrayField(models.CharField(max_length=255), blank=True)

    class Meta:
        db_table = 'users_data'


class CVMetadata(models.Model):
    _id = ObjectIdField(primary_key=True, max_length=64)
    filename = models.CharField(max_length=255)
    filetype = models.CharField(max_length=100)
    candidate_name = models.CharField(max_length=255)
    user_id = models.CharField(max_length=64)
    tags = FieldsArrayField(models.CharField(max_length=255), blank=True)

    class Meta:
        db_table = 'cvs'


class Job(models.Model):
    _id = ObjectIdField(primary_key=True, max_length=64)
    user_id = models.CharField(max_length=64)
    company_name= models.CharField(max_length=255)
    title = models.CharField(max_length=255)
    description = models.TextField()
    location = models.CharField(max_length=255)
    tags = FieldsArrayField(models.CharField(max_length=255), blank=True)

    class Meta:
        db_table = 'jobs'
