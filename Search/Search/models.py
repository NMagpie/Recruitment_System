import json

from django.db import models
from djongo.models import ObjectIdField

class CVMetadata(models.Model):
    _id = ObjectIdField(primary_key=True, max_length=64)
    filename = models.CharField(max_length=255)
    filetype = models.CharField(max_length=100)
    candidate_name = models.CharField(max_length=255)
    user_id = models.CharField(max_length=64)
    tags = models.JSONField()

    def __unicode__(self):
        return self._id

    def id_to_str(self):
        return str(self._id)

    def tags_to_list(self):
        return list(json.loads(self.tags))

    class Meta:
        db_table = 'cvs'
