/**
 * 백엔드 API 응답 타입 정의
 */

/**
 * 통일된 API 응답 형식
 */
export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  message: string | null;
  timestamp: string;
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
 */
export interface PageResponse<T> {
  success: boolean;
  data: T[];
  pagination: Pagination;
  message: string | null;
  timestamp: string;
}

/**
 * 프로젝트 응답
 */
export interface ProjectResponse {
  id: string;
  name: string;
  description: string | null;
  environment: string;
  apiKey: string;
  errorCount: number;
  lastErrorAt: string | null;
  createdAt: string;
  updatedAt: string;
}

/**
 * 에러 응답
 */
export interface ErrorResponse {
  id: string;
  projectId: string;
  errorHash: string;
  type: string;
  message: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  status: 'UNRESOLVED' | 'RESOLVED' | 'IGNORED';
  occurrenceCount: number;
  affectedUsersCount: number;
  priorityScore: number;
  firstSeenAt: string;
  lastSeenAt: string;
  resolvedAt: string | null;
  resolvedBy: string | null;
  stackTrace: string | null;
  file: string | null;
  line: number | null;
  method: string | null;
  url: string | null;
  httpMethod: string | null;
  userAgent: string | null;
}

/**
 * 대시보드 통계 응답
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
 */
export interface ErrorTrendResponse {
  timestamp: string;
  errorCount: number;
  userCount: number;
}

/**
 * 세션 리플레이 응답
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
 * 웹훅 설정 응답
 */
export interface WebhookConfigResponse {
  id: string;
  projectId: string;
  type: 'DISCORD' | 'SLACK' | 'TELEGRAM' | 'CUSTOM';
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
 */
export interface ProjectRequest {
  name: string;
  description?: string;
  environment: string;
}

/**
 * 웹훅 설정 요청
 */
export interface WebhookConfigRequest {
  projectId: string;
  type: 'DISCORD' | 'SLACK' | 'TELEGRAM' | 'CUSTOM';
  webhookUrl: string;
  name: string;
  enabled?: boolean;
  severityFilters?: string[];
  environmentFilters?: string[];
}

/**
 * 에러 수집 요청 (SDK에서 사용)
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
    events: any[];
  };
}
