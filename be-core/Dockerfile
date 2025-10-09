FROM eclipse-temurin:17-jdk-alpine

# Instalar curl para health checks
RUN apk add --no-cache curl

WORKDIR /app

COPY target/*.jar app.jar
# Los tres usan lo mismo, solo cambia el path del jar
EXPOSE 8082

ENTRYPOINT ["java","-jar","app.jar"]