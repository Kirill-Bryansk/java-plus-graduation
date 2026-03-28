FROM amazoncorretto:21
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} stats-server.jar
ENTRYPOINT ["java", "-jar", "/stats-server.jar"]