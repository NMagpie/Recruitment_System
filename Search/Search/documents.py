from django_elasticsearch_dsl import Document, fields
from django_elasticsearch_dsl.registries import registry

from .models import CVMetadata


@registry.register_document
class CVMetadataDocument(Document):
    class Index:
        name = 'cvs'

    tags = fields.TextField(attr="tags_to_list")
    db_id = fields.TextField(attr="id_to_str")

    @classmethod
    def generate_id(cls, object_instance):
        return str(object_instance._id)

    class Django:
        model = CVMetadata
        fields = ['filename', 'filetype', 'candidate_name', 'user_id']
