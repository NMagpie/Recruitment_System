FROM openjdk:17-jdk-slim

EXPOSE ${APP_PORT:-8002} ${APP_PORT:-8002}

ENV DOCKER_ENV=true

ENV REGISTER_URL_VAR ${REGISTER_URL:-https://host.docker.internal:8001/auth/register}

ENV REFRESH_URL_VAR ${REFRESH_URL:-https://host.docker.internal:8001/auth/register}

COPY target/authentication-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["sh", "-c", "java -DREGISTER_URL=${REGISTER_URL_VAR} -DREFRESH_URL=${REFRESH_URL_VAR} -DSSL_PASSWORD=rootroot -DSHARED_SECRET_KEY=TestStringSecretKey1234TestStringSecretKey1234TestStringSecretKey1234TestStringSecretKey1234 -jar /app.jar"]