"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { Plus, Trash2, TestTube, Edit, Check, X } from "lucide-react";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import type { WebhookConfigResponse, WebhookConfigRequest } from "@/types/api";

export default function WebhooksPage() {
  const [webhooks, setWebhooks] = useState<WebhookConfigResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingWebhook, setEditingWebhook] = useState<WebhookConfigResponse | null>(null);
  const [selectedProject, setSelectedProject] = useState<string>("");

  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = async () => {
    try {
      const projects = await api.getProjects();
      if (projects.length > 0) {
        setSelectedProject(projects[0].id);
        loadWebhooks(projects[0].id);
      }
    } catch (error) {
      console.error("Failed to load projects:", error);
    }
  };

  const loadWebhooks = async (projectId: string) => {
    try {
      setLoading(true);
      const data = await api.getWebhooks(projectId);
      setWebhooks(data);
    } catch (error) {
      console.error("Failed to load webhooks:", error);
      toast.error("웹훅 목록을 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleTest = async (webhookId: string) => {
    try {
      const result = await api.testWebhook(webhookId);
      toast.success(result);
    } catch (error: any) {
      console.error("Failed to test webhook:", error);
      toast.error(error.message || "웹훅 테스트에 실패했습니다.");
    }
  };

  const handleDelete = async (id: string, name: string) => {
    if (!confirm(`"${name}" 웹훅을 삭제하시겠습니까?`)) {
      return;
    }

    try {
      await api.deleteWebhook(id);
      await loadWebhooks(selectedProject);
      toast.success("웹훅이 삭제되었습니다.");
    } catch (error) {
      console.error("Failed to delete webhook:", error);
      toast.error("웹훅 삭제에 실패했습니다.");
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-text-secondary">웹훅을 불러오는 중...</div>
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
        <div>
          <h1 className="text-2xl font-bold text-text-primary">웹훅 설정</h1>
          <p className="text-sm text-text-secondary mt-1">
            Discord, Slack 등의 웹훅을 설정하여 에러 알림을 받으세요
          </p>
        </div>
        <Button onClick={() => setShowCreateModal(true)}>
          <Plus className="w-4 h-4 mr-2" />
          웹훅 추가
        </Button>
      </div>

      {/* Webhooks List */}
      {webhooks.length === 0 ? (
        <div className="text-center py-12 bg-bg-secondary rounded-lg border border-border">
          <p className="text-text-secondary mb-4">설정된 웹훅이 없습니다.</p>
          <Button variant="outline" onClick={() => setShowCreateModal(true)}>
            <Plus className="w-4 h-4 mr-2" />
            첫 웹훅 추가하기
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {webhooks.map((webhook) => (
            <WebhookCard
              key={webhook.id}
              webhook={webhook}
              onTest={() => handleTest(webhook.id)}
              onEdit={() => setEditingWebhook(webhook)}
              onDelete={() => handleDelete(webhook.id, webhook.name)}
            />
          ))}
        </div>
      )}

      {/* Create/Edit Dialog */}
      <WebhookDialog
        open={showCreateModal || !!editingWebhook}
        onClose={() => {
          setShowCreateModal(false);
          setEditingWebhook(null);
        }}
        onSuccess={() => {
          loadWebhooks(selectedProject);
          setShowCreateModal(false);
          setEditingWebhook(null);
        }}
        webhook={editingWebhook}
        projectId={selectedProject}
      />
    </motion.div>
  );
}

function WebhookCard({
  webhook,
  onTest,
  onEdit,
  onDelete,
}: {
  webhook: WebhookConfigResponse;
  onTest: () => void;
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <motion.div
      className="bg-bg-secondary rounded-lg p-6 border border-border"
      whileHover={{ scale: 1.01 }}
      transition={{ duration: 0.2 }}
    >
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center gap-3 mb-2">
            <h3 className="text-lg font-semibold text-text-primary">{webhook.name}</h3>
            <Badge variant={webhook.type === "DISCORD" ? "default" : "secondary"}>
              {webhook.type}
            </Badge>
            {webhook.enabled ? (
              <Badge variant="default">
                <Check className="w-3 h-3 mr-1" />
                활성
              </Badge>
            ) : (
              <Badge variant="secondary">
                <X className="w-3 h-3 mr-1" />
                비활성
              </Badge>
            )}
          </div>

          <p className="text-sm font-mono text-text-secondary mb-4">
            {webhook.webhookUrl}
          </p>

          <div className="flex items-center gap-4 text-sm text-text-secondary">
            <span>전송: {webhook.totalSent}회</span>
            {webhook.failureCount > 0 && (
              <span className="text-error">실패: {webhook.failureCount}회</span>
            )}
            {webhook.lastTriggeredAt && (
              <span>마지막 전송: {new Date(webhook.lastTriggeredAt).toLocaleString()}</span>
            )}
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={onTest}>
            <TestTube className="w-4 h-4 mr-1" />
            테스트
          </Button>
          <Button variant="outline" size="sm" onClick={onEdit}>
            <Edit className="w-4 h-4 mr-1" />
            수정
          </Button>
          <Button variant="destructive" size="sm" onClick={onDelete}>
            <Trash2 className="w-4 h-4" />
          </Button>
        </div>
      </div>
    </motion.div>
  );
}

function WebhookDialog({
  open,
  onClose,
  onSuccess,
  webhook,
  projectId,
}: {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
  webhook: WebhookConfigResponse | null;
  projectId: string;
}) {
  const [formData, setFormData] = useState<WebhookConfigRequest>({
    projectId: projectId,
    type: "DISCORD",
    webhookUrl: "",
    name: "",
    enabled: true,
  });

  useEffect(() => {
    if (webhook) {
      setFormData({
        projectId: webhook.projectId,
        type: webhook.type as any,
        webhookUrl: webhook.webhookUrl,
        name: webhook.name,
        enabled: webhook.enabled,
      });
    } else {
      setFormData({
        projectId: projectId,
        type: "DISCORD",
        webhookUrl: "",
        name: "",
        enabled: true,
      });
    }
  }, [webhook, projectId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      if (webhook) {
        await api.updateWebhook(webhook.id, formData);
        toast.success("웹훅이 수정되었습니다.");
      } else {
        await api.createWebhook(formData);
        toast.success("웹훅이 추가되었습니다.");
      }
      onSuccess();
    } catch (error) {
      console.error("Failed to save webhook:", error);
      toast.error("웹훅 저장에 실패했습니다.");
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{webhook ? "웹훅 수정" : "웹훅 추가"}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="name">웹훅 이름</Label>
            <Input
              id="name"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="예: Discord 알림"
              required
            />
          </div>

          <div>
            <Label htmlFor="type">웹훅 타입</Label>
            <select
              id="type"
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value as any })}
              className="w-full px-3 py-2 bg-bg-tertiary border border-border rounded-lg text-text-primary"
              required
            >
              <option value="DISCORD">Discord</option>
              <option value="SLACK">Slack</option>
              <option value="TELEGRAM">Telegram</option>
              <option value="CUSTOM">Custom</option>
            </select>
          </div>

          <div>
            <Label htmlFor="webhookUrl">웹훅 URL</Label>
            <Input
              id="webhookUrl"
              type="url"
              value={formData.webhookUrl}
              onChange={(e) => setFormData({ ...formData, webhookUrl: e.target.value })}
              placeholder="https://discord.com/api/webhooks/..."
              required
            />
          </div>

          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="enabled"
              checked={formData.enabled}
              onChange={(e) => setFormData({ ...formData, enabled: e.target.checked })}
              className="w-4 h-4"
            />
            <Label htmlFor="enabled">웹훅 활성화</Label>
          </div>

          <div className="flex items-center gap-2 justify-end">
            <Button type="button" variant="outline" onClick={onClose}>
              취소
            </Button>
            <Button type="submit">{webhook ? "수정" : "추가"}</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
