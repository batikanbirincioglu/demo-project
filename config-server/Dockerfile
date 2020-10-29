FROM openjdk:8-jdk-alpine
ARG FROM_JAR_PATH=target/config-server-1.0-SNAPSHOT.jar
ARG TO_JAR_PATH=/config-server.jar
COPY ${FROM_JAR_PATH} ${TO_JAR_PATH}
ENTRYPOINT ["java", "-jar", "/config-server.jar"]