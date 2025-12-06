# Bugshot - Real-time Error Monitoring System

## Final Report for Object Oriented Programming (Project 4)

---

## (a) Project Overview

### Project Title
**Bugshot**: Real-time JavaScript Error Monitoring and Session Replay System

### Team Information
| Role | Name | Student ID |
|------|------|------------|
| Team Leader | 김덕환 | 20200477 |
| Team Member | 정은재 | 20226495 |

**Team Name**: Bugshot

**Course**: Object Oriented Programming (Prof. Bong-Soo Sohn)

### Project Summary
Bugshot is a comprehensive error monitoring service that captures JavaScript errors from web applications in real-time. It provides:
- **Real-time Error Collection**: Captures errors via SDK integration
- **Intelligent Error Grouping**: Uses SHA-256 hashing to deduplicate similar errors
- **Priority-based Alert System**: Automatically calculates error priority and sends notifications
- **Session Replay**: Records user sessions for debugging
- **Multi-channel Notifications**: Supports Discord, Slack, Email, Kakao Work, Telegram, and Webhooks

---

## (b) System Requirements and Build Instructions

### System Requirements

| Component | Requirement |
|-----------|-------------|
| Java | JDK 21 or higher |
| Build Tool | Gradle 8.x (wrapper included) |
| Database | MySQL 8.0+ |
| Cache | Redis 6.0+ (optional) |
| OS | Windows 10+, macOS, Linux |
| Memory | Minimum 2GB RAM |
| Disk | Minimum 500MB free space |

### How to Compile

#### Method 1: Using Gradle Wrapper (Recommended)
```bash
# Windows
gradlew.bat build -x test

# Linux/Mac
./gradlew build -x test
```

#### Method 2: Direct Gradle
```bash
gradle build -x test
```

> **Note**: The `-x test` flag skips tests as they require MySQL connection.

### How to Execute

#### 1. Database Setup
Create a MySQL database and configure `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bugshot
    username: your_username
    password: your_password
```

#### 2. Run JAR File
```bash
java -jar build/libs/bugshot-0.0.1-SNAPSHOT.jar
```

Or with profile:
```bash
java -jar -Dspring.profiles.active=local build/libs/bugshot-0.0.1-SNAPSHOT.jar
```

#### 3. Verify Execution
- **Main Application**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **Health Check**: http://localhost:8081/actuator/health

---

## (c) Software Functionality

### Core Features

#### 1. Error Collection (Ingest API)
- Receives error data from JavaScript SDK
- Validates API key for project authentication
- Stores error information with context (URL, browser, OS, etc.)

#### 2. Error Deduplication
- Uses SHA-256 hash of (errorType + filePath + lineNumber)
- Groups identical errors together
- Tracks occurrence count and affected users

#### 3. Priority Calculation
Automatic priority scoring based on:
- **Page Weight**: Checkout pages (10x), Login pages (8x), Homepage (5x)
- **Occurrence Count**: How many times the error occurred
- **Affected Users**: Number of unique users impacted

```java
priority = occurrenceCount * affectedUsersCount * pageWeight
```

#### 4. Multi-channel Notifications
| Channel | Description |
|---------|-------------|
| Discord | Webhook + Bot integration |
| Slack | Webhook notifications |
| Email | SMTP-based alerts |
| Kakao Work | Korean messenger integration |
| Telegram | Bot API notifications |
| Webhook | Custom HTTP endpoints |

#### 5. Session Replay
- Records DOM mutations and user interactions
- Stores replay data in cloud storage (Cloudflare R2)
- Links replays to specific error occurrences

#### 6. Dashboard & Statistics
- Error trends over time
- Project-level statistics
- Redis caching for performance (10-minute TTL)

---

