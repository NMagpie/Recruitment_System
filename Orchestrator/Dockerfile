FROM openjdk:17-jdk-slim

EXPOSE ${APP_PORT:-8002} ${APP_PORT:-8002}

ENV DOCKER_ENV=true

ENV EUREKA_URL_DEFAULT_ZONE_VAR ${EUREKA_URL_DEFAULT_ZONE:-http://eureka:8761/eureka}

ENV EUREKA_URL_VAR ${EUREKA_URL:-http://eureka:8761/}

COPY target/orchestrator-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["sh", "-c", "java -DSSL_PASSWORD=rootroot -DSHARED_SECRET_KEY=TestStringSecretKey1234TestStringSecretKey1234TestStringSecretKey1234TestStringSecretKey1234 \
-DEUREKA_URL_DEFAULT_ZONE=${EUREKA_URL_DEFAULT_ZONE_VAR} -DEUREKA_URL=${EUREKA_URL_VAR} \
 -jar /app.jar"]