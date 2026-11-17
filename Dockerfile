FROM eclipse-temurin:21-jdk-jammy AS compilacion

WORKDIR /workspace
COPY gradle gradle
COPY gradlew gradlew
COPY settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew

COPY src src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre-jammy AS ejecucion

RUN useradd --system --create-home --uid 1001 mrcatfood
WORKDIR /app
COPY --from=compilacion /workspace/build/libs/MrCatFood-0.0.1-SNAPSHOT.jar app.jar

USER mrcatfood
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
