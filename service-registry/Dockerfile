FROM openjdk:8-jdk-alpine
ARG FROM_JAR_PATH=target/service-registry-1.0-SNAPSHOT.jar
ARG TO_JAR_PATH=/service-registry.jar
COPY ${FROM_JAR_PATH} ${TO_JAR_PATH}
ENTRYPOINT ["java", "-jar", "/service-registry.jar"]