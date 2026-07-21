# Build stage: Use Maven with JDK 21 on Alpine Linux as the compilation environment
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

# Set the working directory inside the container for compilation
WORKDIR /app

# Copy the application source code and Maven wrapper files
COPY . .

# Run Maven to build the executable JAR file, skipping tests to speed up the process
RUN mvn clean package -DskipTests

# Runtime stage: Use a lightweight JRE 21 on Alpine Linux to execute the application
FROM eclipse-temurin:21-jre-alpine

# Set the working directory for the runtime environment
WORKDIR /app

# Copy the compiled JAR artifact from the build stage into the runtime environment
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 which matches the default application web server port
EXPOSE 8080

# Define the entrypoint command to launch the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]