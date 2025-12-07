-- ============================================
-- Bugshot - KST to UTC Migration Script
-- ============================================
-- 이 스크립트는 기존 KST(Asia/Seoul) 기준 타임스탬프를 UTC로 변환합니다.
-- KST = UTC + 9시간 이므로, 모든 타임스탬프에서 9시간을 뺍니다.
--
-- 실행 전 주의사항:
-- 1. 반드시 백업을 먼저 수행하세요!
-- 2. 서비스 중단 상태에서 실행하세요.
-- 3. 이 스크립트는 한 번만 실행해야 합니다.
-- ============================================

-- 백업 확인 알림 (MySQL에서는 주석으로만 표시)
-- BACKUP YOUR DATABASE BEFORE RUNNING THIS SCRIPT!

-- ============================================
-- 1. users 테이블
-- ============================================
UPDATE users SET
    created_at = DATE_SUB(created_at, INTERVAL 9 HOUR),
    updated_at = DATE_SUB(updated_at, INTERVAL 9 HOUR)
WHERE created_at IS NOT NULL;

-- ============================================
-- 2. projects 테이블
-- ============================================
UPDATE projects SET
    created_at = DATE_SUB(created_at, INTERVAL 9 HOUR),
    updated_at = DATE_SUB(updated_at, INTERVAL 9 HOUR),
    last_error_at = DATE_SUB(last_error_at, INTERVAL 9 HOUR)
WHERE created_at IS NOT NULL;

-- ============================================
-- 3. errors 테이블
-- ============================================
UPDATE errors SET
    created_at = DATE_SUB(created_at, INTERVAL 9 HOUR),
    updated_at = DATE_SUB(updated_at, INTERVAL 9 HOUR),
    first_seen_at = DATE_SUB(first_seen_at, INTERVAL 9 HOUR),
    last_seen_at = DATE_SUB(last_seen_at, INTERVAL 9 HOUR),
    resolved_at = DATE_SUB(resolved_at, INTERVAL 9 HOUR)
WHERE created_at IS NOT NULL;

-- ============================================
-- 4. error_occurrences 테이블
-- ============================================
UPDATE error_occurrences SET
    occurred_at = DATE_SUB(occurred_at, INTERVAL 9 HOUR)
WHERE occurred_at IS NOT NULL;

-- ============================================
-- 5. session_replays 테이블
-- ============================================
UPDATE session_replays SET
    recorded_at = DATE_SUB(recorded_at, INTERVAL 9 HOUR),
    expires_at = DATE_SUB(expires_at, INTERVAL 9 HOUR)
WHERE recorded_at IS NOT NULL;

-- ============================================
-- 6. webhook_configs 테이블
-- ============================================
UPDATE webhook_configs SET
    created_at = DATE_SUB(created_at, INTERVAL 9 HOUR),
    updated_at = DATE_SUB(updated_at, INTERVAL 9 HOUR),
    last_triggered_at = DATE_SUB(last_triggered_at, INTERVAL 9 HOUR)
WHERE created_at IS NOT NULL;

-- ============================================
-- 7. notification_channels 테이블
-- ============================================
UPDATE notification_channels SET
    created_at = DATE_SUB(created_at, INTERVAL 9 HOUR),
    updated_at = DATE_SUB(updated_at, INTERVAL 9 HOUR),
    last_notified_at = DATE_SUB(last_notified_at, INTERVAL 9 HOUR)
WHERE created_at IS NOT NULL;

-- ============================================
-- 마이그레이션 완료 확인
-- ============================================
SELECT 'Migration completed! All timestamps converted from KST to UTC.' AS status;
