import PyPDF2 as PyPDF2
import spacy
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.decorators import api_view, authentication_classes, permission_classes
from rest_framework.permissions import IsAuthenticated

from .models import Job
import json
from bson import ObjectId
from django.forms import model_to_dict

from .saga_pattern.saga_pattern_util import is_document_locked, prepare_document
from .serviceJWTAuthentication import ServiceAuthJWTAuthentication, AuthorizationJWTAuthentication

nlp = spacy.load('en_core_web_sm')


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
def upload_job(request):
    if request.method == 'POST':
        data = json.loads(request.body)
        job = Job(
            user_id=data['user_id'],
            company_name=data['company_name'],
            title=data['title'],
            description=data['description'],
            location=data['location'],
            tags=[]
        )

        if is_document_locked(str(job._id), Job):
            return JsonResponse({'status': 'error', 'message': 'document is locked'}, status=400)

        doc = nlp(data['description'])

        keywords = set()
        for token in doc:
            if not token.is_stop and token.is_alpha and token.pos_ in ['NOUN', 'PROPN']:
                keywords.add(token.text.lower())

        job.tags = list(keywords)

        transaction_id = prepare_document(job, 'create')

        json_job = model_to_dict(job)

        json_job['_id'] = str(json_job['_id'])

        return JsonResponse({'status': 'success', 'transaction_id': transaction_id, 'data': json_job}, status=200)
    else:
        return JsonResponse({'status': 'error', 'message': 'invalid request method'}, status=400)


@api_view(['GET', 'PUT', 'DELETE'])
@authentication_classes([
    AuthorizationJWTAuthentication,
    ServiceAuthJWTAuthentication])
@permission_classes([IsAuthenticated])
@csrf_exempt
def rud_job(request, id):
    try:
        job = Job.objects.get(_id=ObjectId(id))
    except:
        return JsonResponse({'error': 'Job posting does not exist'}, status=404)

    if request.method == 'GET':
        job_dict = {
            '_id': str(job._id),
            'user_id': job.user_id,
            'company_name': job.company_name,
            'title': job.title,
            'description': job.description,
            'location': job.location
        }
        return JsonResponse(job_dict, status=200)

    elif request.method == 'PUT':
        data = json.loads(request.body)

        if is_document_locked(str(job._id), Job):
            return JsonResponse({'status': 'error', 'message': 'document is locked'}, status=400)

        job.title = data['title']
        job.company_name = data['company_name']
        job.description = data['description']
        job.location = data['location']

        transaction_id = prepare_document(job, 'update')

        return JsonResponse({'status': 'success', 'transaction_id': transaction_id}, status=200)

    elif request.method == 'DELETE':

        if is_document_locked(str(job._id), Job):
            return JsonResponse({'status': 'error', 'message': 'document is locked'}, status=400)

        transaction_id = prepare_document(job, 'delete')

        return JsonResponse({'status': 'success', 'transaction_id': transaction_id}, status=200)

    return JsonResponse({'error': 'Invalid request method'}, status=400)


@api_view(['GET'])
@authentication_classes([
    AuthorizationJWTAuthentication,
    ServiceAuthJWTAuthentication])
@permission_classes([IsAuthenticated])
@csrf_exempt
def get_user_jobs(request, userId):
    try:
        jobs = Job.objects.filter(user_id=userId)

        response = [{
            '_id': str(job._id),
            'user_id': job.user_id,
            'company_name': job.company_name,
            'title': job.title,
            'description': job.description,
            'location': job.location
        } for job in jobs]
        return JsonResponse({'documents': response}, status=200)
    except Job.DoesNotExist:
        return JsonResponse({'documents': []}, status=200)

def extract_text_from_pdf(file):
    pdf_reader = PyPDF2.PdfReader(file)
    text = ''
    for page in pdf_reader.pages:
        text += page.extract_text()
    return text
