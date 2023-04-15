import json

from bson import ObjectId
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from elasticsearch_dsl import Q

from .documents import JobDocument
from .models import Job


@csrf_exempt
def save_job(request):
    if request.method == 'POST':
        data = json.loads(request.body)

        job = Job(
            _id=ObjectId(data.get('_id')),
            user_id=data.get('user_id'),
            title=data.get('title'),
            description=data.get('description'),
            location=data.get('location'),
            tags=json.dumps(data.get('tags'))
        )

        job.save()

        return JsonResponse({'status': 'success'})
    return JsonResponse({'status': 'error'})


@csrf_exempt
def search_job(request):
    if request.method == 'GET':

        tags = request.GET.getlist('q')

        try:
            offset = int(request.GET.get('offset'))
        except:
            return JsonResponse({'status': 'error', 'message': 'offset must be integer'})

        if offset < 0:
            return JsonResponse({'status': 'error', 'message': 'offset must be greater or equal to 0'})

        if not len(tags):
            return JsonResponse({'status': 'error', 'message': 'need at least one tag for search'})

        should_clause = [Q('fuzzy', tags={'value': tag, 'fuzziness': 'AUTO'}) for tag in tags] + \
                        [Q('match_phrase', tags={'query': tag, 'slop': 1}) for tag in tags]

        search_results = JobDocument.search() \
            .query(Q('bool', should=should_clause)) \
            .sort('_score') \
            .extra(size=10, from_=10 * offset)

        count = search_results.count(),
        results = [(result.to_dict()) for result in search_results]

        return JsonResponse({'status': 'success', 'count': count, 'results': results})
    return JsonResponse({'status': 'error'})


@csrf_exempt
def delete_job(request):
    if request.method == 'POST':
        # Get the file hash and filename from the request
        data = json.loads(request.body)

        job_id = data.get('id')

        # Delete the metadata from the database
        Job.objects.filter(_id=ObjectId(job_id)).delete()

        # Return a success response
        return JsonResponse({'status': 'success'}, status=200)
    else:
        return JsonResponse({'error': 'Invalid request method'}, status=400)
