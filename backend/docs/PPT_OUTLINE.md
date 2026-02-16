# Bugshot Presentation Outline

**발표 시간: 약 7분**
- What (시스템 설명): **2분** (직접 발표)
- How (설계/구현): **1분** (직접 발표)
- Demo: **4분** (영상으로 대체, 캡션 포함)

---

# Part 1: What - 시스템 설명 (2분)

## Slide 1: Title (15초)

**Content:**
- Bugshot - Real-time Error Monitoring System
- Team: 김덕환 (20200477), 정은재 (20226495)
- Object Oriented Programming, Prof. Bong-Soo Sohn

**Script:**
> "Good afternoon. We are Team Bugshot. Today we present Bugshot - a real-time error monitoring system built with Java and Spring Boot."

---

## Slide 2: Problem (30초)

**Content:**
- JavaScript errors occur silently in production
- Users don't report errors - they just leave
- Hard to reproduce without context

**Script:**
> "The problem: JavaScript errors happen silently. Users don't report bugs - they just leave your website. By the time developers discover issues through complaints, customers are already lost."

---

## Slide 3: Solution (45초)

**Content:**
- Real-time Error Capture (SDK -> Backend API)
- Intelligent Grouping (SHA-256 hash)
- Priority-based Alerts (checkout 10x, login 8x)
- Multi-channel Notifications (Discord, Slack, Email, Telegram, Kakao)

**Script:**
> "Bugshot solves this. Our SDK captures errors automatically and sends them to our Spring Boot backend. We use SHA-256 hashing to group identical errors. The system calculates priority - checkout page errors get 10x weight, login gets 8x. Then it sends alerts through Discord, Slack, or other channels."

---

## Slide 4: Architecture (30초)

**Content:**
- Backend: Java 21, Spring Boot 3.5
- Database: MySQL 8.0, Redis 6.0
- Storage: Cloudflare R2
- API: bugshot-api.log8.kr
- Swagger: bugshot-api.log8.kr/swagger-ui

**Script:**
> "Our backend is built with Java 21 and Spring Boot 3.5. We use MySQL for storage and Redis for caching. The system is deployed and running - you can try it at bugshot-api.log8.kr."

---

# Part 2: How - 설계/구현 (1분)

## Slide 5: OOP Concepts (40초)

**Content:**

**1. Inheritance**
- BaseEntity -> User, Project, Error
- Shared audit fields (createdAt, updatedAt)

**2. Polymorphism**
- NotificationStrategy interface
- 6 implementations: Discord, Slack, Email, Telegram, Webhook, Kakao

**3. Encapsulation**
- Error.calculatePriority() - business logic inside entity

**4. Design Patterns**
- Strategy Pattern: Notification channels
- Observer Pattern: Event-driven processing

**Script:**
> "For OOP concepts: First, INHERITANCE - all entities extend BaseEntity for shared audit fields. Second, POLYMORPHISM - we have a NotificationStrategy interface with 6 implementations, one for each channel. Third, ENCAPSULATION - priority calculation logic is inside the Error entity. We also used Strategy Pattern for notifications and Observer Pattern for event processing."

---

## Slide 6: UML Diagrams (20초)

**Content:**
- Class Diagram: BaseEntity hierarchy, Strategy interface
- Use Case Diagram: SDK, Developer, System actors
- Activity Diagram: Error ingest flow

(Show diagram images)

**Script:**
> "Here are our UML diagrams showing the class hierarchy, use cases, and activity flow for error processing."

---

# Part 3: Demo (4분) - 영상으로 대체

## Demo Video Content (캡션 추가 예정)

1. **Swagger UI** (30초)
   - Show API documentation at /swagger-ui
   - Point out main controllers: Ingest, Error, Project, Webhook

2. **POST /api/ingest** (45초)
   - Send error with API key
   - Show request body structure
   - Show 201 response

3. **Error List** (30초)
   - GET /api/errors
   - Show priority scores, occurrence counts

4. **Error Detail** (30초)
   - Click on error
   - Show stack trace, affected users, severity

5. **Webhook Setup** (30초)
   - Show webhook configuration
   - Discord/Slack integration

6. **Notification** (30초)
   - Show Discord receiving the alert
   - Error type, project, location, occurrence count

7. **SDK Test Page** (15초)
   - Show test buttons triggering errors

---

# Part 4: Conclusion (30초)

## Slide 7: Summary

**Content:**
- Real-time error monitoring with SDK
- SHA-256 based error grouping
- Multi-channel notifications (Strategy Pattern)
- Event-driven architecture (Observer Pattern)

**Script:**
> "In summary, Bugshot provides real-time error monitoring with intelligent grouping and multi-channel notifications. We applied OOP concepts like inheritance, polymorphism, and encapsulation, along with Strategy and Observer patterns. Thank you."

---

## Slide 8: Q&A

**Content:**
- GitHub: github.com/IISweetHeartII/bugshot
- API: bugshot-api.log8.kr/swagger-ui
- Thank you!

---

# 예상 질문 & 답변

**Q1: "Why Strategy Pattern for notifications?"**
> "Because we need to support multiple channels without modifying existing code. Adding a new channel just requires a new implementation class."

**Q2: "How does error grouping work?"**
> "We calculate SHA-256 hash from error type, file path, and line number. Same hash means same error group."

**Q3: "How does priority calculation work?"**
> "We use weighted scoring. Checkout page errors get 10x multiplier, login 8x, homepage 5x. We also consider occurrence count and affected users."

**Q4: "What if notification fails?"**
> "Notifications run asynchronously with @Async. If one fails, the error is still saved. Failures are logged but don't block the main flow."

**Q5: "Why inherit from BaseEntity?"**
> "To avoid code duplication. All entities need createdAt and updatedAt fields. BaseEntity provides these, following DRY principle."

---

# 발표 체크리스트

- [ ] PPT 파일 준비 (8 슬라이드)
- [ ] Demo 영상 촬영 (4분)
- [ ] 영상에 캡션 추가
- [ ] 발표 연습 (What 2분 + How 1분)
- [ ] 백업: 라이브 서버 접속 테스트
