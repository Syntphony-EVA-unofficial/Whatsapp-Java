# Use Maven with Java 17 to build the artifact
FROM maven:3.8.6-eclipse-temurin-17 AS build-env

# Set the working directory for the build
WORKDIR /app

# Copy the pom.xml to download dependencies
COPY pom.xml ./

# Download all dependencies for offline use. This leverages Docker cache.
RUN mvn dependency:go-offline

# Copy the source code to the container
COPY src ./src

# Package the application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Use a lightweight OpenJDK 17 runtime image for production
FROM eclipse-temurin:17-jre

# Set the working directory for the production container
WORKDIR /app

# Copy the packaged JAR file from the build container
COPY --from=build-env /app/target/whatsapp-connector-*.jar /app/whatsapp-connector.jar

# Expose port 8080 (adjust if your app uses a different port)
EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-jar", "/app/whatsapp-connector.jar"]