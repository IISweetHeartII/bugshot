"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { AlertCircle, TrendingUp, Users, Clock } from "lucide-react";
import { formatNumber, formatRelativeTime, getSeverityEmoji, getSeverityBadgeVariant } from "@/lib/utils";
import { PAGINATION, MESSAGES } from "@/lib/constants";
import { motion } from "framer-motion";
import { Badge } from "@/components/ui/badge";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { AnimatedStatCard } from "@/components/ui/stat-card";
import { toast } from "sonner";
import type { DashboardStatsResponse, ErrorResponse } from "@/types/api";

export default function DashboardPage() {
  const router = useRouter();
  const [stats, setStats] = useState<DashboardStatsResponse | null>(null);
  const [recentErrors, setRecentErrors] = useState<ErrorResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedProject, setSelectedProject] = useState<string>("all");
  const [period, setPeriod] = useState<string>("7d");

  useEffect(() => {
    loadDashboard();
  }, [selectedProject, period]);

  const loadDashboard = async () => {
    try {
      setLoading(true);

      // selectedProject가 "all"이면 모든 프로젝트 통계를 합산
      const projectId = selectedProject === "all" ? "all" : selectedProject;

      const [statsData, errorsData] = await Promise.all([
        api.getDashboardStats(projectId, period),
        api.getErrors({
          // 에러 목록은 프로젝트 필터 없이 전체 조회 (selectedProject가 "all"인 경우)
          projectId: projectId === "all" ? undefined : projectId,
          page: PAGINATION.DEFAULT_PAGE,
          size: PAGINATION.DASHBOARD_ERRORS_SIZE,
          sort: "priority",
          status: "UNRESOLVED"
        }),
      ]);

      setStats(statsData);
      setRecentErrors(errorsData.data);
    } catch (error) {
      console.error("Failed to load dashboard:", error);
      toast.error(MESSAGES.ERROR.LOAD_DASHBOARD);
    } finally {
      setLoading(false);
    }
  };

  const handleErrorClick = (errorId: string) => {
    router.push(`/errors/${errorId}`);
  };

  if (loading) {
    return <LoadingSpinner message={MESSAGES.LOADING.DASHBOARD} />;
  }

  return (
    <motion.div
      className="space-y-6"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.5 }}
    >
      {/* Stats Cards */}
      <motion.div
        className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6"
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
          title="총 에러 수"
          value={formatNumber(stats?.totalErrors || 0)}
          icon={AlertCircle}
          iconColor="text-error"
        />
        <AnimatedStatCard
          title="영향받은 사용자"
          value={formatNumber(stats?.affectedUsers || 0)}
          icon={Users}
          iconColor="text-warning"
        />
        <AnimatedStatCard
          title="오늘 에러"
          value={formatNumber(stats?.todayErrors || 0)}
          icon={Clock}
          iconColor="text-info"
        />
        <AnimatedStatCard
          title="에러 추세"
          value={`${stats?.changeRate || 0}%`}
          icon={TrendingUp}
          iconColor={stats && stats.changeRate < 0 ? "text-success" : "text-error"}
          subtitle="어제 대비"
        />
      </motion.div>

      {/* Recent Errors */}
      <motion.div
        className="bg-bg-secondary rounded-xl p-6 border border-bg-primary"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
      >
        <h3 className="text-lg font-semibold text-text-primary mb-4">
          최근 에러 (우선순위순)
        </h3>

        {recentErrors.length === 0 ? (
          <div className="text-center py-12 text-text-muted">
            아직 에러가 없습니다.
          </div>
        ) : (
          <motion.div
            className="space-y-3"
            initial="hidden"
            animate="visible"
            variants={{
              visible: {
                transition: {
                  staggerChildren: 0.05
                }
              }
            }}
          >
            {recentErrors.map((error) => (
              <motion.div
                key={error.id}
                className="flex items-center gap-4 p-4 bg-bg-primary rounded-lg hover:bg-bg-tertiary transition-colors cursor-pointer"
                onClick={() => handleErrorClick(error.id)}
                variants={{
                  hidden: { opacity: 0, x: -20 },
                  visible: { opacity: 1, x: 0 }
                }}
                whileHover={{ scale: 1.01, x: 5 }}
                whileTap={{ scale: 0.99 }}
                role="button"
                tabIndex={0}
                aria-label={`${error.errorType} 에러 상세 보기`}
                onKeyDown={(e) => e.key === "Enter" && handleErrorClick(error.id)}
              >
                <div className="text-2xl" aria-hidden="true">
                  {getSeverityEmoji(error.severity)}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-medium text-text-primary truncate">
                      {error.errorType}
                    </span>
                    <Badge variant={getSeverityBadgeVariant(error.severity)}>
                      {error.severity}
                    </Badge>
                  </div>
                  <div className="flex items-center gap-4 text-sm text-text-muted">
                    <span>{error.occurrenceCount}회 발생</span>
                    <span aria-hidden="true">•</span>
                    <span>{error.affectedUsersCount}명 영향</span>
                    <span aria-hidden="true">•</span>
                    <span>{formatRelativeTime(error.lastSeenAt)}</span>
                  </div>
                </div>
              </motion.div>
            ))}
          </motion.div>
        )}
      </motion.div>
    </motion.div>
  );
}
