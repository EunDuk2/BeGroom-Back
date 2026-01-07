# 1단계: 빌드 스테이지
FROM eclipse-temurin:25-jdk AS stage1
WORKDIR /app

# 빌드 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# 빌드 실행
RUN chmod +x gradlew
RUN ./gradlew bootJar -x test

# 2단계: 실행 스테이지 (경량화 이미지)
# jre 버전이 있다면 더 가볍겠지만, 현재 25 버전은 jdk 이미지를 주로 사용합니다.
FROM eclipse-temurin:25-jdk
WORKDIR /app

# 빌드 결과물만 복사
COPY --from=stage1 /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]