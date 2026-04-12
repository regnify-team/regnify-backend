# Use lightweight Java runtime
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy jar file
COPY target/*.jar app.jar

# Expose port (Provider will override with PORT env)
EXPOSE 8080

# Run application
ENTRYPOINT ["sh", "-c", "java -Xmx256m -Xms128m -jar app.jar --server.port=${PORT}"]