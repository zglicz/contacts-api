FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/contacts-api-0.0.1-SNAPSHOT.jar contacts-api-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/contacts-api-0.0.1-SNAPSHOT.jar"]