## (d) Implementation Details

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Bugshot Architecture                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────┐     ┌──────────────┐     ┌──────────────┐    │
│  │   SDK    │────▶│  Ingest API  │────▶│  Error       │    │
│  │ (Browser)│     │  Controller  │     │  Service     │    │
│  └──────────┘     └──────────────┘     └──────┬───────┘    │
│                                               │             │
│                            ┌──────────────────┼────────┐   │
│                            │    Event Publisher        │   │
│                            └──────────────────┬────────┘   │
│                                               │             │
│            ┌──────────────┬──────────────┬────┴───────┐    │
│            ▼              ▼              ▼            ▼    │
│    ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌────────┐│
│    │ Priority  │  │  Session  │  │Notification│  │ Stats  ││
│    │ Listener  │  │  Replay   │  │  Listener  │  │Listener││
│    └───────────┘  └───────────┘  └─────┬─────┘  └────────┘│
│                                        │                   │
│                              ┌─────────┴─────────┐        │
│                              │ Strategy Registry │        │
│                              └─────────┬─────────┘        │
│                   ┌─────────┬─────────┬┴────────┬────────┐│
│                   ▼         ▼         ▼         ▼        ▼│
│              ┌────────┐┌────────┐┌────────┐┌────────┐┌────┐│
│              │Discord ││ Slack  ││ Email  ││ Kakao  ││... ││
│              │Strategy││Strategy││Strategy││Strategy││    ││
│              └────────┘└────────┘└────────┘└────────┘└────┘│
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Key Implementation Issues

#### 1. Error Deduplication Algorithm
```java
public static String calculateErrorHash(String errorType, String filePath, Integer lineNumber) {
    String input = errorType + "|" + filePath + "|" + lineNumber;
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
    // Convert to hex string
    return hexString;
}
```

#### 2. Asynchronous Event Processing
Using Spring's `@Async` and `@EventListener` for non-blocking processing:
```java
@Async
@EventListener
public void handleErrorIngested(ErrorIngestedEvent event) {
    notificationService.notifyError(event.getProject(), event.getError(), event.getOccurrence());
}
```

#### 3. Strategy Pattern for Notifications
Each notification channel implements `NotificationStrategy` interface, allowing easy addition of new channels without modifying existing code.

---

## (e) UML Diagrams

### 1. Class Diagram

> **[INSERT CLASS DIAGRAM IMAGE HERE]**

#### Main Classes and Relationships:

```
┌─────────────────────────────────────────────────────────────────┐
│                        BaseEntity                                │
│  - createdAt: LocalDateTime                                      │
│  - updatedAt: LocalDateTime                                      │
└─────────────────────┬───────────────────────────────────────────┘
                      │ extends
        ┌─────────────┼─────────────┬─────────────┐
        ▼             ▼             ▼             ▼
┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────────┐
│   User    │  │  Project  │  │   Error   │  │NotificationCh.│
├───────────┤  ├───────────┤  ├───────────┤  ├───────────────┤
│-id        │  │-id        │  │-id        │  │-id            │
│-email     │  │-name      │  │-errorHash │  │-channelType   │
│-name      │  │-apiKey    │  │-errorType │  │-config        │
│-planType  │◀─┤-user      │◀─┤-project   │  │-minSeverity   │
│-projects  │──┤-errors    │──┤-occurrences│  │-enabled       │
└───────────┘  └───────────┘  └───────────┘  └───────────────┘
     │              │              │
     │              │              ▼
     │              │       ┌─────────────────┐
     │              │       │ ErrorOccurrence │
     │              │       ├─────────────────┤
     │              │       │-url             │
     │              │       │-userAgent       │
     │              │       │-sessionId       │
     │              │       └─────────────────┘
     │              │
     │              ▼
     │       ┌─────────────┐
     │       │SessionReplay│
     │       ├─────────────┤
     │       │-sessionId   │
     │       │-replayDataUrl│
     │       │-durationMs  │
     │       └─────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│                   <<interface>>                                  │
│                NotificationStrategy                              │
├─────────────────────────────────────────────────────────────────┤
│ + getChannelType(): ChannelType                                 │
│ + send(channel, project, error, occurrence): void               │
│ + sendTest(channel): void                                       │
└─────────────────────────────────────────────────────────────────┘
         △                    △                    △
         │                    │                    │
         │ implements         │                    │
┌────────┴───────┐  ┌────────┴───────┐  ┌────────┴───────┐
│DiscordStrategy │  │ SlackStrategy  │  │ EmailStrategy  │
└────────────────┘  └────────────────┘  └────────────────┘
```

