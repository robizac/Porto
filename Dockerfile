# ---- build stage ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
# Cache de dependencias
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
# Compila e empacota (testes rodam no CI; aqui pulamos para build rapido de imagem)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
