"use client";

import { useEffect, useState, useMemo } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { Search, X } from "lucide-react";
import {
  formatRelativeTime,
  getSeverityEmoji,
  getSeverityBadgeVariant,
  formatNumber,
} from "@/lib/utils";
import { PAGINATION, MESSAGES, SEVERITY_FILTER_OPTIONS, STATUS_FILTER_OPTIONS } from "@/lib/constants";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { FilterSelect } from "@/components/ui/select";
import { toast } from "sonner";
import type { ErrorResponse } from "@/types/api";

interface ErrorFilters {
  severity: string;
  status: string;
  search: string;
}

export default function ErrorsPage() {
  const router = useRouter();
  const [errors, setErrors] = useState<ErrorResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState<ErrorFilters>({
    severity: "ALL",
    status: "UNRESOLVED",
    search: "",
  });
  const [page, setPage] = useState<number>(PAGINATION.DEFAULT_PAGE);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedProject, setSelectedProject] = useState<string>("all");

  useEffect(() => {
    loadErrors();
  }, [filters.severity, filters.status, page, selectedProject]);

  const loadErrors = async () => {
    try {
      setLoading(true);
      const params: {
        page: number;
        size: number;
        sort: string;
        projectId?: string;
        severity?: string;
        status?: string;
      } = {
        page,
        size: PAGINATION.DEFAULT_SIZE,
        sort: "priority",
      };

      if (selectedProject !== "all") {
        params.projectId = selectedProject;
      }

      if (filters.severity !== "ALL") {
        params.severity = filters.severity;
      }

      if (filters.status !== "ALL") {
        params.status = filters.status;
      }

      const data = await api.getErrors(params);
      setErrors(data.data);
      setTotalPages(data.pagination.totalPages);
    } catch (error) {
      console.error("Failed to load errors:", error);
      toast.error(MESSAGES.ERROR.LOAD_ERRORS);
    } finally {
      setLoading(false);
    }
  };

  const filteredErrors = useMemo(() => {
    if (!filters.search) return errors;
    const searchLower = filters.search.toLowerCase();
    return errors.filter(
      (error) =>
        error.errorType.toLowerCase().includes(searchLower) ||
        error.errorMessage.toLowerCase().includes(searchLower)
    );
  }, [errors, filters.search]);

  const handleErrorClick = (errorId: string) => {
    router.push(`/errors/${errorId}`);
  };

  const handleFilterChange = (key: keyof ErrorFilters, value: string) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    if (key !== "search") {
      setPage(PAGINATION.DEFAULT_PAGE);
    }
  };

  return (
    <motion.div
      className="space-y-6"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
    >
      {/* Header */}
      <motion.div
        className="flex items-center justify-between"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div>
          <h2 className="text-2xl font-bold text-text-primary">에러 목록</h2>
          <p className="text-text-secondary mt-1">
            총 {formatNumber(filteredErrors.length)}개의 에러
          </p>
        </div>
      </motion.div>

      {/* Filters */}
      <motion.div
        className="bg-bg-secondary rounded-xl p-4 border border-bg-primary"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Search */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-text-muted" aria-hidden="true" />
            <input
              type="text"
              value={filters.search}
              onChange={(e) => handleFilterChange("search", e.target.value)}
              className="w-full bg-bg-tertiary border border-bg-primary rounded-lg pl-10 pr-10 py-2 text-text-primary focus:outline-none focus:border-primary"
              placeholder="에러 검색..."
              aria-label="에러 검색"
            />
            {filters.search && (
              <button
                onClick={() => handleFilterChange("search", "")}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-primary"
                aria-label="검색어 지우기"
              >
                <X className="w-5 h-5" aria-hidden="true" />
              </button>
            )}
          </div>

          {/* Severity Filter */}
          <FilterSelect
            value={filters.severity}
            onChange={(value) => handleFilterChange("severity", value)}
            options={SEVERITY_FILTER_OPTIONS}
            aria-label="심각도 필터"
          />

          {/* Status Filter */}
          <FilterSelect
            value={filters.status}
            onChange={(value) => handleFilterChange("status", value)}
            options={STATUS_FILTER_OPTIONS}
            aria-label="상태 필터"
          />
        </div>
      </motion.div>

      {/* Errors List */}
      {loading ? (
        <LoadingSpinner message={MESSAGES.LOADING.ERRORS} />
      ) : filteredErrors.length === 0 ? (
        <motion.div
          className="bg-bg-secondary rounded-xl p-12 text-center border border-bg-primary"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
        >
          <div className="text-6xl mb-4" role="img" aria-label="깨끗함">
            ✨
          </div>
          <h3 className="text-xl font-semibold text-text-primary mb-2">
            에러가 없습니다
          </h3>
          <p className="text-text-secondary">
            현재 조건에 맞는 에러가 없습니다.
          </p>
        </motion.div>
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
          {filteredErrors.map((error) => (
            <motion.div
              key={error.id}
              className="bg-bg-secondary rounded-xl p-6 border border-bg-primary cursor-pointer hover:border-primary transition-colors"
              onClick={() => handleErrorClick(error.id)}
              variants={{
                hidden: { opacity: 0, x: -20 },
                visible: { opacity: 1, x: 0 }
              }}
              whileHover={{ x: 5 }}
              whileTap={{ scale: 0.99 }}
              role="button"
              tabIndex={0}
              aria-label={`${error.errorType} 에러 상세 보기`}
              onKeyDown={(e) => e.key === "Enter" && handleErrorClick(error.id)}
            >
              <div className="flex items-start gap-4">
                {/* Severity Emoji */}
                <div className="text-3xl shrink-0" aria-hidden="true">
                  {getSeverityEmoji(error.severity)}
                </div>

                {/* Error Info */}
                <div className="flex-1 min-w-0">
                  {/* Header */}
                  <div className="flex items-start justify-between gap-4 mb-2">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1 flex-wrap">
                        <h3 className="text-lg font-semibold text-text-primary truncate">
                          {error.errorType}
                        </h3>
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
                      <p className="text-sm text-text-secondary line-clamp-2">
                        {error.errorMessage}
                      </p>
                    </div>

                    {/* Priority Score */}
                    <div className="text-right shrink-0">
                      <div className="text-2xl font-bold text-primary">
                        {Math.round(error.priorityScore)}
                      </div>
                      <div className="text-xs text-text-muted">우선순위</div>
                    </div>
                  </div>

                  {/* Location */}
                  {error.filePath && (
                    <div className="mb-3">
                      <code className="text-xs bg-bg-tertiary px-2 py-1 rounded text-text-secondary font-mono">
                        {error.filePath}
                        {error.lineNumber && `:${error.lineNumber}`}
                      </code>
                    </div>
                  )}

                  {/* Stats */}
                  <div className="flex items-center gap-6 text-sm text-text-muted flex-wrap">
                    <div>
                      <span className="font-medium text-text-primary">
                        {formatNumber(error.occurrenceCount)}회
                      </span>{" "}
                      발생
                    </div>
                    <div>
                      <span className="font-medium text-text-primary">
                        {formatNumber(error.affectedUsersCount)}명
                      </span>{" "}
                      영향
                    </div>
                    <div>마지막: {formatRelativeTime(error.lastSeenAt)}</div>
                    <div>처음: {formatRelativeTime(error.firstSeenAt)}</div>
                  </div>
                </div>
              </div>
            </motion.div>
          ))}
        </motion.div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <motion.div
          className="flex items-center justify-center gap-2"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.3 }}
          role="navigation"
          aria-label="페이지 네비게이션"
        >
          <Button
            onClick={() => setPage(Math.max(0, page - 1))}
            disabled={page === 0}
            variant="outline"
          >
            이전
          </Button>
          <span className="text-text-secondary">
            {page + 1} / {totalPages}
          </span>
          <Button
            onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
            disabled={page >= totalPages - 1}
            variant="outline"
          >
            다음
          </Button>
        </motion.div>
      )}
    </motion.div>
  );
}
