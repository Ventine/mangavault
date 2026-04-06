# ETAPA 1: Compilación (Build)
# Usamos una imagen de Maven con Java 21 para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copiamos el pom y el código fuente
COPY pom.xml .
COPY src ./src
# Compilamos saltando los tests para ir más rápido
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Runtime)
# Usamos una imagen ligera de Java para correr el JAR
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copiamos el archivo JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar
# Exponemos el puerto de la API
EXPOSE 8080
# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]