### 2. Use-Case Diagram

> **[INSERT USE-CASE DIAGRAM IMAGE HERE]**

#### Actors:
- **Developer**: Creates projects, views errors, manages settings
- **SDK (Client App)**: Sends error data, uploads session replays
- **System**: Calculates priority, sends notifications

#### Use Cases:

```
┌─────────────────────────────────────────────────────────────────┐
│                      Bugshot System                              │
│                                                                  │
│  ┌──────────────────┐                                           │
│  │ Create Project   │◀────────────────┐                        │
│  └──────────────────┘                  │                        │
│                                        │                        │
│  ┌──────────────────┐                  │                        │
│  │ View Error List  │◀────────────────┤                        │
│  └──────────────────┘                  │        ┌─────────┐    │
│                                        ├────────│Developer│    │
│  ┌──────────────────┐                  │        └─────────┘    │
│  │ View Error Detail│◀────────────────┤                        │
│  └──────────────────┘                  │                        │
│                                        │                        │
│  ┌──────────────────┐                  │                        │
│  │ Resolve/Ignore   │◀────────────────┤                        │
│  │     Error        │                  │                        │
│  └──────────────────┘                  │                        │
│                                        │                        │
│  ┌──────────────────┐                  │                        │
│  │ Configure        │◀────────────────┘                        │
│  │ Notifications    │                                           │
│  └──────────────────┘                                           │
│                                                                  │
│  ┌──────────────────┐         ┌───────┐                        │
│  │ Send Error Data  │◀────────│  SDK  │                        │
│  └────────┬─────────┘         └───────┘                        │
│           │                                                     │
│           ▼ <<include>>                                         │
│  ┌──────────────────┐                                           │
│  │ Group Errors     │                                           │
│  └────────┬─────────┘                                           │
│           │                                                     │
│           ▼ <<include>>           ┌────────┐                   │
│  ┌──────────────────┐             │ System │                   │
│  │Calculate Priority│◀────────────└────────┘                   │
│  └────────┬─────────┘                                           │
│           │                                                     │
│           ▼ <<include>>                                         │
│  ┌──────────────────┐                                           │
│  │Send Notification │                                           │
│  └──────────────────┘                                           │
│                                                                  │
│  ┌──────────────────┐         ┌───────┐                        │
│  │Upload Session    │◀────────│  SDK  │                        │
│  │    Replay        │         └───────┘                        │
│  └──────────────────┘                                           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3. Activity Diagram

> **[INSERT ACTIVITY DIAGRAM IMAGE HERE]**

#### Error Ingest Process:

```
    ┌─────────┐
    │  Start  │
    └────┬────┘
         │
         ▼
┌─────────────────┐
│ Receive Error   │
│   from SDK      │
└────────┬────────┘
         │
         ▼
    ┌─────────┐
    │Validate │
    │ API Key │
    └────┬────┘
         │
    ┌────┴────┐
    │ Valid?  │
    └────┬────┘
    No   │   Yes
    │    │    │
    ▼    │    ▼
