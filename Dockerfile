# ===================================================================
# ETAPA 1: Dependencias (Caching de Maven)
# ===================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS deps
WORKDIR /app
# Copiamos solo el pom para descargar dependencias y aprovechar el cache de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# ===================================================================
# ETAPA 2: Compilación (Build)
# ===================================================================
FROM deps AS build
WORKDIR /app
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===================================================================
# ETAPA 3: Ejecución (Hardened Runtime)
# ===================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# [FIX] Forzamos el usuario root para realizar tareas administrativas
USER root

# Creamos el grupo y usuario para no correr la app como root (Seguridad)
RUN addgroup -S spring && adduser -S spring -G spring

# Copiamos el JAR desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# [IMPORTANTE] Cambiamos la propiedad del JAR al usuario spring
RUN chown spring:spring app.jar

# Cambiamos al usuario no-root para la ejecución final
USER spring

# Configuración de variables de entorno por defecto
ENV PORT=8080

# Optimizaciones de JVM para contenedores y puerto dinámico para Render
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Dserver.port=${PORT}", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]