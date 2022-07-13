FROM adoptopenjdk/openjdk11:latest
MAINTAINER dan4ik1707 <danyil.shykh@gmail.com>
#ARG JAR_FILE=target/*.jar
#COPY ${JAR_FILE} app.jar
COPY target/library-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]