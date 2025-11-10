"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { ArrowLeft, RefreshCw, Copy, Settings, TrendingDown, Users, AlertCircle } from "lucide-react";
import { formatRelativeTime, formatNumber, copyToClipboard } from "@/lib/utils";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import type { ProjectResponse, ErrorResponse } from "@/types/api";

export default function ProjectDetailPage() {
  const params = useParams();
  const router = useRouter();
  const projectId = params.id as string;

  const [project, setProject] = useState<ProjectResponse | null>(null);
  const [recentErrors, setRecentErrors] = useState<ErrorResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [copiedKey, setCopiedKey] = useState(false);

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
      toast.error("프로젝트를 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const loadRecentErrors = async () => {
    try {
      const data = await api.getErrors({
        projectId,
        page: 0,
        size: 5,
        sort: "priority",
        status: "UNRESOLVED",
      });
      setRecentErrors(data.data);
    } catch (error) {
      console.error("Failed to load recent errors:", error);
    }
  };

  const handleCopyApiKey = async () => {
    if (!project) return;
    const success = await copyToClipboard(project.apiKey);
    if (success) {
      setCopiedKey(true);
      setTimeout(() => setCopiedKey(false), 2000);
      toast.success("API 키가 복사되었습니다!");
    }
  };

  const handleRegenerateKey = async () => {
    if (!confirm("API 키를 재생성하시겠습니까? 기존 키는 사용할 수 없게 됩니다.")) {
      return;
    }

    try {
      await api.regenerateApiKey(projectId);
      await loadProject();
      toast.success("API 키가 재생성되었습니다!");
    } catch (error) {
      console.error("Failed to regenerate API key:", error);
      toast.error("API 키 재생성에 실패했습니다.");
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-text-secondary">프로젝트를 불러오는 중...</div>
      </div>
    );
  }

  if (!project) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] space-y-4">
        <p className="text-text-secondary">프로젝트를 찾을 수 없습니다.</p>
        <Button variant="outline" onClick={() => router.push("/projects")}>
          <ArrowLeft className="w-4 h-4 mr-2" />
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
            <ArrowLeft className="w-4 h-4 mr-2" />
            뒤로
          </Button>
          <div>
            <div className="flex items-center gap-3 mb-1">
              <h1 className="text-2xl font-bold text-text-primary">{project.name}</h1>
              <Badge variant={project.environment === "production" ? "destructive" : "secondary"}>
                {project.environment}
              </Badge>
            </div>
            {project.description && (
              <p className="text-sm text-text-secondary">{project.description}</p>
            )}
          </div>
        </div>

        <Button variant="outline" onClick={() => router.push(`/projects/${projectId}/settings`)}>
          <Settings className="w-4 h-4 mr-2" />
          설정
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatCard
          icon={AlertCircle}
          label="총 에러 수"
          value={formatNumber(project.errorCount)}
          iconColor="text-error"
        />
        <StatCard
          icon={TrendingDown}
          label="마지막 에러"
          value={project.lastErrorAt ? formatRelativeTime(project.lastErrorAt) : "없음"}
          iconColor="text-warning"
        />
        <StatCard
          icon={Users}
          label="생성일"
          value={new Date(project.createdAt).toLocaleDateString()}
          iconColor="text-primary"
        />
      </div>

      {/* API Key Section */}
      <div className="bg-bg-secondary rounded-lg p-6 border border-border">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-text-primary">API 키</h2>
          <Button variant="outline" size="sm" onClick={handleRegenerateKey}>
            <RefreshCw className="w-4 h-4 mr-2" />
            재생성
          </Button>
        </div>

        <div className="flex items-center gap-2 bg-bg-tertiary p-4 rounded-lg border border-border">
          <code className="flex-1 font-mono text-sm text-text-primary break-all">
            {project.apiKey}
          </code>
          <Button variant="ghost" size="sm" onClick={handleCopyApiKey}>
            {copiedKey ? (
              <>
                <Copy className="w-4 h-4 mr-2 text-success" />
                복사됨
              </>
            ) : (
              <>
                <Copy className="w-4 h-4 mr-2" />
                복사
              </>
            )}
          </Button>
        </div>

        <p className="text-sm text-text-secondary mt-3">
          이 API 키를 사용하여 ErrorWatch SDK를 초기화하세요.
        </p>
      </div>

      {/* Recent Errors */}
      <div className="bg-bg-secondary rounded-lg p-6 border border-border">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-text-primary">최근 미해결 에러</h2>
          <Button
            variant="outline"
            size="sm"
            onClick={() => router.push(`/errors?projectId=${projectId}`)}
          >
            전체 보기
          </Button>
        </div>

        {recentErrors.length === 0 ? (
          <p className="text-center text-text-secondary py-8">
            미해결 에러가 없습니다.
          </p>
        ) : (
          <div className="space-y-3">
            {recentErrors.map((error) => (
              <div
                key={error.id}
                className="flex items-start justify-between p-4 bg-bg-tertiary rounded-lg border border-border hover:border-primary cursor-pointer transition-colors"
                onClick={() => router.push(`/errors/${error.id}`)}
              >
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <Badge variant={
                      error.severity === "CRITICAL" ? "destructive" :
                      error.severity === "HIGH" ? "destructive" :
                      error.severity === "MEDIUM" ? "default" : "secondary"
                    }>
                      {error.severity}
                    </Badge>
                    <span className="text-sm font-medium text-text-primary">
                      {error.type}
                    </span>
                  </div>
                  <p className="text-sm text-text-secondary line-clamp-1">
                    {error.message}
                  </p>
                  <div className="flex items-center gap-3 mt-2 text-xs text-text-secondary">
                    <span>{error.occurrenceCount}회 발생</span>
                    <span>•</span>
                    <span>{formatRelativeTime(error.lastSeenAt)}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* SDK Integration Guide */}
      <div className="bg-bg-secondary rounded-lg p-6 border border-border">
        <h2 className="text-lg font-semibold text-text-primary mb-4">SDK 통합 가이드</h2>

        <div className="space-y-4">
          <div>
            <h3 className="text-sm font-semibold text-text-primary mb-2">JavaScript</h3>
            <pre className="bg-bg-tertiary p-4 rounded-lg border border-border overflow-x-auto">
              <code className="text-sm text-text-primary">{`import ErrorWatch from '@errorwatch/browser-sdk';

ErrorWatch.init({
  apiKey: '${project.apiKey}',
  environment: '${project.environment}',
  release: '1.0.0'
});`}</code>
            </pre>
          </div>

          <div>
            <h3 className="text-sm font-semibold text-text-primary mb-2">React</h3>
            <pre className="bg-bg-tertiary p-4 rounded-lg border border-border overflow-x-auto">
              <code className="text-sm text-text-primary">{`import { ErrorBoundary } from '@errorwatch/react';

<ErrorBoundary apiKey="${project.apiKey}">
  <App />
</ErrorBoundary>`}</code>
            </pre>
          </div>
        </div>
      </div>
    </motion.div>
  );
}

function StatCard({
  icon: Icon,
  label,
  value,
  iconColor,
}: {
  icon: any;
  label: string;
  value: string | number;
  iconColor: string;
}) {
  return (
    <div className="bg-bg-secondary rounded-lg p-6 border border-border">
      <div className="flex items-center gap-4">
        <div className={`p-3 bg-opacity-10 rounded-lg ${iconColor.replace('text-', 'bg-')}`}>
          <Icon className={`w-6 h-6 ${iconColor}`} />
        </div>
        <div>
          <p className="text-sm text-text-secondary mb-1">{label}</p>
          <p className="text-2xl font-bold text-text-primary">{value}</p>
        </div>
      </div>
    </div>
  );
}
