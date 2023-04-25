FROM openjdk:17-jdk-slim

EXPOSE ${APP_PORT:-8002} ${APP_PORT:-8002}

ENV DOCKER_ENV=true

COPY target/orchestrator-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-DSSL_PASSWORD=rootroot","-DSHARED_SECRET_KEY=TestStringSecretKey1234TestStringSecretKey1234TestStringSecretKey1234TestStringSecretKey1234","-jar","/app.jar"]