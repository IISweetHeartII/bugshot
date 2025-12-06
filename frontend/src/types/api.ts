/**
 * 백엔드 API 응답 타입 정의
 * 백엔드 DTO와 동기화됨
 */

/**
 * 유효성 검증 에러
 * 백엔드 ValidationError.java와 동기화됨
 */
export interface ValidationError {
  field: string;
  message: string;
  rejectedValue?: unknown;
}

/**
 * 통일된 API 응답 형식
 * 백엔드 ApiResponse.java와 동기화됨
 */
export interface ApiResponse<T> {
  success: boolean;
  code?: string; // 응답 코드 (COMMON_200, PROJECT_404 등)
  data: T | null;
  message: string | null;
  timestamp: string;
  path?: string; // 요청 경로 (에러 시)
  traceId?: string; // 추적 ID (에러 시)
  errors?: ValidationError[]; // 유효성 검증 에러 목록
}

/**
 * Session Replay 이벤트 (rrweb 호환)
 */
export interface SessionReplayEvent {
  type: number;
  data: Record<string, unknown>;
  timestamp: number;
}

/**
 * 페이지네이션 정보
 */
export interface Pagination {
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

/**
 * 페이지네이션이 포함된 API 응답
 * 백엔드 PageResponse.java와 동기화됨
 */
export interface PageResponse<T> {
  success: boolean;
  data: T[];
  pagination: Pagination;
  message: string | null;
  timestamp: string;
}

/**
 * 프로젝트 통계 정보
 * 백엔드 ProjectResponse.StatsInfo와 동기화됨
 */
export interface ProjectStatsInfo {
  totalErrors: number;
  totalUsersAffected: number;
  criticalCount?: number;
  highCount?: number;
  mediumCount?: number;
  lowCount?: number;
  lastErrorAt: string | null;
}

/**
 * 프로젝트 응답
 * 백엔드 ProjectResponse.java와 동기화됨
 */
export interface ProjectResponse {
  id: string;
  name: string;
  description: string | null;
  environment: string;
  apiKey: string;
  sessionReplayEnabled: boolean;
  sessionReplaySampleRate: number;
  stats: ProjectStatsInfo;
  createdAt: string;
  updatedAt: string;
}

/**
 * 에러 응답
 * 백엔드 ErrorResponse.java와 동기화됨
 */
export interface ErrorResponse {
  id: string;
  projectId: string;
  errorType: string;
  errorMessage: string;
  filePath: string | null;
  lineNumber: number | null;
  methodName: string | null;
  stackTrace: string | null;
  priorityScore: number;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  occurrenceCount: number;
  affectedUsersCount: number;
  status: 'UNRESOLVED' | 'RESOLVED' | 'IGNORED';
  resolvedAt: string | null;
  resolvedBy: string | null;
  firstSeenAt: string;
  lastSeenAt: string;
}

/**
 * 대시보드 통계 응답
 * 백엔드 DashboardStatsResponse.java와 동기화됨
 */
export interface DashboardStatsResponse {
  totalErrors: number;
  unresolvedErrors: number;
  todayErrors: number;
  affectedUsers: number;
  changeRate: number;
  avgResponseTime: number | null;
  severityCount: {
    critical: number;
    high: number;
    medium: number;
    low: number;
  };
}

/**
 * 에러 트렌드 응답
 * 백엔드 ErrorTrendResponse.java와 동기화됨
 */
export interface ErrorTrendResponse {
  timestamp: string;
  errorCount: number;
  userCount: number;
}

/**
 * 세션 리플레이 응답
 * 백엔드 SessionReplayResponse.java와 동기화됨
 */
export interface SessionReplayResponse {
  errorId: string;
  replayUrl: string;
  size: number;
  recordedAt: string;
  duration: number; // seconds
  userInfo: {
    userId: string | null;
    ip: string | null;
    userAgent: string | null;
    browser: string | null;
    os: string | null;
  };
}

/**
 * 웹훅 타입
 * 백엔드 WebhookConfig.WebhookType과 동기화됨
 */
export type WebhookType = 'DISCORD' | 'SLACK' | 'TELEGRAM' | 'CUSTOM';

/**
 * 웹훅 설정 응답
 * 백엔드 WebhookConfigResponse.java와 동기화됨
 */
export interface WebhookConfigResponse {
  id: string;
  projectId: string;
  type: WebhookType;
  name: string;
  webhookUrl: string;
  enabled: boolean;
  severityFilters: string[];
  environmentFilters: string[];
  createdAt: string;
  updatedAt: string;
  lastTriggeredAt: string | null;
  totalSent: number;
  failureCount: number;
}

/**
 * 프로젝트 생성/수정 요청
 * 백엔드 ProjectRequest.java와 동기화됨
 */
export interface ProjectRequest {
  name: string;
  description?: string;
  environment: string;
  sessionReplayEnabled?: boolean;
  sessionReplaySampleRate?: number;
}

/**
 * 웹훅 설정 요청
 * 백엔드 WebhookConfigRequest.java와 동기화됨
 */
export interface WebhookConfigRequest {
  projectId: string;
  type: WebhookType;
  webhookUrl: string;
  name: string;
  enabled?: boolean;
  severityFilters?: string[];
  environmentFilters?: string[];
}

/**
 * Next.js API Route Context Types
 */
export type IdRouteContext = { params: Promise<{ id: string }> };
export type ErrorIdRouteContext = { params: Promise<{ errorId: string }> };

/**
 * 에러 수집 요청 (SDK에서 사용)
 * 백엔드 IngestRequest.java와 동기화됨
 */
export interface IngestRequest {
  apiKey: string;
  error: {
    type: string;
    message: string;
    stackTrace?: string;
    file?: string;
    line?: number;
    method?: string;
  };
  context: {
    url: string;
    httpMethod?: string;
    userAgent?: string;
    sessionId?: string;
  };
  sessionReplay?: {
    sessionId: string;
    durationMs: number;
    events: SessionReplayEvent[];
  };
}
