import hashlib
import json
import os

from bson import ObjectId
from django.core.management.commands.runserver import Command as runserver
from django.forms import model_to_dict
from django.http import HttpResponseBadRequest, HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt

from .models import FileMetadata

class CustomJSONEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, ObjectId):
            return str(o)
        return super().default(o)

@csrf_exempt
def upload_cv(request):
    if request.method == 'POST' and request.FILES.get('file'):
        # Get the file and metadata from the request
        cv_file = request.FILES['file']
        candidate_name = request.POST.get('candidate_name')
        user_id = request.POST.get('user_id')

        # Check if the file is a PDF
        if cv_file.content_type != 'application/pdf':
            return HttpResponseBadRequest('File must be a PDF')

        # hash_object = hashlib.sha256(cv_file.read())
        # cv_file_hash = hash_object.hexdigest()

        cv_file_hash = ObjectId()

        cv_updated_name = cv_file.name.replace(' ', '_')

        # Save the file to the local filesystem
        cv_file_path = os.path.join('./uploaded_CVs/', f'{cv_file_hash}_{cv_updated_name}')
        with open(cv_file_path, 'wb+') as destination:
            for chunk in cv_file.chunks():
                destination.write(chunk)

        metadata = FileMetadata(_id=cv_file_hash,
                                filename=cv_updated_name,
                                filetype=cv_file.content_type,
                                candidate_name=candidate_name,
                                user_id=user_id,
                                tags=['test', 'test1'])
        metadata.save()

        json_metadata = model_to_dict(metadata)

        json_metadata['_id'] = str(json_metadata['_id'])

        # Return a success response
        return JsonResponse({'status': 'success', 'data': json_metadata}, status=201)
    else:
        return JsonResponse({'error': 'Invalid request method or missing file'}, status=400)

@csrf_exempt
def delete_cv(request):
    if request.method == 'POST':
        # Get the file hash and filename from the request
        cv_file_hash = request.POST.get('file_hash')
        cv_file_name = request.POST.get('file_name')

        # Delete the file from the local filesystem
        cv_file_path = os.path.join('./uploaded_CVs/', f'{cv_file_hash}_{cv_file_name}')
        os.remove(cv_file_path)

        # Delete the metadata from the database
        FileMetadata.objects.filter(_id=ObjectId(cv_file_hash)).delete()

        # Return a success response
        return JsonResponse({'status': 'success'}, status=200)
    else:
        return JsonResponse({'error': 'Invalid request method'}, status=400)


@csrf_exempt
def cv_details(request, id):
    try:
        cv_file = FileMetadata.objects.get(_id=ObjectId(id))
        response = {
            '_id': str(cv_file._id),
            'filename': cv_file.filename,
            'filetype': cv_file.filetype,
            'candidate_name': cv_file.candidate_name,
            'user_id': cv_file.user_id,
            'download_link': f'https://{runserver.default_addr}:{runserver.default_port}/cv/download/{str(cv_file._id)}_{cv_file.filename}'
        }
        return JsonResponse(response, status=200)
    except FileMetadata.DoesNotExist:
        return JsonResponse({'error': 'CV file does not exist'}, status=404)


@csrf_exempt
def cv_download(request, filename):
    file_path = os.path.join('./uploaded_CVs', filename)

    if os.path.exists(file_path):
        with open(file_path, 'rb') as file:
            response = HttpResponse(file.read())
            response['Content-Type'] = 'application/pdf'
            response['Content-Disposition'] = f'attachment; filename="{filename}"'

            return response
    else:
        return JsonResponse({'error': 'CV file does not exist'}, status=404)
