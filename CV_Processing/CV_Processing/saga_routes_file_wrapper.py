import os

from bson import ObjectId
from django.urls import path

import json

from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.decorators import api_view, authentication_classes, permission_classes
from rest_framework.permissions import IsAuthenticated

from CV_Processing.models import FileMetadata
from CV_Processing.saga_pattern.TransactionLogModel import TransactionLog
from CV_Processing.saga_pattern.saga_pattern_util import saga_fail, saga_success
from CV_Processing.serviceJWTAuthentication import AuthorizationJWTAuthentication, ServiceAuthJWTAuthentication


def get_file_saga_urls():
    return [
        path('cv/rollback/', file_rollback_saga, name='file_rollback_saga'),
        path('cv/success/', file_success_saga, name='file_success_saga'),
    ]


@api_view(['POST'])
@authentication_classes([
    AuthorizationJWTAuthentication,
    ServiceAuthJWTAuthentication])
@permission_classes([IsAuthenticated])
@csrf_exempt
def file_rollback_saga(request):
    if request.method == 'POST':
        try:
            # Get the file hash and filename from the request
            data = json.loads(request.body)

            transaction_id = data.get('id')

            transaction = TransactionLog.objects.get(_id=ObjectId(transaction_id))

            previous_state = dict(transaction.previous_state)

            action = transaction.action

            if action == 'create':
                state = FileMetadata.objects.get(_id=ObjectId(transaction.document_id))

                filename = f'{state._id}_{state.filename}'
            else:
                filename = f'{transaction.document_id}_{previous_state["filename"]}'

            if action == 'delete':
                os.rename('./temp/' + filename, './uploaded_CVs/' + filename)
            else:
                os.remove('./temp/' + filename)

            # Delete the metadata from the database
            saga_fail(transaction_id)

            # Return a success response
            return JsonResponse({'status': 'success'}, status=200)

        except ValueError:
            # Handle errors caused by invalid JSON data in the request body
            return JsonResponse({'error': 'Invalid JSON data'}, status=400)
        except TransactionLog.DoesNotExist:
            # Handle errors caused by trying to delete a non-existent object
            return JsonResponse({'error': 'Transaction data not found'}, status=404)
        except Exception as e:
            # Handle all other exceptions
            print(e)
            return JsonResponse({'error': 'An error occurred'}, status=500)
    else:
        return JsonResponse({'status': 'error', 'message': 'invalid request method'}, status=400)


@api_view(['POST'])
@authentication_classes([
    AuthorizationJWTAuthentication,
    ServiceAuthJWTAuthentication])
@permission_classes([IsAuthenticated])
@csrf_exempt
def file_success_saga(request):
    if request.method == 'POST':
        try:
            # Get the file hash and filename from the request
            data = json.loads(request.body)

            transaction_id = data.get('id')

            transaction = TransactionLog.objects.get(_id=ObjectId(transaction_id))

            previous_state = dict(transaction.previous_state)

            action = transaction.action

            if action == 'create':
                state = FileMetadata.objects.get(_id=ObjectId(transaction.document_id))

                filename = f'{state._id}_{state.filename}'
            else:
                filename = f'{transaction.document_id}_{previous_state["filename"]}'

            if action == 'delete':
                os.remove('./temp/' + filename)
            else:
                os.rename('./temp/' + filename, './uploaded_CVs/' + filename)

            # # Delete the metadata from the database
            saga_success(transaction_id)

            # Return a success response
            return JsonResponse({'status': 'success'}, status=200)

        except ValueError:
            # Handle errors caused by invalid JSON data in the request body
            return JsonResponse({'error': 'Invalid JSON data'}, status=400)
        except TransactionLog.DoesNotExist:
            # Handle errors caused by trying to delete a non-existent object
            return JsonResponse({'error': 'Transaction data not found'}, status=404)
        except Exception as e:
            # Handle all other exceptions
            print(e)
            return JsonResponse({'error': 'An error occurred'}, status=500)
    else:
        return JsonResponse({'status': 'error', 'message': 'invalid request method'}, status=400)
