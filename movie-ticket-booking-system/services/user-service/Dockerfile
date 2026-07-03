FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Build riêng user-service
RUN mvn -pl services/user-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/services/user-service/target/*.jar app.jar

# Sửa lại port nếu application.yml của User Service không phải 8081
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]