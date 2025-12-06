"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { api } from "@/lib/api";
import {
  ArrowLeft,
  CheckCircle,
  XCircle,
  RotateCcw,
  Users,
  Clock,
  MapPin,
} from "lucide-react";
import {
  formatRelativeTime,
  getSeverityEmoji,
  getSeverityBadgeVariant,
  formatNumber,
} from "@/lib/utils";
import { MESSAGES } from "@/lib/constants";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { AnimatedStatCard } from "@/components/ui/stat-card";
import { toast } from "sonner";
import type { ErrorResponse } from "@/types/api";

export default function ErrorDetailPage() {
  const params = useParams();
  const router = useRouter();
  const [error, setError] = useState<ErrorResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (params.id) {
      loadError(params.id as string);
    }
  }, [params.id]);

  const loadError = async (id: string) => {
    try {
      setLoading(true);
      const data = await api.getError(id);
      setError(data);
    } catch (err) {
      console.error("Failed to load error:", err);
      toast.error(MESSAGES.ERROR.LOAD_ERROR);
      router.push("/errors");
    } finally {
      setLoading(false);
    }
  };

  const handleResolve = async () => {
    if (!error) return;

    try {
      await api.resolveError(error.id);
      await loadError(error.id);
      toast.success(MESSAGES.SUCCESS.ERROR_RESOLVED);
    } catch (err) {
      console.error("Failed to resolve error:", err);
      toast.error(MESSAGES.ERROR.RESOLVE_ERROR);
    }
  };

  const handleIgnore = async () => {
    if (!error) return;

    try {
      await api.ignoreError(error.id);
      await loadError(error.id);
      toast.success(MESSAGES.SUCCESS.ERROR_IGNORED);
    } catch (err) {
      console.error("Failed to ignore error:", err);
      toast.error(MESSAGES.ERROR.IGNORE_ERROR);
    }
  };

  const handleReopen = async () => {
    if (!error) return;

    try {
      await api.reopenError(error.id);
      await loadError(error.id);
      toast.success(MESSAGES.SUCCESS.ERROR_REOPENED);
    } catch (err) {
      console.error("Failed to reopen error:", err);
      toast.error(MESSAGES.ERROR.REOPEN_ERROR);
    }
  };

  if (loading) {
    return <LoadingSpinner message={MESSAGES.LOADING.ERRORS} />;
  }

  if (!error) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-text-secondary">에러를 찾을 수 없습니다.</div>
      </div>
    );
  }

  return (
    <motion.div
      className="space-y-6"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
    >
      {/* Back Button */}
      <motion.div
        initial={{ opacity: 0, x: -20 }}
        animate={{ opacity: 1, x: 0 }}
      >
        <Button
          onClick={() => router.back()}
          variant="ghost"
          className="gap-2"
        >
          <ArrowLeft className="w-5 h-5" aria-hidden="true" />
          목록으로 돌아가기
        </Button>
      </motion.div>

      {/* Header */}
      <motion.div
        className="bg-bg-secondary rounded-xl p-6 border border-bg-primary"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <div className="flex items-start gap-4">
          <div className="text-4xl" aria-hidden="true">{getSeverityEmoji(error.severity)}</div>
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2 flex-wrap">
              <h1 className="text-2xl font-bold text-text-primary">
                {error.errorType}
              </h1>
              <Badge variant={getSeverityBadgeVariant(error.severity)}>
                {error.severity}
              </Badge>
              {error.status === "RESOLVED" && (
                <Badge variant="default" className="bg-success/20 text-success border-success/30">
                  ✓ 해결됨
                </Badge>
              )}
              {error.status === "IGNORED" && (
                <Badge variant="secondary">
                  무시됨
                </Badge>
              )}
            </div>
            <p className="text-text-secondary mb-4">{error.errorMessage}</p>

            {/* Actions */}
            <div className="flex gap-3">
              {error.status === "UNRESOLVED" ? (
                <>
                  <Button
                    onClick={handleResolve}
                    className="bg-success hover:bg-success/80 text-white"
                  >
                    <CheckCircle className="w-5 h-5" aria-hidden="true" />
                    해결
                  </Button>
                  <Button
                    onClick={handleIgnore}
                    variant="outline"
                  >
                    <XCircle className="w-5 h-5" aria-hidden="true" />
                    무시
                  </Button>
                </>
              ) : (
                <Button onClick={handleReopen}>
                  <RotateCcw className="w-5 h-5" aria-hidden="true" />
                  재오픈
                </Button>
              )}
            </div>
          </div>

          {/* Priority Score */}
          <motion.div
            className="text-center"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: "spring", delay: 0.3 }}
          >
            <div className="text-4xl font-bold text-primary">
              {Math.round(error.priorityScore)}
            </div>
            <div className="text-sm text-text-muted">우선순위</div>
          </motion.div>
        </div>
      </motion.div>

      {/* Stats Grid */}
      <motion.div
        className="grid grid-cols-1 md:grid-cols-4 gap-6"
        initial="hidden"
        animate="visible"
        variants={{
          visible: {
            transition: {
              staggerChildren: 0.1
            }
          }
        }}
      >
        <AnimatedStatCard
          title="발생 횟수"
          value={formatNumber(error.occurrenceCount) + "회"}
          icon={Clock}
        />
        <AnimatedStatCard
          title="영향받은 사용자"
          value={formatNumber(error.affectedUsersCount) + "명"}
          icon={Users}
        />
        <AnimatedStatCard
          title="처음 발생"
          value={formatRelativeTime(error.firstSeenAt)}
          icon={Clock}
        />
        <AnimatedStatCard
          title="마지막 발생"
          value={formatRelativeTime(error.lastSeenAt)}
          icon={Clock}
        />
      </motion.div>

      {/* Location */}
      {error.filePath && (
        <motion.div
          className="bg-bg-secondary rounded-xl p-6 border border-bg-primary"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
        >
          <h3 className="text-lg font-semibold text-text-primary mb-4 flex items-center gap-2">
            <MapPin className="w-5 h-5" aria-hidden="true" />
            위치
          </h3>
          <div className="space-y-2">
            <div>
              <span className="text-text-muted">파일:</span>{" "}
              <code className="text-text-primary bg-bg-tertiary px-2 py-1 rounded font-mono text-sm">
                {error.filePath}
              </code>
            </div>
            {error.lineNumber && (
              <div>
                <span className="text-text-muted">라인:</span>{" "}
                <code className="text-text-primary bg-bg-tertiary px-2 py-1 rounded font-mono text-sm">
                  {error.lineNumber}
                </code>
              </div>
            )}
            {error.methodName && (
              <div>
                <span className="text-text-muted">메서드:</span>{" "}
                <code className="text-text-primary bg-bg-tertiary px-2 py-1 rounded font-mono text-sm">
                  {error.methodName}
                </code>
              </div>
            )}
          </div>
        </motion.div>
      )}

      {/* Stack Trace */}
      {error.stackTrace && (
        <motion.div
          className="bg-bg-secondary rounded-xl p-6 border border-bg-primary"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
        >
          <h3 className="text-lg font-semibold text-text-primary mb-4">
            스택 트레이스
          </h3>
          <pre className="bg-bg-tertiary p-4 rounded-lg overflow-x-auto text-sm text-text-secondary font-mono whitespace-pre-wrap">
            {error.stackTrace}
          </pre>
        </motion.div>
      )}

      {/* Resolved Info */}
      {error.status === "RESOLVED" && error.resolvedAt && (
        <motion.div
          className="bg-success/10 border border-success/20 rounded-xl p-6"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
        >
          <h3 className="text-lg font-semibold text-success mb-2">
            ✓ 해결됨
          </h3>
          <p className="text-text-secondary">
            {formatRelativeTime(error.resolvedAt)}에 해결됨
          </p>
        </motion.div>
      )}
    </motion.div>
  );
}
