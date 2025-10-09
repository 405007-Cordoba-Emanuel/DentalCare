# ============================================
# Stage 1: Build con Maven
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copiar archivos de configuración de Maven primero (para cache)
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# ============================================
# Stage 2: Runtime (imagen final ligera)
# ============================================
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copiar solo el JAR desde la etapa de build
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]