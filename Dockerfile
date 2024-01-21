FROM openjdk:8
WORKDIR /app
COPY target/gs-spring-boot-0.1.0.jar /app
ENTRYPOINT ["java", "-jar", "gs-spring-boot-0.1.0.jar"]