from django.shortcuts import render
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from .models import Job
import json
from bson import ObjectId


@csrf_exempt
def upload_job(request):
    if request.method == 'POST':
        data = json.loads(request.body)
        job = Job(
            user_id=data['user_id'],
            title=data['title'],
            description=data['description'],
            location=data['location']
        )

        job.save()

        return JsonResponse({'status': 'success', '_id': str(job._id)}, status=200)
    else:
        return JsonResponse({'error': 'Invalid request method'}, status=400)


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
            'title': job.title,
            'description': job.description,
            'location': job.location
        }
        return JsonResponse(job_dict, status=200)

    elif request.method == 'PUT':
        data = json.loads(request.body)
        job.title = data['title']
        job.description = data['description']
        job.location = data['location']
        job.save()
        return JsonResponse({'status': 'success'}, status=200)

    elif request.method == 'DELETE':
        job.delete()
        return JsonResponse({'status': 'success'}, status=200)

    return JsonResponse({'error': 'Invalid request method'}, status=400)