┌──────┐ │ ┌─────────────────┐
│Return│ │ │Calculate Error  │
│ 401  │ │ │     Hash        │
└──────┘ │ └────────┬────────┘
         │          │
         │          ▼
         │    ┌──────────┐
         │    │ Existing │
         │    │  Error?  │
         │    └────┬─────┘
         │    No   │   Yes
         │    │    │    │
         │    ▼    │    ▼
         │ ┌──────┐│ ┌──────────────┐
         │ │Create││ │  Increment   │
         │ │ New  ││ │  Occurrence  │
         │ │Error ││ │    Count     │
         │ └──┬───┘│ └──────┬───────┘
         │    │    │        │
         │    └────┼────────┘
         │         │
         │         ▼
         │  ┌──────────────┐
         │  │Create Error  │
         │  │  Occurrence  │
         │  └──────┬───────┘
         │         │
         │         ▼
         │  ┌──────────────┐
         │  │Publish Event │
         │  │(ErrorIngested)│
         │  └──────┬───────┘
         │         │
         │    ┌────┴────┬────────────┬────────────┐
         │    ▼         ▼            ▼            ▼
         │ ┌──────┐ ┌────────┐ ┌──────────┐ ┌────────┐
         │ │Calc  │ │ Save   │ │  Send    │ │Update  │
         │ │Prior.│ │Replay  │ │Notificat.│ │ Stats  │
         │ └──────┘ └────────┘ └──────────┘ └────────┘
         │    │         │            │            │
         │    └─────────┴────────────┴────────────┘
         │                    │
         │                    ▼
         │             ┌──────────┐
         │             │  Return  │
         │             │ Success  │
         │             └────┬─────┘
         │                  │
         └──────────────────┤
                            ▼
                       ┌─────────┐
                       │   End   │
                       └─────────┘
```

---

## (f) Execution Results

### Screen Captures

> **[INSERT SCREENSHOTS HERE]**

#### 1. Swagger UI - API Documentation
- Screenshot showing available API endpoints

#### 2. Error Ingest API Test
- Screenshot of POST /api/v1/ingest request and response

#### 3. Error List API
- Screenshot of GET /api/v1/errors response

#### 4. Error Detail with Session Replay
- Screenshot showing error detail with replay link

#### 5. Notification Delivery
- Screenshot of Discord/Slack notification received

---

## (g) Object-Oriented Concepts Applied

### 1. Encapsulation (캡슐화)

**Implementation**: Each entity class encapsulates its data with private fields and exposes behavior through public methods.

```java
// Error.java - Business logic encapsulated in entity
public class Error {
    private Integer occurrenceCount = 1;
    private BigDecimal priorityScore;

    public void incrementOccurrence() {
        this.occurrenceCount++;
        this.lastSeenAt = LocalDateTime.now();
    }

