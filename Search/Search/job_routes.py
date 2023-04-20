import json

from bson import ObjectId
from django.http import JsonResponse
from django.utils.decorators import method_decorator
from django.views import View
from django.views.decorators.csrf import csrf_exempt

from .models import Job
from .saga_pattern.saga_pattern_util import is_document_locked, prepare_document


@method_decorator(csrf_exempt, name='dispatch')
class Job_View(View):
    # @csrf_exempt
    def post(self, request):
        if request.method == 'POST':
            try:
                data = json.loads(request.body)

                _id = data.get('_id')
                user_id = data.get('user_id')
                title = data.get('title')
                description = data.get('description')
                location = data.get('location')
                tags = data.get('tags')

                if is_document_locked(_id, Job):
                    return JsonResponse({'status': 'error', 'message': 'document is locked'}, status=400)

                if not _id or \
                        not user_id or \
                        not title or \
                        not description or \
                        not location or \
                        not tags:
                    return JsonResponse({'status': 'error', 'message': 'required fields are missing'}, status=400)

                job = Job(
                    _id=ObjectId(_id),
                    user_id=user_id,
                    title=title,
                    description=description,
                    location=location,
                    tags=tags
                )

                transaction_id = prepare_document(job)

                return JsonResponse({'status': 'success', 'transaction_id': transaction_id}, status=200)
            except json.JSONDecodeError:
                return JsonResponse({'status': 'error', 'message': 'invalid JSON format in request body'}, status=400)
        return JsonResponse({'status': 'error', 'message': 'Invalid request method'}, status=400)

    # @csrf_exempt
    def delete(self, request):
        if request.method == 'DELETE':
            try:
                job_id = request.GET.get('_id')

                if not job_id:
                    return JsonResponse({'status': 'error', 'message': 'missing argument: _id'}, status=400)

                if is_document_locked(job_id, Job):
                    return JsonResponse({'status': 'error', 'message': 'document is locked'}, status=400)

                document = Job.objects.filter(_id=ObjectId(job_id)).first()

                # Delete the data from the database
                transaction_id = prepare_document(document, 'delete')

                # Return a success response
                return JsonResponse({'status': 'success', 'transaction_id': transaction_id}, status=200)

            except Job.DoesNotExist:
                # Handle errors caused by trying to delete a non-existent object
                return JsonResponse({'error': 'Job data not found'}, status=404)
            except Exception as e:
                # Handle all other exceptions
                print(e)
                return JsonResponse({'error': 'An error occurred'}, status=500)
        else:
            return JsonResponse({'status': 'error', 'message': 'invalid request method'}, status=400)
