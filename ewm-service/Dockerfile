FROM amazoncorretto:11
COPY target/*.jar ewm-server.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/ewm-server.jar"]