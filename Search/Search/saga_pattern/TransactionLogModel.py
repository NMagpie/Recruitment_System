from djongo import models
from djongo.models import ObjectIdField

from Search.models import Job, CVMetadata

# types of choices for Model
TYPE_CHOICES = (
    ('JOB', 'job'),
    ('CV_METADATA', 'cv_metadata')
)

# types of Models by type
type_model = {
    'job': Job,
    'cv_metadata': CVMetadata
}


class TransactionLog(models.Model):
    ACTION_CHOICES = (
        ('CREATE', 'create'),
        ('UPDATE', 'update'),
        ('DELETE', 'delete'),
    )

    _id = ObjectIdField(primary_key=True, max_length=64)
    document_type = models.CharField(max_length=32, choices=TYPE_CHOICES)
    document_id = models.CharField(max_length=64)
    # The type of action performed by the request (create, update, delete)
    action = models.CharField(max_length=16, choices=ACTION_CHOICES)
    # The data affected by the request
    previous_state = models.JSONField()

    class Meta:
        db_table = 'transactions'
