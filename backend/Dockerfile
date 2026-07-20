#build 
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

#Najpierw sam pom.xml - warstwa z zaleznosciami jest cache'owana
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q package -DskipTests

#runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

#Proces w kontenerze nie powinien chodzic na roocie
RUN adduser -D spring
USER spring

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
