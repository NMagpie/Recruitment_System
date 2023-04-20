from djongo import models
from djongo.models import ObjectIdField


class TransactionLog(models.Model):
    ACTION_CHOICES = (
        ('CREATE', 'create'),
        ('UPDATE', 'update'),
        ('DELETE', 'delete'),
    )

    _id = ObjectIdField(primary_key=True, max_length=64)
    document_type = models.CharField(max_length=64)
    document_id = models.CharField(max_length=64)
    # The type of action performed by the request (create, update, delete)
    action = models.CharField(max_length=16, choices=ACTION_CHOICES)
    # The data affected by the request
    previous_state = models.JSONField()

    class Meta:
        db_table = 'transactions'
