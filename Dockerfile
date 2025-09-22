# Стадия 1: Сборка JAR-файла
FROM gradle:8.4-jdk21 AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы конфигурации Gradle и исходники
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src

# Собираем JAR-файл
RUN gradle clean build -x test --no-daemon

# Стадия 2: Запуск JAR
FROM openjdk:21-jdk-slim

WORKDIR /app

# Копируем JAR-файл из стадии сборки
COPY --from=builder /app/build/libs/*.jar app.jar

# Указываем активный профиль
ENV SPRING_PROFILES_ACTIVE=LOCAL

# Открываем порт
EXPOSE 8080

# Запускаем приложение
CMD ["java", "-jar", "app.jar"]
