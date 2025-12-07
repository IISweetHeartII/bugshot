"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { ArrowLeft, RefreshCw, Copy, Settings, TrendingDown, Users, AlertCircle } from "lucide-react";
import { formatRelativeTime, formatNumber, getSeverityBadgeVariant, formatDate } from "@/lib/utils";
import { MESSAGES, PAGINATION } from "@/lib/constants";
import { useCopyToClipboard } from "@/hooks";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { StatCard } from "@/components/ui/stat-card";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { useConfirmDialog } from "@/components/ui/confirm-dialog";
import { toast } from "sonner";
import type { ProjectResponse, ErrorResponse } from "@/types/api";

export default function ProjectDetailPage() {
  const params = useParams();
  const router = useRouter();
  const projectId = params.id as string;

  const [project, setProject] = useState<ProjectResponse | null>(null);
  const [recentErrors, setRecentErrors] = useState<ErrorResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const { copied, copy } = useCopyToClipboard();
  const { confirm, ConfirmDialogComponent } = useConfirmDialog();

  useEffect(() => {
    loadProject();
    loadRecentErrors();
  }, [projectId]);

  const loadProject = async () => {
    try {
      setLoading(true);
      const data = await api.getProject(projectId);
      setProject(data);
    } catch (error) {
      console.error("Failed to load project:", error);
      toast.error(MESSAGES.ERROR.LOAD_PROJECT);
    } finally {
      setLoading(false);
    }
  };

  const loadRecentErrors = async () => {
    try {
      const data = await api.getErrors({
        projectId,
        page: PAGINATION.DEFAULT_PAGE,
        size: PAGINATION.PROJECT_ERRORS_SIZE,
        sort: "priority",
        status: "UNRESOLVED",
      });
      setRecentErrors(data.data);
    } catch (error) {
      console.error("Failed to load recent errors:", error);
    }
  };

  const handleRegenerateKey = async () => {
    const confirmed = await confirm({
      title: "API 키 재생성",
      description: "API 키를 재생성하시겠습니까? 기존 키는 사용할 수 없게 됩니다.",
      confirmText: "재생성",
      confirmVariant: "destructive",
    });

    if (!confirmed) return;

    try {
      await api.regenerateApiKey(projectId);
      await loadProject();
      toast.success(MESSAGES.SUCCESS.API_KEY_REGENERATED);
    } catch (error) {
      console.error("Failed to regenerate API key:", error);
      toast.error(MESSAGES.ERROR.REGENERATE_API_KEY);
    }
  };

  if (loading) {
    return <LoadingSpinner message={MESSAGES.LOADING.PROJECTS} />;
  }

  if (!project) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] space-y-4">
        <p className="text-text-secondary">프로젝트를 찾을 수 없습니다.</p>
        <Button variant="outline" onClick={() => router.push("/projects")}>
          <ArrowLeft className="w-4 h-4 mr-2" aria-hidden="true" />
          프로젝트 목록으로
        </Button>
      </div>
    );
  }

  return (
    <motion.div
      className="space-y-6"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
    >
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => router.push("/projects")}>
            <ArrowLeft className="w-4 h-4 mr-2" aria-hidden="true" />
            뒤로
          </Button>
          <div>
            <div className="flex items-center gap-3 mb-1">
              <h1 className="text-2xl font-bold text-text-primary">{project.name}</h1>
              <Badge variant={project.environment === "PRODUCTION" ? "destructive" : "secondary"}>
                {project.environment}
              </Badge>
            </div>
            {project.description && (
              <p className="text-sm text-text-secondary">{project.description}</p>
            )}
          </div>
        </div>

        <Button variant="outline" onClick={() => router.push(`/projects/${projectId}/settings`)}>
          <Settings className="w-4 h-4 mr-2" aria-hidden="true" />
          설정
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatCard
          title="총 에러 수"
          value={formatNumber(project.stats.totalErrors)}
          icon={AlertCircle}
          iconColor="text-error"
        />
        <StatCard
          title="마지막 에러"
          value={project.stats.lastErrorAt ? formatRelativeTime(project.stats.lastErrorAt) : "없음"}
          icon={TrendingDown}
          iconColor="text-warning"
        />
        <StatCard
          title="생성일"
          value={formatDate(project.createdAt)}
          icon={Users}
          iconColor="text-primary"
        />
      </div>

      {/* API Key Section */}
      <Card size="md">
        <CardHeader>
          <CardTitle>API 키</CardTitle>
          <Button variant="outline" size="sm" onClick={handleRegenerateKey}>
            <RefreshCw className="w-4 h-4 mr-2" aria-hidden="true" />
            재생성
          </Button>
        </CardHeader>

        <div className="flex items-center gap-2 bg-bg-tertiary p-4 rounded-lg border border-bg-primary">
          <code className="flex-1 font-mono text-sm text-text-primary break-all">
            {project.apiKey}
          </code>
          <Button variant="ghost" size="sm" onClick={() => project && copy(project.apiKey)}>
            {copied ? (
              <>
                <Copy className="w-4 h-4 mr-2 text-success" aria-hidden="true" />
                복사됨
              </>
            ) : (
              <>
                <Copy className="w-4 h-4 mr-2" aria-hidden="true" />
                복사
              </>
            )}
          </Button>
        </div>

        <p className="text-sm text-text-secondary mt-3">
          이 API 키를 사용하여 BugShot SDK를 초기화하세요.
        </p>
      </Card>

      {/* Recent Errors */}
      <Card size="md">
        <CardHeader>
          <CardTitle>최근 미해결 에러</CardTitle>
          <Button
            variant="outline"
            size="sm"
            onClick={() => router.push(`/errors?projectId=${projectId}`)}
          >
            전체 보기
          </Button>
        </CardHeader>

        {recentErrors.length === 0 ? (
          <p className="text-center text-text-secondary py-8">
            미해결 에러가 없습니다.
          </p>
        ) : (
          <div className="space-y-3">
            {recentErrors.map((error) => (
              <div
                key={error.id}
                className="flex items-start justify-between p-4 bg-bg-tertiary rounded-lg border border-bg-primary hover:border-primary cursor-pointer transition-colors"
                onClick={() => router.push(`/errors/${error.id}`)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => e.key === "Enter" && router.push(`/errors/${error.id}`)}
              >
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <Badge variant={getSeverityBadgeVariant(error.severity)}>
                      {error.severity}
                    </Badge>
                    <span className="text-sm font-medium text-text-primary">
                      {error.errorType}
                    </span>
                  </div>
                  <p className="text-sm text-text-secondary line-clamp-1">
                    {error.errorMessage}
                  </p>
                  <div className="flex items-center gap-3 mt-2 text-xs text-text-secondary">
                    <span>{error.occurrenceCount}회 발생</span>
                    <span aria-hidden="true">•</span>
                    <span>{formatRelativeTime(error.lastSeenAt)}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* SDK Integration Guide */}
      <Card size="md">
        <CardTitle className="mb-4">SDK 통합 가이드</CardTitle>

        <div className="space-y-4">
          <div>
            <h3 className="text-sm font-semibold text-text-primary mb-2">JavaScript</h3>
            <pre className="bg-bg-tertiary p-4 rounded-lg border border-bg-primary overflow-x-auto">
              <code className="text-sm text-text-primary">{`import BugShot from '@bugshot/browser-sdk';

BugShot.init({
  apiKey: '${project.apiKey}',
  environment: '${project.environment}',
  release: '1.0.0'
});`}</code>
            </pre>
          </div>

          <div>
            <h3 className="text-sm font-semibold text-text-primary mb-2">React</h3>
            <pre className="bg-bg-tertiary p-4 rounded-lg border border-bg-primary overflow-x-auto">
              <code className="text-sm text-text-primary">{`import { ErrorBoundary } from '@bugshot/react';

<ErrorBoundary apiKey="${project.apiKey}">
  <App />
</ErrorBoundary>`}</code>
            </pre>
          </div>
        </div>
      </Card>

      {/* Confirm Dialog */}
      {ConfirmDialogComponent}
    </motion.div>
  );
}
