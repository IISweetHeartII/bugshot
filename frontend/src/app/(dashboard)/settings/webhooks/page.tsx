"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { Plus, Trash2, TestTube, Edit, Check, X } from "lucide-react";
import { getErrorMessage, formatDate } from "@/lib/utils";
import { MESSAGES, WEBHOOK_TYPES } from "@/lib/constants";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { Select } from "@/components/ui/select";
import { useConfirmDialog } from "@/components/ui/confirm-dialog";
import { toast } from "sonner";
import type { WebhookConfigResponse, WebhookConfigRequest } from "@/types/api";

type WebhookType = WebhookConfigRequest['type'];

export default function WebhooksPage() {
  const [webhooks, setWebhooks] = useState<WebhookConfigResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingWebhook, setEditingWebhook] = useState<WebhookConfigResponse | null>(null);
  const [selectedProject, setSelectedProject] = useState<string>("");
  const { confirm, ConfirmDialogComponent } = useConfirmDialog();

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
      toast.error(MESSAGES.ERROR.LOAD_WEBHOOKS);
    } finally {
      setLoading(false);
    }
  };

  const handleTest = async (webhookId: string) => {
    try {
      const result = await api.testWebhook(webhookId);
      toast.success(result);
    } catch (error: unknown) {
      console.error("Failed to test webhook:", error);
      toast.error(getErrorMessage(error));
    }
  };

  const handleDelete = async (id: string, name: string) => {
    const confirmed = await confirm({
      title: "웹훅 삭제",
      description: `"${name}" 웹훅을 삭제하시겠습니까?`,
      confirmText: "삭제",
      confirmVariant: "destructive",
    });

    if (!confirmed) return;

    try {
      await api.deleteWebhook(id);
      await loadWebhooks(selectedProject);
      toast.success(MESSAGES.SUCCESS.WEBHOOK_DELETED);
    } catch (error) {
      console.error("Failed to delete webhook:", error);
      toast.error(MESSAGES.ERROR.DELETE_WEBHOOK);
    }
  };

  if (loading) {
    return <LoadingSpinner message="웹훅을 불러오는 중..." />;
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
          <Plus className="w-4 h-4 mr-2" aria-hidden="true" />
          웹훅 추가
        </Button>
      </div>

      {/* Webhooks List */}
      {webhooks.length === 0 ? (
        <div className="text-center py-12 bg-bg-secondary rounded-lg border border-bg-primary">
          <p className="text-text-secondary mb-4">설정된 웹훅이 없습니다.</p>
          <Button variant="outline" onClick={() => setShowCreateModal(true)}>
            <Plus className="w-4 h-4 mr-2" aria-hidden="true" />
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

      {/* Confirm Dialog */}
      {ConfirmDialogComponent}
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
      className="bg-bg-secondary rounded-lg p-6 border border-bg-primary"
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
                <Check className="w-3 h-3 mr-1" aria-hidden="true" />
                활성
              </Badge>
            ) : (
              <Badge variant="secondary">
                <X className="w-3 h-3 mr-1" aria-hidden="true" />
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
              <span>마지막 전송: {formatDate(webhook.lastTriggeredAt, "short")}</span>
            )}
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={onTest}>
            <TestTube className="w-4 h-4 mr-1" aria-hidden="true" />
            테스트
          </Button>
          <Button variant="outline" size="sm" onClick={onEdit}>
            <Edit className="w-4 h-4 mr-1" aria-hidden="true" />
            수정
          </Button>
          <Button variant="destructive" size="sm" onClick={onDelete}>
            <Trash2 className="w-4 h-4" aria-hidden="true" />
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
        type: webhook.type,
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
        toast.success(MESSAGES.SUCCESS.WEBHOOK_UPDATED);
      } else {
        await api.createWebhook(formData);
        toast.success(MESSAGES.SUCCESS.WEBHOOK_CREATED);
      }
      onSuccess();
    } catch (error) {
      console.error("Failed to save webhook:", error);
      toast.error(webhook ? MESSAGES.ERROR.UPDATE_WEBHOOK : MESSAGES.ERROR.CREATE_WEBHOOK);
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
            <Select
              id="type"
              value={formData.type}
              onChange={(value) => setFormData({ ...formData, type: value as WebhookType })}
              options={WEBHOOK_TYPES}
              aria-label="웹훅 타입 선택"
            />
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
