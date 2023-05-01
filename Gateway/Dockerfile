FROM openjdk:17-jdk-slim

EXPOSE ${APP_PORT:-8000} ${APP_PORT:-8000}

ENV DOCKER_ENV=true

COPY target/gateway-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["sh", "-c", "java \
-DSSL_PASSWORD=${SSL_PASSWORD:-rootroot} \
-DSHARED_SECRET_KEY=${SHARED_SECRET_KEY:-TestStringSecretKey1234TestStringSecretKey1234TestStringSecretKey1234TestStringSecretKey1234} \
-jar /app.jar"]