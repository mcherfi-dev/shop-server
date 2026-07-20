FROM eclipse-temurin:17-jdk-alpine AS build

RUN apk add --no-cache maven

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY --from=build /app/target/shop-app-0.0.1-SNAPSHOT.jar shop-app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "\
  echo 'Waiting for Elasticsearch...' && \
  until curl -sf http://elasticsearch:9200/_cluster/health > /dev/null; do \
    sleep 5; \
  done && \
  echo 'Starting Spring Boot...' && \
  java -jar shop-app.jar \
"]
