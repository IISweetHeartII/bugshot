"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { AlertCircle, TrendingUp, Users, Clock, Activity } from "lucide-react";
import { formatNumber, formatRelativeTime, getSeverityEmoji } from "@/lib/utils";
import { motion } from "framer-motion";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import type { DashboardStatsResponse, ErrorResponse } from "@/types/api";

export default function DashboardPage() {
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

      // TODO: 실제 프로젝트 ID 사용 (현재는 임시로 첫 번째 프로젝트 사용)
      const projects = await api.getProjects();
      const projectId = projects[0]?.id || "default";

      // 실제 대시보드 통계 API 호출
      const [statsData, errorsData] = await Promise.all([
        api.getDashboardStats(projectId, period),
        api.getErrors({
          projectId,
          page: 0,
          size: 10,
          sort: "priority",
          status: "UNRESOLVED"
        }),
      ]);

      setStats(statsData);
      setRecentErrors(errorsData.data);
    } catch (error) {
      console.error("Failed to load dashboard:", error);
      toast.error("대시보드 데이터를 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-text-secondary">데이터를 불러오는 중...</div>
      </div>
    );
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
        <StatCard
          title="총 에러 수"
          value={formatNumber(stats?.totalErrors || 0)}
          icon={AlertCircle}
          iconColor="text-error"
        />
        <StatCard
          title="영향받은 사용자"
          value={formatNumber(stats?.affectedUsers || 0)}
          icon={Users}
          iconColor="text-warning"
        />
        <StatCard
          title="마지막 에러"
          value={stats?.lastErrorTime ? formatRelativeTime(stats.lastErrorTime) : "N/A"}
          icon={Clock}
          iconColor="text-info"
        />
        <StatCard
          title="에러 추세"
          value={`${stats?.trend || 0}%`}
          icon={TrendingUp}
          iconColor={stats && stats.trend < 0 ? "text-success" : "text-error"}
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
                onClick={() => (window.location.href = `/errors/${error.id}`)}
                variants={{
                  hidden: { opacity: 0, x: -20 },
                  visible: { opacity: 1, x: 0 }
                }}
                whileHover={{ scale: 1.02, x: 5 }}
                whileTap={{ scale: 0.98 }}
              >
                <motion.div
                  className="text-2xl"
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  transition={{ type: "spring", delay: 0.2 }}
                >
                  {getSeverityEmoji(error.severity)}
                </motion.div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-medium text-text-primary truncate">
                      {error.errorType}
                    </span>
                    <Badge
                      variant={
                        error.severity === "CRITICAL" ? "critical" :
                        error.severity === "HIGH" ? "high" :
                        error.severity === "MEDIUM" ? "medium" : "low"
                      }
                    >
                      {error.severity}
                    </Badge>
                  </div>
                  <div className="flex items-center gap-4 text-sm text-text-muted">
                    <span>{error.occurrenceCount}회 발생</span>
                    <span>•</span>
                    <span>{error.affectedUsersCount}명 영향</span>
                    <span>•</span>
                    <span>{formatRelativeTime(error.lastOccurredAt)}</span>
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

function StatCard({
  title,
  value,
  icon: Icon,
  iconColor,
  subtitle,
}: {
  title: string;
  value: string;
  icon: React.ElementType;
  iconColor: string;
  subtitle?: string;
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
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-text-secondary text-sm font-medium">{title}</h3>
        <motion.div
          whileHover={{ rotate: 360 }}
          transition={{ duration: 0.6 }}
        >
          <Icon className={`w-5 h-5 ${iconColor}`} />
        </motion.div>
      </div>
      <motion.div
        className="text-3xl font-bold text-text-primary mb-1"
        initial={{ scale: 0 }}
        animate={{ scale: 1 }}
        transition={{ type: "spring", delay: 0.2 }}
      >
        {value}
      </motion.div>
      {subtitle && <p className="text-xs text-text-muted">{subtitle}</p>}
    </motion.div>
  );
}
