FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Build riêng showtime-service
RUN mvn -pl services/showtime-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/services/showtime-service/target/*.jar app.jar

# Sửa lại port nếu application.yml của Showtime Service không phải 8084
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]