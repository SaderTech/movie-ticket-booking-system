# Bước 1: Build mã nguồn bằng Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy toàn bộ source code vào container
COPY . .

# Build riêng module api-gateway và các module phụ thuộc (-am)
RUN mvn -pl infra/api-gateway -am clean package -DskipTests

# Bước 2: Chạy ứng dụng bằng JRE siêu nhẹ
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Lấy file .jar từ Bước 1 sang Bước 2
COPY --from=build /app/infra/api-gateway/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]