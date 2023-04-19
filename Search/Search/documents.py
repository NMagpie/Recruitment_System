from django_elasticsearch_dsl import Document, fields
from django_elasticsearch_dsl.registries import registry

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
            'description',
            'location'
        ]
