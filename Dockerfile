FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon --quiet dependencies > /dev/null
COPY src ./src
ARG APP_VERSION=1.0
RUN ./gradlew --no-daemon bootJar -x test -PappVersion=${APP_VERSION}

FROM eclipse-temurin:17-jre
ARG APP_VERSION=1.0
LABEL org.opencontainers.image.title="brumkul-ramazan-student-api" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.authors="Ramazan Brumkul" \
      org.opencontainers.image.source="https://github.com/Lebou-git/brumkul-ramazan-student-api" \
      student.name="Ramazan Brumkul" \
      student.group="IT2-2301" \
      student.id="37911"
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring --no-create-home --shell /usr/sbin/nologin spring
COPY --from=build /workspace/build/libs/brumkul-ramazan-student-api.jar app.jar
USER spring
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
