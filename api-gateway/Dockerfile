FROM openjdk:8-jdk-alpine
ARG FROM_JAR_PATH=target/api-gateway-1.0-SNAPSHOT.jar
ARG TO_JAR_PATH=/api-gateway.jar
COPY ${FROM_JAR_PATH} ${TO_JAR_PATH}
ENTRYPOINT ["java", "-jar", "/api-gateway.jar"]