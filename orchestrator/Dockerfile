FROM openjdk:8-jdk-alpine
ARG FROM_JAR_PATH=target/orchestrator-1.0-SNAPSHOT.jar
ARG TO_JAR_PATH=/orchestrator.jar
COPY ${FROM_JAR_PATH} ${TO_JAR_PATH}
ENTRYPOINT ["java", "-jar", "/orchestrator.jar"]