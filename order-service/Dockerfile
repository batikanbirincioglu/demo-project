FROM openjdk:8-jdk-alpine
ARG FROM_JAR_PATH=target/order-service-1.0-SNAPSHOT.jar
ARG TO_JAR_PATH=/order-service.jar
COPY ${FROM_JAR_PATH} ${TO_JAR_PATH}
ENTRYPOINT ["java", "-jar", "/order-service.jar"]