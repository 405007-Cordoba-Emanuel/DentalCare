FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/*.jar app.jar
# Los tres usan lo mismo, solo cambia el path del jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]