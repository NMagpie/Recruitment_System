import json

from django import forms
from django.core.exceptions import ValidationError
from django.db import models


class FieldsArrayField(models.Field):
    description = "Field of the field array"

    def __init__(self, base_field=None, *args, **kwargs):
        if not base_field:
            raise ValueError('You must specify a base_field')
        self.base_field = base_field
        super().__init__(*args, **kwargs)

    def db_type(self, connection):
        return '{}[]'.format(self.base_field.db_type(connection=connection))

    def from_db_value(self, value, expression, connection):
        if value is None:
            return []
        return value

    def to_python(self, value):
        if isinstance(value, list):
            return [self.base_field.to_python(val) for val in value]
        elif value is None:
            return []
        elif isinstance(value, str):
            # If the value is a string, try to parse it as a JSON array
            try:
                value = json.loads(value)
                return [self.base_field.to_python(val) for val in value]
            except ValueError:
                pass
        raise ValidationError("Invalid value for ArrayField")

    def get_prep_value(self, value):
        if isinstance(value, list):
            return [self.base_field.get_prep_value(val) for val in value]
        elif value is None:
            return []
        else:
            return json.dumps([self.base_field.get_prep_value(val) for val in value])

    def formfield(self, **kwargs):
        defaults = {
            'form_class': forms.CharField,
            'widget': forms.Textarea(attrs={'rows': 2}),
        }
        defaults.update(kwargs)
        return super().formfield(**defaults)
