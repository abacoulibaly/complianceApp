FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app 

COPY pom.xml .
RUN mvn -q -B dependency:go-offline -DskipTests

COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app 

RUN addgroup -S compliance && adduser -S compliance -G compliance \
    && apk add --no-cache curl

USER compliance

COPY --from=build /app/target/compliance-automation-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
