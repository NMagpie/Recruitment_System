import json

from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from elasticsearch_dsl import Q, IllegalOperation
from rest_framework.decorators import api_view, authentication_classes, permission_classes
from rest_framework.permissions import IsAuthenticated

from .documents import CVMetadataDocument, JobDocument
from .serviceJWTAuthentication import AuthorizationJWTAuthentication, ServiceAuthJWTAuthentication


@api_view(['GET'])
@csrf_exempt
def health():
    return JsonResponse({'status': 'UP'}, status=200)


@api_view(['GET'])
@authentication_classes([
    AuthorizationJWTAuthentication,
    ServiceAuthJWTAuthentication])
@permission_classes([IsAuthenticated])
@csrf_exempt
def search_data(request, search_type):
    if request.method == 'GET':

        if search_type == 'cvs':
            document = CVMetadataDocument
        elif search_type == 'jobs':
            document = JobDocument

        try:
            tags = request.GET.getlist('q')
            offset = int(request.GET.get('offset'))
        except ValueError:
            return JsonResponse({'status': 'error', 'message': 'offset must be integer'}, status=400)

        if offset < 0:
            return JsonResponse({'status': 'error', 'message': 'offset must be greater or equal to 0'}, status=400)

        if not len(tags):
            return JsonResponse({'status': 'error', 'message': 'need at least one tag for search'}, status=400)

        should_clause = [Q('fuzzy', tags={'value': tag, 'fuzziness': 'AUTO'}) for tag in tags] + \
                        [Q('match_phrase', tags={'query': tag, 'slop': 1}) for tag in tags]

        try:
            search_results = document.search() \
                .query(Q('bool', should=should_clause)) \
                .sort('_score') \
                .source(excludes=['tags']) \
                .extra(size=10, from_=10 * offset)

            count = search_results.count()

            results = [result.to_dict() for result in search_results]

        except IllegalOperation:
            return JsonResponse({'status': 'error', 'message': 'invalid search query'}, status=400)
        except Exception as e:
            print(e)
            return JsonResponse({'status': 'error', 'message': 'an error occurred'}, status=500)

        return JsonResponse({'status': 'success', 'count': count, 'results': results}, status=200)
    else:
        return JsonResponse({'status': 'error', 'message': 'invalid request method'}, status=400)
