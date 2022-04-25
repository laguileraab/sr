FROM openjdk:17 AS spring-boot-upload-mongodb

WORKDIR /app



ARG JAR_FILE=*.jar

COPY target/${JAR_FILE} application.jar

EXPOSE 80

ENTRYPOINT ["java", "-jar", "application.jar"]