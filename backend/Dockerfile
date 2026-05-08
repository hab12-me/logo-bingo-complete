# backend/Dockerfile - COMPLETE VERSION
# Stage 1: Build the JAR file
FROM maven:3.8-openjdk-11 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src src
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR file
FROM openjdk:11-jre-slim

WORKDIR /app

# Copy the built JAR from Stage 1
COPY --from=build /app/target/logo-bing-backend-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
