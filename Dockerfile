# Render Web Service (Docker). Set env in Render dashboard:
#   SPRING_PROFILES_ACTIVE=prod (default in image)
#   MSE_JWT_SECRET=<random 32+ byte secret>
#   MSE_CORS_ORIGINS=https://your-game-client.example.com
# Optional persistent disk mounted at /data for H2 file DB across restarts.

FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /app

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:25-jre-jammy AS runtime
WORKDIR /app

RUN useradd -r -u 10001 appuser \
	&& mkdir -p /data \
	&& chown -R appuser:appuser /data /app

COPY --from=build --chown=appuser:appuser /app/target/mseserver-*.jar app.jar

USER appuser

ENV SPRING_PROFILES_ACTIVE=prod
ENV DATABASE_URL=jdbc:h2:file:/data/msedb;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE
ENV JAVA_TOOL_OPTIONS=-XX:+UseContainerSupport

# Render injects PORT; application-prod uses server.port=${PORT:9090}
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
