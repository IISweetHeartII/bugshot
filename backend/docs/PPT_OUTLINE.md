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
"Hello, we are Team Bugshot. Today we will present our project, Bugshot - a real-time error monitoring system."

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
"JavaScript errors in web applications often go unnoticed. Users experience bugs but rarely report them - they simply leave. By the time developers learn about these issues, they've already lost users."

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
```

### Speaker Notes
"Our system is a full-stack application. The Browser SDK captures errors and sends them to our Spring Boot backend. The Next.js frontend provides a dashboard for developers. We use MySQL for data storage, Redis for caching, and Cloudflare R2 for session replay files."

---

## Slide 5: Deployment Status (30초)

### Content
```
🚀 Live in Production!

┌─────────────────────────────────────────────────┐
│              Deployment Architecture             │
├─────────────────────────────────────────────────┤
│                                                  │
│  [Cloudflare CDN + WAF]                         │
│         │              │                        │
│         ▼              ▼                        │
│    ┌─────────┐   ┌─────────────┐               │
│    │ Vercel  │   │ Cloudflare  │               │
│    │ (Front) │   │   Tunnel    │               │
│    └─────────┘   └──────┬──────┘               │
│                         ▼                       │
│                  ┌─────────────┐               │
│                  │  Mac Mini   │               │
│                  │  (Docker)   │               │
│                  └─────────────┘               │
│                                                  │
└─────────────────────────────────────────────────┘

🔗 Live URLs:
• Frontend: bugshot.log8.kr
• API Docs: bugshot-api.log8.kr/swagger-ui
• GitHub: github.com/IISweetHeartII/bugshot
```

### Speaker Notes
"What makes this project special is that it's actually deployed and running in production. You can access our frontend at bugshot.log8.kr and the API documentation at bugshot-api.log8.kr. The source code is available on our public GitHub repository. This demonstrates real-world deployment experience."

---

## Slide 6: OOP Concepts Applied (45초)

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
"We applied multiple OOP concepts. Encapsulation keeps business logic inside entities. Inheritance from BaseEntity provides common audit fields. Polymorphism through the NotificationStrategy interface allows different notification implementations. We also used Strategy, Observer, and Builder patterns."

---

## Slide 7: Key Implementation - Strategy Pattern (30초)

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
"The Strategy pattern is key to our notification system. Each channel implements the NotificationStrategy interface. To add a new channel like Microsoft Teams, we simply create a new strategy class - no existing code needs to change."

---

## Slide 8: UML Diagrams (40초)

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
"This class diagram shows our main entities and their relationships. User owns Projects, Projects contain Errors, and each Error has multiple Occurrences."

---

## Slide 9: Demo Introduction (15초)

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

## Slide 10-14: Demo Slides (4분)

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

## Slide 15: Conclusion (30초)

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
"In summary, Bugshot provides comprehensive error monitoring with intelligent grouping and multi-channel alerts. We successfully applied OOP concepts including Strategy and Observer patterns. Thank you for your attention."

---

## Slide 16: Q&A

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
| Slides 1-7 (What & How) | 3분 |
| Demo | 4분 |
| Total | 7분 |

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
   - "Notification failures are logged but don't affect the main error processing. We use async processing."
