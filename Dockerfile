FROM openjdk:8-jre-alpine
VOLUME /tmp
EXPOSE 9092
ADD build/libs/gateway-service-0.0.1-SNAPSHOT.jar gateway-service.jar
ENTRYPOINT ["java","-jar","gateway-service.jar"]
