use authentication
db.createCollection("transactions")
db.createCollection("users")

use cv_processing
db.createCollection("transactions")
db.createCollection("uploaded_cv_files_info")

use job_posting
db.createCollection("transactions")
db.createCollection("uploaded_jobs_info")

use recommendation
db.createCollection("transactions")
db.createCollection("users_data")

use search
db.createCollection("transactions")
db.createCollection("cv")
db.createCollection("jobs")