    public void calculatePriority(String url) {
        double pageWeight = determinePageWeight(url);
        this.priorityScore = BigDecimal.valueOf(
            occurrenceCount * affectedUsersCount * pageWeight
        );
    }
}
```

### 2. Inheritance (상속)

**Implementation**: `BaseEntity` provides common audit fields to all entities.

```java
// BaseEntity.java - Parent class
@MappedSuperclass
public abstract class BaseEntity {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// User.java - Child class
public class User extends BaseEntity {
    // Inherits createdAt, updatedAt
    private String email;
    private String name;
}
```

**Benefit**: Code reuse - all entities automatically have creation/modification timestamps.

### 3. Polymorphism (다형성)

**Implementation**: `NotificationStrategy` interface with multiple implementations.

```java
// Interface
public interface NotificationStrategy {
    ChannelType getChannelType();
    void send(NotificationChannel channel, Project project,
              Error error, ErrorOccurrence occurrence);
}

// Implementations
public class DiscordNotificationStrategy implements NotificationStrategy {
    @Override
    public void send(...) { /* Discord-specific logic */ }
}

public class SlackNotificationStrategy implements NotificationStrategy {
    @Override
    public void send(...) { /* Slack-specific logic */ }
}

public class EmailNotificationStrategy implements NotificationStrategy {
    @Override
    public void send(...) { /* Email-specific logic */ }
}
```

**Usage**:
```java
// Same interface, different behavior
NotificationStrategy strategy = registry.getStrategy(channelType);
strategy.send(channel, project, error, occurrence);
```

### 4. Abstraction (추상화)

**Implementation**: Complex notification logic abstracted behind simple interface.

```java
// Client code doesn't know implementation details
notificationService.notifyError(project, error, occurrence);

// NotificationService handles all complexity internally
// - Finding enabled channels
// - Filtering by severity
// - Selecting appropriate strategy
// - Error handling
```

### 5. Design Patterns Applied

#### Strategy Pattern (전략 패턴)
- **Purpose**: Encapsulate notification algorithms
- **Classes**: `NotificationStrategy`, `DiscordNotificationStrategy`, `SlackNotificationStrategy`, etc.
- **Benefit**: Easy to add new notification channels

#### Observer Pattern (옵저버 패턴)
- **Purpose**: Decouple error processing from side effects
- **Classes**: `ErrorIngestedEvent`, `NotificationListener`, `PriorityCalculationListener`, `SessionReplayListener`
- **Benefit**: Asynchronous processing, loose coupling

#### Builder Pattern (빌더 패턴)
- **Purpose**: Create complex objects step by step
- **Implementation**: Lombok `@Builder` annotation
- **Benefit**: Readable object construction

```java
Error error = Error.builder()
    .project(project)
    .errorType("TypeError")
    .errorMessage("Cannot read property")
    .filePath("/app/index.js")
    .lineNumber(42)
    .build();
```

#### Repository Pattern (리포지토리 패턴)
- **Purpose**: Abstract data access layer
- **Classes**: `ErrorRepository`, `ProjectRepository`, `UserRepository`
- **Benefit**: Separation of concerns

---

## (h) Conclusion

### Project Summary
Bugshot successfully demonstrates the application of Object-Oriented Programming concepts in building a real-world error monitoring system. The project showcases:

1. **Practical OOP Application**: Encapsulation, inheritance, polymorphism, and abstraction are effectively used throughout the codebase.

2. **Design Pattern Usage**: Strategy, Observer, Builder, and Repository patterns provide flexible, maintainable architecture.

3. **Real-world Problem Solving**: The system addresses actual developer needs for error monitoring and debugging.

### Technical Achievements
- SHA-256 based error deduplication
- Event-driven architecture for loose coupling
- Strategy pattern for extensible notification system
- RESTful API design with Swagger documentation

### Lessons Learned
- Importance of proper abstraction for maintainability
- Value of design patterns in solving common problems
- Benefits of event-driven architecture for scalability

### Future Improvements
- Add more notification channels (Microsoft Teams, SMS)
- Implement machine learning for error clustering
- Add user authentication improvements
- Performance optimization for large-scale deployments

---

## Appendix

### A. Project Structure
```
backend/
├── src/main/java/com/bugshot/
│   ├── BugshotApplication.java
│   ├── domain/
│   │   ├── auth/           # User authentication (OAuth)
│   │   ├── project/        # Project management
│   │   ├── error/          # Error handling core
│   │   ├── notification/   # Multi-channel notifications
│   │   ├── replay/         # Session replay
│   │   ├── webhook/        # Webhook configuration
│   │   └── dashboard/      # Statistics
│   └── global/
│       ├── config/         # Spring configurations
│       ├── security/       # Security filters
│       └── exception/      # Exception handling
└── src/main/resources/
    └── application.yml
```

### B. API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/ingest | Receive error from SDK |
| GET | /api/v1/errors | List errors for project |
| GET | /api/v1/errors/{id} | Get error detail |
| POST | /api/v1/errors/{id}/resolve | Mark as resolved |
| POST | /api/v1/errors/{id}/ignore | Mark as ignored |
| GET | /api/v1/projects | List user's projects |
| POST | /api/v1/projects | Create new project |

### C. Technology Stack
| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Database | MySQL 8.0, Redis |
| ORM | Spring Data JPA |
| Build | Gradle 8.x |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Async | Spring @Async |

---

**End of Report**
