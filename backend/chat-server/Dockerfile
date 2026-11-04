FROM eclipse-temurin:17-jdk@sha256:43338b737d0f75d6883d3c4563bc66974113ca8ac296e82f39d161fb3d398f0d AS build
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew
COPY src ./src
RUN ./gradlew bootJar --no-daemon
FROM eclipse-temurin:17-jre@sha256:2a996a534890f1bee252fab23a0c01e773e9e1dc0e4700e8f0f6a2697c6cbf76
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
