FROM python:3.10

WORKDIR /app

ENV APP_PORT_VAR ${APP_PORT:-8003}

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY . .

EXPOSE ${APP_PORT_VAR} ${APP_PORT_VAR}

CMD ["sh", "-c", "python manage.py runsslserver 0.0.0.0:${APP_PORT_VAR}"]
