FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Build riêng cinema-service
RUN mvn -pl services/cinema-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/services/cinema-service/target/*.jar app.jar

# Sửa lại port nếu application.yml của Cinema Service không phải 8083
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]