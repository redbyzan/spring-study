FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/ecommerce-1.0.jar discoveryService.jar
ENTRYPOINT ["java","-jar","discoveryService.jar"]
