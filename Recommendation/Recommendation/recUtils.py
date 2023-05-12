from sklearn.feature_extraction.text import CountVectorizer, HashingVectorizer

from Recommendation.models import Job, CVMetadata

import pandas as pd

job_vectorizer = HashingVectorizer()

cv_vectorizer = HashingVectorizer()


def update_vectors():
    # Get all objects from DB
    jobs = pd.DataFrame(Job.objects.all())

    job_vectorizer.fit_transform()

    del jobs

    #cvs = CVMetadata.objects.all()
