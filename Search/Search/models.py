from djongo import models
from djongo.models import ObjectIdField

from Search.array_field import FieldsArrayField


class CVMetadata(models.Model):
    _id = ObjectIdField(primary_key=True, max_length=64)
    filename = models.CharField(max_length=255)
    filetype = models.CharField(max_length=100)
    candidate_name = models.CharField(max_length=255)
    user_id = models.CharField(max_length=64)
    tags = FieldsArrayField(models.CharField(max_length=255), blank=True)

    def __unicode__(self):
        return self._id

    def id_to_str(self):
        return str(self._id)

    class Meta:
        db_table = 'cvs'


class Job(models.Model):
    _id = ObjectIdField(primary_key=True, max_length=64)
    user_id = models.CharField(max_length=64)
    company_name = models.CharField(max_length=255)
    title = models.CharField(max_length=255)
    description = models.TextField()
    location = models.CharField(max_length=255)
    tags = FieldsArrayField(models.CharField(max_length=255), blank=True)

    def id_to_str(self):
        return str(self._id)

    class Meta:
        db_table = 'jobs'
