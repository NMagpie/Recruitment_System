FROM python:3.10

WORKDIR /app

ENV APP_PORT_VAR ${APP_PORT:-8004}

COPY requirements.txt .
RUN pip install -r requirements.txt

RUN pip install --force-reinstall -v "pymongo==3.12.3"

COPY . .

EXPOSE ${APP_PORT_VAR} ${APP_PORT_VAR}

CMD ["sh", "-c", "python manage.py runsslserver 0.0.0.0:${APP_PORT_VAR} \
 --certificate ${CERTIFICATE_LOCATION:-./secrets/certificate.pem} \
 --key ${KEY_LOCATION:-./secrets/key.pem}"]
