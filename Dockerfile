FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 🚀 Runtime Stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/extractor-0.0.1-SNAPSHOT.jar app.jar
# Create a Downloads folder (this path will be mounted to host)
RUN mkdir -p /app/Downloads

# Set environment variable for the app (optional)
ENV DOWNLOAD_DIR=/app/Downloads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]