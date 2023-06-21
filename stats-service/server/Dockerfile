FROM amazoncorretto:11
COPY target/*.jar stats-server.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/stats-server.jar"]