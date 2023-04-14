import json

from bson import ObjectId
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from elasticsearch_dsl import Q

from .documents import CVMetadataDocument
from .models import CVMetadata

@csrf_exempt
def save_cv_metadata(request):
    if request.method == 'POST':
        data = json.loads(request.body)

        cv_metadata = CVMetadata(
            filename=data.get('filename'),
            filetype=data.get('filetype'),
            candidate_name=data.get('candidate_name'),
            user_id=data.get('user_id'),
            tags=json.dumps(data.get('tags'))
        )

        cv_metadata.save()

        return JsonResponse({'status': 'success'})
    return JsonResponse({'status': 'error'})


def search_cv_metadata(request):
    if request.method == 'GET':

        tags = request.GET.getlist('q')

        if not len(tags):
            return JsonResponse({'status': 'error', 'message': 'need at least one tag for search'})

        fuzzy_query = Q('bool', should=[Q('fuzzy', tags={'value': tag}) for tag in tags])

        search_results = CVMetadataDocument.search().query(fuzzy_query)

        count = search_results.count(),
        results = [result.to_dict() for result in search_results]

        return JsonResponse({'status': 'success', 'count': count, 'results': results})
    return JsonResponse({'status': 'error'})
