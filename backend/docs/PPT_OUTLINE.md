# Bugshot Presentation Outline

발표 시간: **약 7분**
- What (시스템 설명): ~2분
- How (설계/구현): ~1분
- Demo: ~4분

---

## Slide 1: Title Slide (15초)

### Content
```
Bugshot
Real-time Error Monitoring System

Team: Bugshot
Members:
- 김덕환 (20200477)
- 정은재 (20226495)

Object Oriented Programming
Prof. Bong-Soo Sohn
December 2025
```

### Speaker Notes
"Good morning/afternoon everyone. We are Team Bugshot. I am [Name 1] and this is [Name 2]. Today, we will present Bugshot - a real-time error monitoring system that we developed using object-oriented programming principles."

---

## Slide 2: Problem Statement (30초)

### Content
```
The Problem

❌ JavaScript errors occur silently in production
❌ Users don't report errors - they just leave
❌ Developers only know about errors after user complaints
❌ Hard to reproduce errors without context

"In 2024, 88% of users abandon websites after a bad experience"
```

### Speaker Notes
"Let us start with the problem we are solving. JavaScript errors in web applications often occur silently in production. When users encounter bugs, they do not report them - they simply leave the website. According to recent studies, 88% of users abandon websites after a bad experience. By the time developers discover these issues through user complaints, they have already lost valuable customers. This is where Bugshot comes in."

---

## Slide 3: Our Solution (45초)

### Content
```
Bugshot - The Solution

✅ Real-time Error Capture
   - Automatic error detection via SDK

✅ Intelligent Error Grouping
   - SHA-256 based deduplication

✅ Priority-based Alerts
   - Automatic severity calculation
   - Multi-channel notifications

✅ Session Replay
   - See exactly what users experienced

[Architecture Diagram Here]
```

### Speaker Notes
"Bugshot solves these problems. Our SDK automatically captures errors and sends them to our backend. We use SHA-256 hashing to group similar errors together. The system calculates priority based on page importance and sends alerts through Discord, Slack, Email, and more. Session replay shows exactly what happened before the error."

---

## Slide 4: Full Stack Architecture (30초)

### Content
```
Full Stack System

┌─────────────────────────────────────────────────┐
│                  Bugshot System                  │
├─────────────────────────────────────────────────┤
│                                                  │
│  [Browser SDK]  ──▶  [Backend API]              │
│  TypeScript          Spring Boot 3.5            │
│                          │                      │
│                          ▼                      │
│                    [MySQL + Redis]              │
│                          │                      │
│  [Frontend]  ◀──────────┘                      │
│  Next.js 16                                     │
│  React 19                                       │
│                                                  │
└─────────────────────────────────────────────────┘

Technologies:
• Backend: Java 21, Spring Boot 3.5
• Frontend: Next.js 16, React 19, TypeScript
• Database: MySQL 8.0, Redis 6.0
• Storage: Cloudflare R2

Live: bugshot.log8.kr | API: bugshot-api.log8.kr
```

### Speaker Notes
"Here is our system architecture. On the client side, we have our Browser SDK written in TypeScript. It captures errors and sends them to our Spring Boot backend. The backend is built with Java 21 and Spring Boot 3.5. It processes errors, stores them in MySQL, and uses Redis for caching. Our frontend dashboard is built with Next.js 16 and React 19. The entire system is deployed in production - you can access it live at bugshot.log8.kr."

---

## Slide 5: OOP Concepts Applied (45초)

### Content
```
Object-Oriented Design

1. Encapsulation
   - Business logic inside entity classes
   - Example: Error.calculatePriority()

2. Inheritance
   - BaseEntity → User, Project, Error
   - Shared audit fields (createdAt, updatedAt)

3. Polymorphism
   - NotificationStrategy interface
   - Different implementations for each channel

4. Design Patterns
   - Strategy Pattern: Notification channels
   - Observer Pattern: Event-based processing
   - Builder Pattern: Entity construction
```

### Speaker Notes
"Now, let us discuss the OBJECT-ORIENTED CONCEPTS we applied. First, ENCAPSULATION. Business logic is encapsulated inside entity classes. For example, the Error entity has a calculatePriority method that determines severity based on internal state. Second, INHERITANCE. All our entities extend a BaseEntity class, which provides common audit fields like createdAt and updatedAt. This follows the DRY principle. Third, POLYMORPHISM. We defined a NotificationStrategy interface, and each notification channel implements this interface with its own send method. We also applied several DESIGN PATTERNS: The Strategy Pattern for notifications, The Observer Pattern for event-based processing, and The Builder Pattern for entity construction."

---

## Slide 6: Key Implementation - Strategy Pattern (30초)

### Content
```
Strategy Pattern for Notifications

<<interface>>
NotificationStrategy
+ getChannelType()
+ send(channel, project, error, occurrence)

    ↑ implements
    |
┌───┴───┬───────┬───────┬───────┐
Discord  Slack  Email  Kakao  Telegram
Strategy Strategy Strategy Strategy Strategy

Benefits:
✅ Easy to add new channels
✅ Open for extension, closed for modification
✅ Single responsibility per strategy
```

### Speaker Notes
"Let me explain the STRATEGY PATTERN in more detail, as it is the core of our notification system. We have a NotificationStrategy interface with two methods: getChannelType and send. Each notification channel implements this interface. The key benefit is EXTENSIBILITY. When we need to add a new channel like Microsoft Teams, we simply create a new strategy class. No existing code needs to be modified. This follows the Open-Closed Principle: Open for extension, closed for modification."

---

## Slide 7: UML Diagrams (40초)

