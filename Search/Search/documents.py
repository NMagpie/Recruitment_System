import sys
import time

from django_elasticsearch_dsl import Document, fields
from django_elasticsearch_dsl.registries import registry
from elasticsearch.client import Elasticsearch
from elasticsearch_dsl import Index

from . import settings
from .models import CVMetadata, Job


@registry.register_document
class CVMetadataDocument(Document):
    class Index:
        name = 'cvs'

    tags = fields.ListField(fields.TextField())
    db_id = fields.TextField(attr="id_to_str")

    def prepare_tags(self, instance):
        return instance.tags

    @classmethod
    def generate_id(cls, object_instance):
        return str(object_instance._id)

    class Django:
        model = CVMetadata

        fields = [
            'filename',
            'filetype',
            'candidate_name',
            'user_id'
        ]


@registry.register_document
class JobDocument(Document):
    class Index:
        name = 'jobs'

    tags = fields.ListField(fields.TextField())
    db_id = fields.TextField(attr="id_to_str")

    def prepare_tags(self, instance):
        return instance.tags

    @classmethod
    def generate_id(cls, object_instance):
        return str(object_instance._id)

    class Django:
        model = Job

        fields = [
            'title',
            'user_id',
            'company_name',
            'description',
            'location'
        ]


retry_count = 0
max_retries = 5


def create_indexes():
    global retry_count, max_retries
    retry_count += 1

    client = Elasticsearch(settings.ELASTICSEARCH_DSL['default']['hosts'])

    try:
        index = Index('cvs')
        if not index.exists(using=client):
            index.document(CVMetadataDocument)
            index.create()

        index = Index('jobs')
        if not index.exists(using=client):
            index.document(JobDocument)
            index.create()

    except Exception:
        if retry_count >= max_retries:
            print('Maximum number of Elasticsearch tries has exceeded')
            sys.exit(1)
        else:
            delay = 2 ** retry_count
            print(f'Retrying Elasticsearch connect in {delay} seconds. Attempt {retry_count}')
            time.sleep(delay)
            create_indexes()
            return

    print('Elastic search connected successfully!')

    retry_count = -1
