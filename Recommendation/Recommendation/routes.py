import json

from bson import ObjectId
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.decorators import api_view, authentication_classes, permission_classes
from rest_framework.permissions import IsAuthenticated

from Recommendation.models import UserData
from Recommendation.saga_pattern.saga_pattern_util import is_document_locked, prepare_document
from Recommendation.serviceJWTAuthentication import ServiceAuthJWTAuthentication, AuthorizationJWTAuthentication


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

            recommendations = list(set(userData.tags + userData.searches))

            # Return a success response
            return JsonResponse({'status': 'success', 'recommendations': recommendations}, status=200)

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
                tags = []
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

            limit = 30 if userData.type == 'user' else 100

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

            limit = 30 if userData.type == 'user' else 100

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
