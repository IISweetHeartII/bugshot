/**
 * Application Constants
 * 매직 넘버, 설정값, 메시지 등을 중앙에서 관리
 */
// ============================================
// Pagination
// ============================================
export const PAGINATION = {
  DEFAULT_PAGE: 0,
  DEFAULT_SIZE: 20,
  DASHBOARD_ERRORS_SIZE: 10,
  PROJECT_ERRORS_SIZE: 5,
} as const;
// ============================================
// API Configuration
// ============================================
export const API_CONFIG = {
  REPLAY_DOWNLOAD_EXPIRATION_SECONDS: 3600,
} as const;
// ============================================
// Toast Messages
// ============================================
export const MESSAGES = {
  // Success
  SUCCESS: {
    API_KEY_COPIED: 'API 키가 복사되었습니다!',
    API_KEY_REGENERATED: 'API 키가 재생성되었습니다.',
    PROJECT_CREATED: '프로젝트가 생성되었습니다.',
    PROJECT_DELETED: '프로젝트가 삭제되었습니다.',
    ERROR_RESOLVED: '에러가 해결됨으로 표시되었습니다.',
    ERROR_IGNORED: '에러가 무시됨으로 표시되었습니다.',
    ERROR_REOPENED: '에러가 다시 열렸습니다.',
    WEBHOOK_CREATED: '웹훅이 생성되었습니다.',
    WEBHOOK_UPDATED: '웹훅이 수정되었습니다.',
    WEBHOOK_DELETED: '웹훅이 삭제되었습니다.',
    WEBHOOK_TEST_SUCCESS: '웹훅 테스트가 성공했습니다.',
    DOWNLOAD_STARTED: '다운로드를 시작합니다.',
  },
  // Error
  ERROR: {
    LOAD_PROJECTS: '프로젝트 목록을 불러오는데 실패했습니다.',
    LOAD_PROJECT: '프로젝트를 불러오는데 실패했습니다.',
    LOAD_ERRORS: '에러 목록을 불러오는데 실패했습니다.',
    LOAD_ERROR: '에러 정보를 불러오는데 실패했습니다.',
    LOAD_DASHBOARD: '대시보드 데이터를 불러오는데 실패했습니다.',
    LOAD_WEBHOOKS: '웹훅 목록을 불러오는데 실패했습니다.',
    LOAD_REPLAY: '리플레이를 불러오는데 실패했습니다.',
    CREATE_PROJECT: '프로젝트 생성에 실패했습니다.',
    DELETE_PROJECT: '프로젝트 삭제에 실패했습니다.',
    REGENERATE_API_KEY: 'API 키 재생성에 실패했습니다.',
    RESOLVE_ERROR: '에러 해결 처리에 실패했습니다.',
    IGNORE_ERROR: '에러 무시 처리에 실패했습니다.',
    REOPEN_ERROR: '에러 재오픈에 실패했습니다.',
    DOWNLOAD_REPLAY: '다운로드에 실패했습니다.',
    CREATE_WEBHOOK: '웹훅 생성에 실패했습니다.',
    UPDATE_WEBHOOK: '웹훅 수정에 실패했습니다.',
    DELETE_WEBHOOK: '웹훅 삭제에 실패했습니다.',
    WEBHOOK_TEST: '웹훅 테스트에 실패했습니다.',
    VALIDATION_PROJECT_NAME: '프로젝트 이름을 입력해주세요.',
    VALIDATION_WEBHOOK_URL: '웹훅 URL을 입력해주세요.',
    VALIDATION_WEBHOOK_NAME: '웹훅 이름을 입력해주세요.',
    UNKNOWN: '알 수 없는 오류가 발생했습니다.',
  },
  // Loading
  LOADING: {
    PROJECTS: '프로젝트를 불러오는 중...',
    ERRORS: '에러를 불러오는 중...',
    DASHBOARD: '대시보드를 불러오는 중...',
    REPLAY: '리플레이를 불러오는 중...',
    DEFAULT: '데이터를 불러오는 중...',
  },
} as const;
// ============================================
// Severity Configuration
// ============================================
export const SEVERITY_CONFIG = {
  CRITICAL: { label: 'Critical', color: 'severity-critical', badgeVariant: 'destructive' as const },
  HIGH: { label: 'High', color: 'severity-high', badgeVariant: 'destructive' as const },
  MEDIUM: { label: 'Medium', color: 'severity-medium', badgeVariant: 'default' as const },
  LOW: { label: 'Low', color: 'severity-low', badgeVariant: 'secondary' as const },
} as const;
// ============================================
// Status Configuration
// ============================================
export const STATUS_CONFIG = {
  UNRESOLVED: { label: '미해결', color: 'text-red-400' },
  RESOLVED: { label: '해결됨', color: 'text-green-400' },
  IGNORED: { label: '무시됨', color: 'text-gray-400' },
} as const;
// ============================================
// Webhook Types
// ============================================
export const WEBHOOK_TYPES = [
  { value: 'DISCORD', label: 'Discord' },
  { value: 'SLACK', label: 'Slack' },
  { value: 'TELEGRAM', label: 'Telegram' },
  { value: 'CUSTOM', label: 'Custom' },
] as const;
// ============================================
// Environment Options
// ============================================
export const ENVIRONMENTS = [
  { value: 'PRODUCTION', label: 'Production' },
  { value: 'STAGING', label: 'Staging' },
  { value: 'DEVELOPMENT', label: 'Development' },
] as const;
// ============================================
// Period Options (for dashboard)
// ============================================
export const PERIOD_OPTIONS = [
  { value: '24h', label: '24시간' },
  { value: '7d', label: '7일' },
  { value: '30d', label: '30일' },
] as const;
// ============================================
// Filter Options (for errors page)
// ============================================
export const SEVERITY_FILTER_OPTIONS = [
  { value: 'ALL', label: '모든 심각도' },
  { value: 'CRITICAL', label: '🔴 Critical' },
  { value: 'HIGH', label: '🟡 High' },
  { value: 'MEDIUM', label: '🟢 Medium' },
  { value: 'LOW', label: '⚪ Low' },
] as const;
export const STATUS_FILTER_OPTIONS = [
  { value: 'ALL', label: '모든 상태' },
  { value: 'UNRESOLVED', label: '미해결' },
  { value: 'RESOLVED', label: '해결됨' },
  { value: 'IGNORED', label: '무시됨' },
] as const;
