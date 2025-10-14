FROM gradle:8.5-jdk17-alpine AS build
WORKDIR /home/gradle/src
COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle ./gradle/
COPY src ./src
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]