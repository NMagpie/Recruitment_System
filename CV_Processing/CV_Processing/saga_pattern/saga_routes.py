import json

from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from CV_Processing.saga_pattern.TransactionLogModel import TransactionLog
from CV_Processing.saga_pattern.saga_pattern_util import saga_fail, saga_success

from django.urls import path


# Saga Pattern Routes
def get_saga_urls():
    return [
        path('rollback/', rollback_saga, name='rollback_saga'),
        path('success/', success_saga, name='success_saga'),
    ]


@csrf_exempt
def rollback_saga(request):
    if request.method == 'POST':
        try:
            # Get the file hash and filename from the request
            data = json.loads(request.body)

            transaction_id = data.get('id')

            # # Delete the metadata from the database
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


@csrf_exempt
def success_saga(request):
    if request.method == 'POST':
        try:
            # Get the file hash and filename from the request
            data = json.loads(request.body)

            transaction_id = data.get('id')

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
