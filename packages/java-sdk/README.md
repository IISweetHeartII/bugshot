# Bugshot Java SDK

Java 애플리케이션의 에러를 실시간으로 모니터링하고 Discord, Slack, 카카오톡으로 알림을 받으세요.

[![](https://jitpack.io/v/YOUR_GITHUB_USERNAME/bugshot-java-sdk.svg)](https://jitpack.io/#YOUR_GITHUB_USERNAME/bugshot-java-sdk)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 설치

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.YOUR_GITHUB_USERNAME:bugshot-java-sdk:1.0.0")
}
```

### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.YOUR_GITHUB_USERNAME:bugshot-java-sdk:1.0.0'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.YOUR_GITHUB_USERNAME</groupId>
    <artifactId>bugshot-java-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 빠른 시작

### 1. SDK 초기화

```java
import com.bugshot.Bugshot;
import com.bugshot.BugshotConfig;

public class Application {
    public static void main(String[] args) {
        // SDK 초기화
        Bugshot.init(new BugshotConfig.Builder("your-api-key")
            .endpoint("https://your-bugshot-server.com")
            .environment("production")
            .release("1.0.0")
            .debug(true)  // 개발 중에만 true
            .build());

        // 애플리케이션 실행
        runApplication();
    }
}
```

### 2. 수동 에러 캡처

```java
try {
    riskyOperation();
} catch (Exception e) {
    Bugshot.captureException(e);
}
```

### 3. 추가 컨텍스트와 함께 캡처

```java
try {
    processOrder(orderId);
} catch (Exception e) {
    Map<String, Object> context = new HashMap<>();
    context.put("orderId", orderId);
    context.put("userId", userId);
    context.put("action", "processOrder");

    Bugshot.captureException(e, context);
}
```

### 4. 사용자 정보 설정

```java
// 로그인 후 사용자 정보 설정
Bugshot.setUser("user-123", "user@example.com", "John Doe");

// 로그아웃 시 초기화
Bugshot.clearUser();
```

### 5. 전역 컨텍스트 설정

```java
// 모든 에러에 포함될 컨텍스트
Bugshot.setContext("serverRegion", "ap-northeast-2");
Bugshot.setContext("deploymentId", "deploy-abc123");
```

## Spring Boot 연동

### 전역 예외 핸들러

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        // Bugshot로 에러 캡처
        Map<String, Object> context = new HashMap<>();
        context.put("url", request.getRequestURI());
        context.put("method", request.getMethod());
        context.put("userAgent", request.getHeader("User-Agent"));

        Bugshot.captureException(e, context);

        // 클라이언트에게 응답
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("Internal Server Error"));
    }
}
```

### 애플리케이션 시작 시 초기화

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // Bugshot 초기화
        Bugshot.init(new BugshotConfig.Builder(System.getenv("BUGSHOT_API_KEY"))
            .endpoint(System.getenv("BUGSHOT_ENDPOINT"))
            .environment(System.getenv("SPRING_PROFILES_ACTIVE"))
            .build());

        SpringApplication.run(Application.class, args);
    }
}
```

## 설정 옵션

| 옵션 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `apiKey` | String | (필수) | API 키 |
| `endpoint` | String | `http://localhost:8081` | 서버 엔드포인트 |
| `environment` | String | `production` | 환경 (production, staging, development) |
| `release` | String | null | 릴리스 버전 |
| `debug` | boolean | false | 디버그 로그 출력 |
| `enableAutoCapture` | boolean | true | 미처리 예외 자동 캡처 |
| `sampleRate` | double | 1.0 | 샘플링 비율 (0.0 ~ 1.0) |

## 기능

- **자동 에러 캡처**: `Thread.UncaughtExceptionHandler`를 통한 미처리 예외 자동 캡처
- **수동 에러 캡처**: `try-catch`에서 명시적으로 에러 전송
- **컨텍스트 추가**: 사용자 정보, 커스텀 데이터 첨부
- **샘플링**: 트래픽 조절을 위한 샘플링 기능
- **비동기 전송**: 애플리케이션 성능에 영향 없이 백그라운드 전송
- **Java 11+ 지원**: 외부 의존성 없이 내장 HttpClient 사용

## 요구사항

- Java 11 이상
- Bugshot 서버 (self-hosted)

## 라이선스

MIT License - 자유롭게 사용, 수정, 배포할 수 있습니다.

## 기여

이슈와 PR을 환영합니다!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
