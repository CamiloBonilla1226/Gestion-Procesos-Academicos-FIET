## Execute application
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# Ruta donde se guardarán los archivos
ENV UPLOADS_PATH=/app/uploads

# Crear carpeta de uploads
RUN mkdir -p /app/uploads

# Copiar el jar
ARG JAR_FILE=solicitudes/target/solicitudes-0.0.1.jar
COPY ${JAR_FILE} app_cfiet.jar

# Puerto de la app
EXPOSE 8080

# Ejecutar la app
ENTRYPOINT ["java", "-jar", "app_cfiet.jar"]
