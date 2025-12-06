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
  Play,
} from "lucide-react";
import {
  formatRelativeTime,
  getSeverityColor,
  getSeverityEmoji,
  formatNumber,
} from "@/lib/utils";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
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
      toast.error("에러를 불러오는데 실패했습니다.");
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
      toast.success("에러가 해결됨으로 표시되었습니다.");
    } catch (err) {
      console.error("Failed to resolve error:", err);
      toast.error("에러 처리에 실패했습니다.");
    }
  };

  const handleIgnore = async () => {
    if (!error) return;

    try {
      await api.ignoreError(error.id);
      await loadError(error.id);
      toast.success("에러가 무시됨으로 표시되었습니다.");
    } catch (err) {
      console.error("Failed to ignore error:", err);
      toast.error("에러 처리에 실패했습니다.");
    }
  };

  const handleReopen = async () => {
    if (!error) return;

    try {
      await api.reopenError(error.id);
      await loadError(error.id);
      toast.success("에러가 재오픈되었습니다.");
    } catch (err) {
      console.error("Failed to reopen error:", err);
      toast.error("에러 처리에 실패했습니다.");
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-text-secondary">에러 정보를 불러오는 중...</div>
      </div>
    );
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
          <ArrowLeft className="w-5 h-5" />
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
          <div className="text-4xl">{getSeverityEmoji(error.severity)}</div>
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <h1 className="text-2xl font-bold text-text-primary">
                {error.errorType}
              </h1>
              <Badge
                variant={
                  error.severity === "CRITICAL" ? "critical" :
                  error.severity === "HIGH" ? "high" :
                  error.severity === "MEDIUM" ? "medium" : "low"
                }
              >
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
                    <CheckCircle className="w-5 h-5" />
                    해결
                  </Button>
                  <Button
                    onClick={handleIgnore}
                    variant="outline"
                  >
                    <XCircle className="w-5 h-5" />
                    무시
                  </Button>
                </>
              ) : (
                <Button onClick={handleReopen}>
                  <RotateCcw className="w-5 h-5" />
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
        <StatCard
          icon={Clock}
          label="발생 횟수"
          value={formatNumber(error.occurrenceCount) + "회"}
        />
        <StatCard
          icon={Users}
          label="영향받은 사용자"
          value={formatNumber(error.affectedUsersCount) + "명"}
        />
        <StatCard
          icon={Clock}
          label="처음 발생"
          value={formatRelativeTime(error.firstSeenAt)}
        />
        <StatCard
          icon={Clock}
          label="마지막 발생"
          value={formatRelativeTime(error.lastSeenAt)}
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
            <MapPin className="w-5 h-5" />
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

function StatCard({
  icon: Icon,
  label,
  value,
}: {
  icon: React.ElementType;
  label: string;
  value: string;
}) {
  return (
    <motion.div
      className="bg-bg-secondary rounded-xl p-6 border border-bg-primary"
      variants={{
        hidden: { opacity: 0, y: 20 },
        visible: { opacity: 1, y: 0 }
      }}
      whileHover={{ y: -5, borderColor: "#5865F2" }}
      transition={{ type: "spring", stiffness: 300 }}
    >
      <div className="flex items-center justify-between mb-3">
        <motion.div
          whileHover={{ rotate: 360 }}
          transition={{ duration: 0.6 }}
        >
          <Icon className="w-5 h-5 text-text-muted" />
        </motion.div>
      </div>
      <motion.div
        className="text-2xl font-bold text-text-primary mb-1"
        initial={{ scale: 0 }}
        animate={{ scale: 1 }}
        transition={{ type: "spring", delay: 0.2 }}
      >
        {value}
      </motion.div>
      <div className="text-sm text-text-muted">{label}</div>
    </motion.div>
  );
}
