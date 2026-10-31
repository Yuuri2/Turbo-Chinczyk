# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

#Najpierw sam pom.xml - warstwa z zaleznosciami jest cache'owana,
#a ~/.m2 trzymamy w cache BuildKita miedzy buildami
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q package -DskipTests \
 && cp target/*.jar app.jar \
 && java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

#runtime
FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.source=https://github.com/Yuuri2/Turbo-Chinczyk
WORKDIR /app

#Proces w kontenerze nie powinien chodzic na roocie
RUN adduser -D spring
USER spring

#Warstwy od najrzadziej zmienianych - przy deployu VM pobiera zwykle tylko `application`
COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./

EXPOSE 8080
#Heap liczony od limitu pamieci kontenera (mem_limit w compose), przy OOM kontener sie restartuje
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "org.springframework.boot.loader.launch.JarLauncher"]
