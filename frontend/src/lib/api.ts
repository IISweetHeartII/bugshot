import axios, { AxiosInstance, AxiosRequestConfig } from 'axios';
import type {
  ApiResponse,
  PageResponse,
  ProjectResponse,
  ErrorResponse,
  DashboardStatsResponse,
  ErrorTrendResponse,
  SessionReplayResponse,
  WebhookConfigResponse,
  ProjectRequest,
  WebhookConfigRequest,
  IngestRequest,
} from '@/types/api';

/**
 * API Client using BFF pattern
 *
 * 모든 API 요청은 Next.js API Routes를 통해 처리됩니다.
 * 인증은 서버 사이드에서 NextAuth 세션을 통해 자동으로 처리되므로,
 * 클라이언트에서는 별도의 인증 헤더를 보내지 않습니다.
 *
 * 보안:
 * - X-User-Id 헤더는 더 이상 클라이언트에서 직접 설정하지 않습니다
 * - 모든 인증은 Next.js 서버에서 처리됩니다
 */
class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: '',  // Use relative paths - all routes go to Next.js API Routes
      headers: {
        'Content-Type': 'application/json',
      },
      withCredentials: true,
    });

    // Response interceptor for error handling
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          // Redirect to login if unauthorized
          if (typeof window !== 'undefined') {
            window.location.href = '/login';
          }
        }
        return Promise.reject(error);
      }
    );
  }

  // Generic request method - unwraps ApiResponse
  async request<T>(config: AxiosRequestConfig): Promise<T> {
    const response = await this.client.request<ApiResponse<T>>(config);

    // Unwrap ApiResponse and return data
    if (response.data.success) {
      return response.data.data as T;
    } else {
      throw new Error(response.data.message || 'API request failed');
    }
  }

  // Generic request for PageResponse
  async requestPage<T>(config: AxiosRequestConfig): Promise<PageResponse<T>> {
    const response = await this.client.request<PageResponse<T>>(config);
    return response.data;
  }

  // Projects API
  async getProjects(): Promise<ProjectResponse[]> {
    return this.request<ProjectResponse[]>({
      method: 'GET',
      url: '/api/projects',
    });
  }

  async getProject(id: string): Promise<ProjectResponse> {
    return this.request<ProjectResponse>({
      method: 'GET',
      url: `/api/projects/${id}`,
    });
  }

  async createProject(data: ProjectRequest): Promise<ProjectResponse> {
    return this.request<ProjectResponse>({
      method: 'POST',
      url: '/api/projects',
      data,
    });
  }

  async updateProject(id: string, data: Partial<ProjectRequest>): Promise<ProjectResponse> {
    return this.request<ProjectResponse>({
      method: 'PUT',
      url: `/api/projects/${id}`,
      data,
    });
  }

  async deleteProject(id: string): Promise<void> {
    return this.request<void>({
      method: 'DELETE',
      url: `/api/projects/${id}`,
    });
  }

  async regenerateApiKey(id: string): Promise<{ apiKey: string }> {
    return this.request<{ apiKey: string }>({
      method: 'POST',
      url: `/api/projects/${id}/regenerate-key`,
    });
  }

  // Errors API
  async getErrors(params?: {
    projectId?: string;
    severity?: string;
    status?: string;
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<PageResponse<ErrorResponse>> {
    return this.requestPage<ErrorResponse>({
      method: 'GET',
      url: '/api/errors',
      params,
    });
  }

  async getError(id: string): Promise<ErrorResponse> {
    return this.request<ErrorResponse>({
      method: 'GET',
      url: `/api/errors/${id}`,
    });
  }

  async resolveError(id: string, userId?: string): Promise<void> {
    return this.request<void>({
      method: 'PUT',
      url: `/api/errors/${id}/resolve`,
      params: userId ? { userId } : undefined,
    });
  }

  async ignoreError(id: string): Promise<void> {
    return this.request<void>({
      method: 'PUT',
      url: `/api/errors/${id}/ignore`,
    });
  }

  async reopenError(id: string): Promise<void> {
    return this.request<void>({
      method: 'PUT',
      url: `/api/errors/${id}/reopen`,
    });
  }

  // Dashboard API
  async getDashboardStats(projectId: string, period: string = '7d'): Promise<DashboardStatsResponse> {
    return this.request<DashboardStatsResponse>({
      method: 'GET',
      url: '/api/dashboard/stats',
      params: { projectId, period },
    });
  }

  async getErrorTrends(projectId: string, period: string = '7d'): Promise<ErrorTrendResponse[]> {
    return this.request<ErrorTrendResponse[]>({
      method: 'GET',
      url: '/api/dashboard/trends',
      params: { projectId, period },
    });
  }

  // Session Replay API
  async getSessionReplay(errorId: string): Promise<SessionReplayResponse> {
    return this.request<SessionReplayResponse>({
      method: 'GET',
      url: `/api/replays/${errorId}`,
    });
  }

  async getReplayDownloadUrl(errorId: string, expirationSeconds: number = 3600): Promise<string> {
    return this.request<string>({
      method: 'GET',
      url: `/api/replays/${errorId}/download-url`,
      params: { expirationSeconds },
    });
  }

  // Webhooks API
  async getWebhooks(projectId: string): Promise<WebhookConfigResponse[]> {
    return this.request<WebhookConfigResponse[]>({
      method: 'GET',
      url: '/api/webhooks',
      params: { projectId },
    });
  }

  async createWebhook(data: WebhookConfigRequest): Promise<WebhookConfigResponse> {
    return this.request<WebhookConfigResponse>({
      method: 'POST',
      url: '/api/webhooks',
      data,
    });
  }

  async updateWebhook(id: string, data: WebhookConfigRequest): Promise<WebhookConfigResponse> {
    return this.request<WebhookConfigResponse>({
      method: 'PUT',
      url: `/api/webhooks/${id}`,
      data,
    });
  }

  async deleteWebhook(id: string): Promise<void> {
    return this.request<void>({
      method: 'DELETE',
      url: `/api/webhooks/${id}`,
    });
  }

  async testWebhook(id: string): Promise<string> {
    return this.request<string>({
      method: 'POST',
      url: `/api/webhooks/${id}/test`,
    });
  }

  // Ingest API (for SDK testing)
  async ingestError(data: IngestRequest): Promise<{ errorId: string; occurrenceId: string }> {
    return this.request<{ errorId: string; occurrenceId: string }>({
      method: 'POST',
      url: '/api/ingest',
      data,
    });
  }
}

export const api = new ApiClient();

/**
 * Initialize API client
 *
 * Note: BFF 패턴에서는 인증이 서버 사이드에서 자동으로 처리되므로,
 * 이 함수는 하위 호환성을 위해 유지되지만 더 이상 아무 작업도 하지 않습니다.
 *
 * @deprecated BFF 패턴에서는 더 이상 필요하지 않습니다
 */
export function initializeApi(_userId: string | null) {
  // No-op: Authentication is now handled server-side via BFF pattern
}
