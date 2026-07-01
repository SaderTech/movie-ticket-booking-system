FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Build riêng notification-service
RUN mvn -pl services/notification-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/services/notification-service/target/*.jar app.jar

# Sửa lại port nếu application.yml của Notification Service không phải 8086
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]