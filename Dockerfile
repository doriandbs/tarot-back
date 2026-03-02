FROM maven:3.8.5-openjdk-17 as build
COPY . .
RUN mvn clean package spring-boot:repackage -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/suivi-tarot-1.0-SNAPSHOT.jar back.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","back.jar"]
