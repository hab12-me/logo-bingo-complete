# backend/Dockerfile - Working Version
# Stage 1: Build the JAR file using Maven with Amazon Corretto 17
FROM amazoncorretto:17-alpine3.19 AS build

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src src
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR file
FROM amazoncorretto:17-alpine3.19

WORKDIR /app

# Copy the built JAR from Stage 1
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
