import json
from random import shuffle

from bson import ObjectId
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.decorators import api_view, authentication_classes, permission_classes
from rest_framework.permissions import IsAuthenticated

from Recommendation.models import UserData, CVMetadata, Job
from Recommendation.saga_pattern.saga_pattern_util import is_document_locked, prepare_document
from Recommendation.serviceJWTAuthentication import ServiceAuthJWTAuthentication, AuthorizationJWTAuthentication
from django.db.models import Q
from functools import reduce
import operator


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
def get_recommendations(request):
    if request.method == 'GET':
        try:
            user_id = request.GET.get('_id')

            if not user_id:
                return JsonResponse({'status': 'error', 'message': 'missing argument: _id'}, status=400)

            userData = UserData.objects.get(_id=ObjectId(user_id))

            user_tags = userData.tags

            shuffle(user_tags)

            user_searches = userData.searches

            shuffle(user_searches)

            queryset = UserData.objects.all()

            if not user_tags:
                users = []
            else:
                users = queryset.filter(reduce(operator.or_, (Q(tags__icontains=tag) for tag in user_tags)))

            ranked_users = sorted(users, key=lambda u: len(set(u.tags) & set(user_tags)), reverse=True)
            similar_users = ranked_users[:10]

            similar_tags = [tag
                            for user in similar_users
                            for tag in user.tags]

            shuffle(similar_tags)

            limit = 20 if userData.type == 'user' else 100

            recommendations = list(set(user_searches[:limit] + user_tags[:limit] + similar_tags[:limit]))

            document_type = Job if userData.type == 'user' else CVMetadata

            queryset = document_type.objects.all()

            if not recommendations:
                documents = []
            else:
                documents = queryset.filter(reduce(operator.or_, (Q(tags__icontains=tag) for tag in recommendations)))

            ranked_documents = sorted(documents, key=lambda d: len(set(d.tags) & set(recommendations)), reverse=True)
            recommended_documents = [
                {
                    k: str(v) if k == '_id' else v for k,
                v in doc.__dict__.items() if k != 'tags' and k != '_state'
                }
                for doc in ranked_documents[:10]
            ]

            # Return a success response
            return JsonResponse({'status': 'success', 'recommendations': recommended_documents}, status=200)

        except UserData.DoesNotExist:
            # Handle errors caused by trying to delete a non-existent object
            return JsonResponse({'error': 'User data not found'}, status=404)
        except Exception as e:
            # Handle all other exceptions
            print(e)
            return JsonResponse({'error': 'An error occurred'}, status=500)
    else:
        return JsonResponse({'status': 'error', 'message': 'invalid request method'}, status=400)


@api_view(['POST'])
@authentication_classes([
    AuthorizationJWTAuthentication,
    ServiceAuthJWTAuthentication])
@permission_classes([IsAuthenticated])
@csrf_exempt
def upload_user(request):
    if request.method == 'POST':
        try:
            data = json.loads(request.body)

            id = data.get('_id')
            type = data.get('type')
            location = data.get('location')

            if is_document_locked(id, UserData):
                return JsonResponse({'status': 'error', 'message': 'document is locked'}, status=400)

            if not id or \
                    not type or \
                    not location:
                return JsonResponse({'status': 'error', 'message': 'required fields are missing'}, status=400)

            userData = UserData.objects.filter(_id=ObjectId(id)).first()

            if userData:
                tags = userData.tags
                searches = userData.searches
            else:
                tags = [tag.lower().strip() for tag in location.split(',')]
                searches = []

            userData = UserData(
                _id=ObjectId(id),
                type=type,
                location=location,
                tags=tags,
                searches=searches
            )

            transaction_id = prepare_document(userData)

            return JsonResponse({'status': 'success', 'transaction_id': transaction_id}, status=200)

        except json.JSONDecodeError:
            return JsonResponse({'status': 'error', 'message': 'invalid JSON format in request body'}, status=400)
    return JsonResponse({'status': 'error', 'message': 'invalid request method'}, status=400)


@api_view(['POST'])
@authentication_classes([
    AuthorizationJWTAuthentication,
    ServiceAuthJWTAuthentication])
@permission_classes([IsAuthenticated])
@csrf_exempt
def upload_tags(request):
    if request.method == 'POST':
        try:
            data = json.loads(request.body)

            id = data.get('_id')

            tags = data.get('tags')

            userData = UserData.objects.get(_id=ObjectId(id))

            if is_document_locked(id, UserData):
                return JsonResponse({'status': 'error', 'message': 'document is locked'}, status=400)

            if not id or \
                    not tags:
                return JsonResponse({'status': 'error', 'message': 'required fields are missing'}, status=400)

            limit = 250 if userData.type == 'user' else 3000

            tags = list(set(tags + userData.tags))[:limit]

            userData.tags = tags

            transaction_id = prepare_document(userData)

            return JsonResponse({'status': 'success', 'transaction_id': transaction_id}, status=200)

        except json.JSONDecodeError:
            return JsonResponse({'status': 'error', 'message': 'invalid JSON format in request body'}, status=400)
        except UserData.DoesNotExist:
            # Handle errors caused by trying to delete a non-existent object
            return JsonResponse({'error': 'User data not found'}, status=404)
        except Exception as e:
            # Handle all other exceptions
            print(e)
            return JsonResponse({'error': 'An error occurred'}, status=500)
    return JsonResponse({'status': 'error', 'message': 'invalid request method'}, status=400)


@api_view(['POST'])
@authentication_classes([
    AuthorizationJWTAuthentication,
    ServiceAuthJWTAuthentication])
@permission_classes([IsAuthenticated])
@csrf_exempt
def upload_searches(request):
    if request.method == 'POST':
        try:
            data = json.loads(request.body)

            id = data.get('_id')

            searches = data.get('searches')

            userData = UserData.objects.get(_id=ObjectId(id))

            if is_document_locked(id, UserData):
                return JsonResponse({'status': 'error', 'message': 'document is locked'}, status=400)

            if not id or \
                    not searches:
                return JsonResponse({'status': 'error', 'message': 'required fields are missing'}, status=400)

            limit = 250 if userData.type == 'user' else 3000

            searches = [search.lower().strip() for search in searches]

            searches = list(set(searches + userData.searches))[:limit]

            userData.searches = searches

            transaction_id = prepare_document(userData)

            return JsonResponse({'status': 'success', 'transaction_id': transaction_id}, status=200)

        except json.JSONDecodeError:
            return JsonResponse({'status': 'error', 'message': 'invalid JSON format in request body'}, status=400)
        except UserData.DoesNotExist:
            # Handle errors caused by trying to delete a non-existent object
            return JsonResponse({'error': 'User data not found'}, status=404)
        except Exception as e:
            # Handle all other exceptions
            print(e)
            return JsonResponse({'error': 'An error occurred'}, status=500)
    return JsonResponse({'status': 'error', 'message': 'invalid request method'}, status=400)
