import hashlib
import json
import os
import socket

from bson import ObjectId
from django.forms import model_to_dict
from django.http import HttpResponseBadRequest, HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.decorators import api_view, authentication_classes, permission_classes
from rest_framework.permissions import IsAuthenticated

from .models import FileMetadata
from .saga_pattern.saga_pattern_util import is_document_locked, prepare_document
from .serviceJWTAuthentication import AuthorizationJWTAuthentication, ServiceAuthJWTAuthentication
from .settings import APP_PORT_VAR


class CustomJSONEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, ObjectId):
            return str(o)
        return super().default(o)


@api_view(['GET'])
@csrf_exempt
def health():
    return JsonResponse({'status': 'UP'}, status=200)


@api_view(['POST'])
@authentication_classes([
    AuthorizationJWTAuthentication,
    ServiceAuthJWTAuthentication])
@permission_classes([IsAuthenticated])
@csrf_exempt
def upload_cv(request):
    if request.method == 'POST' and request.FILES.get('file'):
        # Get the file and metadata from the request
        cv_file = request.FILES['file']
        candidate_name = request.POST.get('candidate_name')
        user_id = request.POST.get('user_id')

        file_binary = cv_file.read()

        # Check if the file is a PDF
        if cv_file.content_type != 'application/pdf':
            return HttpResponseBadRequest('File must be a PDF')

        cv_file_hash = ObjectId()

        if is_document_locked(cv_file_hash, FileMetadata):
            return JsonResponse({'status': 'error', 'message': 'document is locked'}, status=400)

        cv_updated_name = cv_file.name.replace(' ', '_')

        # Save the file to the local filesystem
        # cv_file_path = os.path.join('./temp/', f'{cv_file_hash}_{cv_updated_name}')
        # with open(cv_file_path, 'wb+') as destination:
        #     for chunk in cv_file.chunks():
        #         destination.write(chunk)

        metadata = FileMetadata(_id=cv_file_hash,
                                filename=cv_updated_name,
                                filetype=cv_file.content_type,
                                candidate_name=candidate_name,
                                user_id=user_id,
                                tags=['test321', '321test', 'djongo', 'pymongo'],
                                file=file_binary)

        transaction_id = prepare_document(metadata, 'create')

        json_metadata = model_to_dict(metadata)

        json_metadata['_id'] = str(json_metadata['_id'])

        # Return a success response
        return JsonResponse({'status': 'success', 'transaction_id': transaction_id, 'data': json_metadata}, status=201)
    else:
        return JsonResponse({'error': 'Invalid request method or missing file'}, status=400)


@api_view(['DELETE'])
@authentication_classes([
    AuthorizationJWTAuthentication,
    ServiceAuthJWTAuthentication])
@permission_classes([IsAuthenticated])
@csrf_exempt
def delete_cv(request, id):
    if request.method == 'DELETE':
        try:
            # Get the file hash and filename from the request

            metadata = FileMetadata.objects.get(_id=ObjectId(id))

            transaction_id = prepare_document(metadata, 'delete')

            # Return a success response
            return JsonResponse({'status': 'success', 'transaction_id': transaction_id}, status=200)

        except FileMetadata.DoesNotExist:
            return JsonResponse({'status': 'error', 'message': 'CV Not Found'}, status=404)
    else:
        return JsonResponse({'status': 'error', 'message': 'Invalid request method'}, status=400)


@api_view(['GET'])
@authentication_classes([
    AuthorizationJWTAuthentication,
    ServiceAuthJWTAuthentication])
@permission_classes([IsAuthenticated])
@csrf_exempt
def cv_details(request, id):
    try:
        cv_file = FileMetadata.objects.get(_id=ObjectId(id))
        response = {
            '_id': str(cv_file._id),
            'filename': cv_file.filename,
            'filetype': cv_file.filetype,
            'candidate_name': cv_file.candidate_name,
            'user_id': cv_file.user_id
        }
        return JsonResponse(response, status=200)
    except FileMetadata.DoesNotExist:
        return JsonResponse({'error': 'CV file does not exist'}, status=404)


@api_view(['GET'])
@authentication_classes([
    AuthorizationJWTAuthentication,
    ServiceAuthJWTAuthentication
])
@permission_classes([IsAuthenticated])
@csrf_exempt
def cv_download(request, id):
    if '../' in id:
        return JsonResponse({'error': 'Invalid file path'}, status=400)

    try:

        metadata = FileMetadata.objects.get(_id=ObjectId(id))

        response = HttpResponse(metadata.file)
        response['Content-Type'] = metadata.filetype
        response['Content-Disposition'] = f'attachment; filename="{metadata.filename}"'

        return response
    except FileMetadata.DoesNotExist:
        return JsonResponse({'error': 'CV file does not exist'}, status=404)
