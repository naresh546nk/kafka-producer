FROM openjdk:8
EXPOSE 8080
ADD target/liberary-inventory-0.0.1-SNAPSHOT.jar liberary-inventory-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/liberary-inventory-0.0.1-SNAPSHOT.jar"]