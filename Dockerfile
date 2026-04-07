# ETAPA 1: Dependencias (Caching)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS deps
WORKDIR /app
# Copiamos solo el pom para descargar dependencias primero
COPY pom.xml .
RUN mvn dependency:go-offline -B

# ETAPA 2: Compilación
FROM deps AS build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ETAPA 3: Ejecución (Hardened Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Seguridad: No corremos como root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copiamos solo el JAR necesario
COPY --from=build /app/target/*.jar app.jar

# Optimización de JVM para contenedores
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]