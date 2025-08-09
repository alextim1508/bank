FROM openjdk:21-jdk

WORKDIR /bank

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java",  "-jar", "app.jar"]