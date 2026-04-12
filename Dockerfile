# Use lightweight Java runtime
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy jar file
COPY target/*.jar app.jar

# Expose port (Provider will override with PORT env)
EXPOSE 8080

# Run application
ENTRYPOINT ["sh", "-c", "java -Xmx256m -Xms128m -jar app.jar --server.port=${PORT}"]