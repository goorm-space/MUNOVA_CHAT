# Amazon Corretto 21 JRE 사용
FROM amazoncorretto:21-alpine

LABEL maintainer="namoo36"
LABEL version="1.0"

# 빌드된 Spring Boot JAR 복사
COPY build/libs/MUNOVA-CHAT-0.0.1-SNAPSHOT.jar /app/MUNOVA-CHAT.jar
WORKDIR /app

# JVM 시간대
ENV TZ=Asia/Seoul

# 애플리케이션 포트
EXPOSE 8080
EXPOSE 7070

# 실행 명령
CMD ["java", "-jar", "/app/MUNOVA-CHAT.jar"]