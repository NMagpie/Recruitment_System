import json

from bson import ObjectId
from django.http import JsonResponse
from django.utils.decorators import method_decorator
from django.views import View
from django.views.decorators.csrf import csrf_exempt

from .models import CVMetadata
from Search.saga_pattern.saga_pattern_util import is_document_locked, prepare_document

@method_decorator(csrf_exempt, name='dispatch')
class CV_View(View):

    #@csrf_exempt
    def post(self, request):
        if request.method == 'POST':

            try:
                data = json.loads(request.body)

                _id = data.get('_id')
                filename = data.get('filename')
                filetype = data.get('filetype')
                candidate_name = data.get('candidate_name')
                user_id = data.get('user_id')
                tags = data.get('tags')

                if is_document_locked(_id, 'cv_metadata'):
                    return JsonResponse({'status': 'error', 'message': 'document is locked'}, status=400)

                if not _id or \
                        not filename or \
                        not filetype or \
                        not candidate_name or \
                        not user_id or \
                        not tags:
                    return JsonResponse({'status': 'error', 'message': 'required fields are missing'}, status=400)

                cv_metadata = CVMetadata(
                    _id=ObjectId(_id),
                    filename=filename,
                    filetype=filetype,
                    candidate_name=candidate_name,
                    user_id=user_id,
                    tags=tags
                )

                transaction_id = prepare_document(cv_metadata, 'cv_metadata')

                return JsonResponse({'status': 'success', 'transaction_id': transaction_id}, status=200)

            except json.JSONDecodeError:
                return JsonResponse({'status': 'error', 'message': 'invalid JSON format in request body'}, status=400)
        return JsonResponse({'status': 'error', 'message': 'invalid request method'}, status=400)

    #@csrf_exempt
    def delete(self, request):

        if request.method == 'DELETE':
            try:
                cv_id = request.GET.get('_id')

                if not cv_id:
                    return JsonResponse({'status': 'error', 'message': 'missing argument: _id'}, status=400)

                if is_document_locked(cv_id, 'cv_metadata'):
                    return JsonResponse({'status': 'error', 'message': 'document is locked'}, status=400)

                document = CVMetadata.objects.filter(_id=ObjectId(cv_id)).first()

                # Delete the metadata from the database
                transaction_id = prepare_document(document, 'cv_metadata', 'delete')

                # Return a success response
                return JsonResponse({'status': 'success', 'transaction_id': transaction_id}, status=200)

            except CVMetadata.DoesNotExist:
                # Handle errors caused by trying to delete a non-existent object
                return JsonResponse({'error': 'CV data not found'}, status=404)
            except Exception as e:
                # Handle all other exceptions
                print(e)
                return JsonResponse({'error': 'An error occurred'}, status=500)
        else:
            return JsonResponse({'status': 'error', 'message': 'invalid request method'}, status=400)
