"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { Plus, Trash2, RefreshCw, Copy, Check } from "lucide-react";
import { formatRelativeTime, copyToClipboard } from "@/lib/utils";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { CreateProjectDialog } from "@/components/project-create-dialog";
import { toast } from "sonner";
import type { ProjectResponse } from "@/types/api";

export default function ProjectsPage() {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [copiedKey, setCopiedKey] = useState<string | null>(null);

  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = async () => {
    try {
      setLoading(true);
      const data = await api.getProjects();
      setProjects(data);
    } catch (error) {
      console.error("Failed to load projects:", error);
      toast.error("프로젝트를 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleCopyApiKey = async (apiKey: string) => {
    const success = await copyToClipboard(apiKey);
    if (success) {
      setCopiedKey(apiKey);
      setTimeout(() => setCopiedKey(null), 2000);
      toast.success("API 키가 복사되었습니다!");
    }
  };

  const handleRegenerateKey = async (id: string) => {
    if (!confirm("API 키를 재생성하시겠습니까? 기존 키는 사용할 수 없게 됩니다.")) {
      return;
    }

    try {
      const result = await api.regenerateApiKey(id);
      await loadProjects();
      toast.success("API 키가 재생성되었습니다!");
      // 새 키 자동 복사
      if (result.apiKey) {
        await handleCopyApiKey(result.apiKey);
      }
    } catch (error) {
      console.error("Failed to regenerate API key:", error);
      toast.error("API 키 재생성에 실패했습니다.");
    }
  };

  const handleDeleteProject = async (id: string, name: string) => {
    if (!confirm(`"${name}" 프로젝트를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) {
      return;
    }

    try {
      await api.deleteProject(id);
      await loadProjects();
      toast.success("프로젝트가 삭제되었습니다.");
    } catch (error) {
      console.error("Failed to delete project:", error);
      toast.error("프로젝트 삭제에 실패했습니다.");
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-text-secondary">프로젝트를 불러오는 중...</div>
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
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
        >
          <h2 className="text-2xl font-bold text-text-primary">프로젝트</h2>
          <p className="text-text-secondary mt-1">
            총 {projects.length}개의 프로젝트
          </p>
        </motion.div>
        <motion.div
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
        >
          <Button onClick={() => setShowCreateModal(true)}>
            <Plus className="w-5 h-5" />
            새 프로젝트
          </Button>
        </motion.div>
      </div>

      {/* Projects Grid */}
      {projects.length === 0 ? (
        <motion.div
          className="bg-bg-secondary rounded-xl p-12 text-center border border-bg-primary"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
        >
          <motion.div
            className="text-6xl mb-4"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: "spring", delay: 0.2 }}
          >
            📦
          </motion.div>
          <h3 className="text-xl font-semibold text-text-primary mb-2">
            프로젝트가 없습니다
          </h3>
          <p className="text-text-secondary mb-6">
            첫 번째 프로젝트를 생성하고 에러 모니터링을 시작하세요.
          </p>
          <Button onClick={() => setShowCreateModal(true)}>
            <Plus className="w-5 h-5" />
            프로젝트 생성
          </Button>
        </motion.div>
      ) : (
        <motion.div
          className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
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
          {projects.map((project) => (
            <motion.div
              key={project.id}
              className="bg-bg-secondary rounded-xl p-6 border border-bg-primary"
              variants={{
                hidden: { opacity: 0, y: 20 },
                visible: { opacity: 1, y: 0 }
              }}
              whileHover={{ y: -5, borderColor: "#5865F2" }}
              transition={{ type: "spring", stiffness: 300 }}
            >
              {/* Project Header */}
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1 min-w-0">
                  <h3 className="text-lg font-semibold text-text-primary truncate">
                    {project.name}
                  </h3>
                  <p className="text-sm text-text-muted">
                    {formatRelativeTime(project.createdAt)}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <motion.button
                    onClick={() => handleRegenerateKey(project.id)}
                    className="p-2 hover:bg-bg-primary rounded-lg transition-colors"
                    title="API 키 재생성"
                    whileHover={{ scale: 1.1 }}
                    whileTap={{ scale: 0.95 }}
                  >
                    <RefreshCw className="w-4 h-4 text-text-muted" />
                  </motion.button>
                  <motion.button
                    onClick={() => handleDeleteProject(project.id, project.name)}
                    className="p-2 hover:bg-bg-primary rounded-lg transition-colors"
                    title="프로젝트 삭제"
                    whileHover={{ scale: 1.1 }}
                    whileTap={{ scale: 0.95 }}
                  >
                    <Trash2 className="w-4 h-4 text-error" />
                  </motion.button>
                </div>
              </div>

              {/* Description */}
              {project.description && (
                <p className="text-sm text-text-secondary mb-4 line-clamp-2">
                  {project.description}
                </p>
              )}

              {/* Environment Badge */}
              <div className="mb-4">
                <Badge
                  variant={
                    project.environment === "PRODUCTION"
                      ? "destructive"
                      : project.environment === "STAGING"
                      ? "secondary"
                      : "default"
                  }
                >
                  {project.environment}
                </Badge>
              </div>

              {/* API Key */}
              <div className="bg-bg-tertiary rounded-lg p-3">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-medium text-text-muted">
                    API KEY
                  </span>
                  <motion.button
                    onClick={() => handleCopyApiKey(project.apiKey)}
                    className="text-text-muted hover:text-text-primary transition-colors"
                    whileHover={{ scale: 1.1 }}
                    whileTap={{ scale: 0.95 }}
                  >
                    {copiedKey === project.apiKey ? (
                      <Check className="w-4 h-4 text-success" />
                    ) : (
                      <Copy className="w-4 h-4" />
                    )}
                  </motion.button>
                </div>
                <code className="text-xs text-text-secondary font-mono break-all">
                  {project.apiKey}
                </code>
              </div>

              {/* View Button */}
              <Button
                onClick={() => (window.location.href = `/projects/${project.id}`)}
                variant="secondary"
                className="w-full mt-4"
              >
                프로젝트 설정
              </Button>
            </motion.div>
          ))}
        </motion.div>
      )}

      {/* Create Dialog */}
      <CreateProjectDialog
        open={showCreateModal}
        onOpenChange={setShowCreateModal}
        onSuccess={() => {
          setShowCreateModal(false);
          loadProjects();
        }}
      />
    </motion.div>
  );
}
