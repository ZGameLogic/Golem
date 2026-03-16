FROM --platform=linux/arm64 eclipse-temurin:25-jre-alpine
LABEL authors="Ben Shabowski"

ARG SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

WORKDIR /app
COPY /target/golem-1.0.0.jar /app/golem-1.0.0.jar

EXPOSE 8080

CMD ["java", "-jar", "golem-1.0.0.jar"]
