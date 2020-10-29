FROM openjdk:8-jdk-alpine
ARG FROM_JAR_PATH=target/customer-service-1.0-SNAPSHOT.jar
ARG TO_JAR_PATH=/customer-service.jar
COPY ${FROM_JAR_PATH} ${TO_JAR_PATH}
ENTRYPOINT ["java", "-jar", "/customer-service.jar"]