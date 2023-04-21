from django.db import transaction
from bson import ObjectId
from django.forms import model_to_dict

from CV_Processing.saga_pattern.TransactionLogModel import TransactionLog
from django.apps import apps

from CV_Processing.settings import app_name


def is_document_locked(document_id, document_type):
    try:
        if TransactionLog.objects.get(document_id=document_id, document_type=document_type.__name__):
            return True
    except TransactionLog.DoesNotExist:
        return False


@transaction.atomic()
def prepare_document(document, action=None):
    document_model = type(document)

    if not action:
        try:
            if document_model.objects.get(_id=document._id):
                action = 'update'
        except document_model.DoesNotExist:
            action = 'create'

    previous_state = {}
    if action != 'create':
        document_model = type(document)
        try:
            previous_document = document_model.objects.get(_id=document._id)
            previous_state = model_to_dict(previous_document)
            previous_state.pop('_id')
        except document_model.DoesNotExist:
            pass

    document_id = document._id

    if action == 'delete':
        document.delete()
    else:
        document_dict = model_to_dict(document)
        document_dict['_id'] = str(document_dict['_id'])
        document.save()

    transaction = TransactionLog(
        document_type=document_model.__name__,
        document_id=document_id,
        action=action,
        previous_state=previous_state
    )

    transaction.save()

    return str(transaction._id)


def saga_success(transaction_id):
    TransactionLog.objects.get(_id=ObjectId(transaction_id)).delete()


def saga_fail(transaction_id):
    transaction = TransactionLog.objects.get(_id=ObjectId(transaction_id))

    document_model = apps.get_model(app_name, transaction.document_type)

    if transaction.action == 'create':
        document_model \
            .objects \
            .filter(_id=ObjectId(transaction.document_id)) \
            .delete()

    elif transaction.action == 'update':

        previous_state_dict = dict(transaction.previous_state)

        document_model \
            .objects \
            .filter(_id=ObjectId(transaction.document_id)) \
            .update(**previous_state_dict)

    elif transaction.action == 'delete':

        previous_state_dict = dict(transaction.previous_state)

        document = document_model(**previous_state_dict)

        document._id = ObjectId(transaction.document_id)

        document.save()

    transaction.delete()