### Content
```
Class Diagram

![Class Diagram](class-diagram.svg)

**Use Case Diagram:**
![Use Case Diagram](use-case.png)

**Activity Diagrams:**
![Activity - Error Ingest](activity-error-ingest.png)
![Activity - Notification](activity-notification.png)

Key Classes:
- User (OAuth, PlanType)
- Project (API Key management)
- Error (Hash-based grouping)
- ErrorOccurrence (Context data)
- NotificationChannel (Multi-channel support)
```

### Speaker Notes
"Here are our UML diagrams. The CLASS DIAGRAM shows our main entities and their relationships. User owns Projects, Projects contain Errors, and each Error has multiple ErrorOccurrences. Notice how all entities inherit from BaseEntity. The USE CASE DIAGRAM shows the main actors and their interactions. The ACTIVITY DIAGRAMS show two key workflows: Error ingestion and Notification processing. These diagrams were essential for designing our system before writing any code."

---

## Slide 8: Demo Introduction (15초)

### Content
```
Live Demo

1. Start the application
2. Send error via API
3. See error in list
4. Check notification
5. View session replay
```

### Speaker Notes
"Now let's see Bugshot in action. I'll demonstrate the main features."

---

## Slide 9-13: Demo Slides (4분)

### Demo Flow

#### Demo Part 1: Swagger UI (45초)
```
Show: http://localhost:8081/swagger-ui.html

"This is our API documentation.
All endpoints are documented with Swagger."
```

#### Demo Part 2: Send Error (60초)
```
POST /api/v1/ingest

Request Body:
{
  "apiKey": "sk_live_xxx",
  "error": {
    "type": "TypeError",
    "message": "Cannot read property 'name' of undefined",
    "file": "/checkout/payment.js",
    "line": 42
  },
  "context": {
    "url": "/checkout",
    "browser": "Chrome 120",
    "os": "Windows 11"
  }
}

"Notice how the priority is calculated automatically
because this error occurred on the checkout page."
```

#### Demo Part 3: Error List (45초)
```
GET /api/v1/errors?projectId=xxx

Show:
- Error grouping (same errors grouped together)
- Priority scores
- Occurrence counts
- Severity levels
```

#### Demo Part 4: Notification (45초)
```
Show Discord/Slack notification received

"The system automatically sent this notification
because the error severity was CRITICAL
(checkout page errors are always critical)."
```

#### Demo Part 5: Additional Features (45초)
```
- Resolve error: POST /api/v1/errors/{id}/resolve
- Ignore error: POST /api/v1/errors/{id}/ignore
- Dashboard statistics (if time permits)
```

---

## Slide 14: Conclusion (30초)

### Content
```
Summary

✅ Real-time error monitoring with SDK integration
✅ Intelligent error grouping and priority calculation
✅ Multi-channel notifications (Discord, Slack, Email, etc.)
✅ Session replay for debugging

OOP Concepts Applied:
- Encapsulation, Inheritance, Polymorphism
- Strategy, Observer, Builder Patterns

Technology Stack:
Java 21, Spring Boot 3.5, MySQL, Redis
```

### Speaker Notes
"To summarize, Bugshot provides: Real-time error monitoring with SDK integration, Intelligent error grouping using SHA-256 hashing, Priority-based alerting through multiple channels, And session replay for debugging. We successfully applied core OOP concepts: Encapsulation, Inheritance, and Polymorphism. We also used Strategy, Observer, and Builder patterns. This project gave us hands-on experience with full-stack development and object-oriented design. Thank you for your attention."

---

## Slide 15: Q&A

### Content
```
Questions?

🔗 Links:
• GitHub: github.com/IISweetHeartII/bugshot
• Frontend: bugshot.log8.kr
• API Docs: bugshot-api.log8.kr/swagger-ui

Thank you!
```

---

## 발표 팁

### 영어로 발표해야 함!
- 스크립트를 미리 작성하고 연습
- 기술 용어는 영어로 그대로 사용 (Strategy Pattern, Observer Pattern 등)

### 시간 배분
| Section | Time |
|---------|------|
| What (Problem & Solution) | 1-3 | ~1.5분 |
| How (Architecture, OOP, UML) | 4-7 | ~2분 |
| Demo Introduction | 8 | ~15s |
| Demo | 9-13 | ~4 |
| Conclusion & Q&A | 14-15 | ~30s |
| **Total** | **15 slides** | **~7분 |

### 데모 준비
1. 미리 Docker로 MySQL, Redis 실행
2. 백엔드 서버 미리 시작
3. 테스트용 프로젝트와 API 키 준비
4. Discord/Slack 웹훅 설정해두기
5. 백업: 만약 라이브 데모 실패하면 스크린샷으로 대체

### 예상 질문
1. "Why did you choose Strategy pattern?"
   - "Because we needed to support multiple notification channels without modifying existing code."

2. "How does error grouping work?"
   - "We calculate SHA-256 hash of error type, file path, and line number."

3. "What happens if notification fails?"
   - "Notification failures are logged but do not affect the main error processing. We use asynchronous processing with Spring Async annotation, so the error is still saved to the database even if the notification fails."

4. "How does priority scoring work?"
   - "We use a weighted algorithm that considers page importance. Checkout pages have a 10x multiplier, login pages 8x, and homepage 5x. We also factor in error frequency and the number of affected users."

5. "Why did you use inheritance for BaseEntity?"
   - "To avoid code duplication. All entities need audit fields like createdAt, updatedAt, and id. By putting these in a BaseEntity superclass, we follow the DRY principle."
