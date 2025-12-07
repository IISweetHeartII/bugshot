"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { ArrowLeft, Plus, Trash2, TestTube, Edit, Check, X } from "lucide-react";
import { MESSAGES, WEBHOOK_TYPES } from "@/lib/constants";
import { getErrorMessage } from "@/lib/utils";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { Select } from "@/components/ui/select";
import { useConfirmDialog } from "@/components/ui/confirm-dialog";
import { toast } from "sonner";
import type { ProjectResponse, WebhookConfigResponse, WebhookConfigRequest } from "@/types/api";

type WebhookType = WebhookConfigRequest["type"];

export default function ProjectSettingsPage() {
  const params = useParams();
  const router = useRouter();
  const projectId = params.id as string;

  const [project, setProject] = useState<ProjectResponse | null>(null);
  const [webhooks, setWebhooks] = useState<WebhookConfigResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingWebhook, setEditingWebhook] = useState<WebhookConfigResponse | null>(null);
  const { confirm, ConfirmDialogComponent } = useConfirmDialog();

  useEffect(() => {
    loadProject();
    loadWebhooks();
  }, [projectId]);

  const loadProject = async () => {
    try {
      const data = await api.getProject(projectId);
      setProject(data);
    } catch (error) {
      console.error("Failed to load project:", error);
      toast.error(MESSAGES.ERROR.LOAD_PROJECT);
    }
  };

  const loadWebhooks = async () => {
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
      await loadWebhooks();
      toast.success(MESSAGES.SUCCESS.WEBHOOK_DELETED);
    } catch (error) {
      console.error("Failed to delete webhook:", error);
      toast.error(MESSAGES.ERROR.DELETE_WEBHOOK);
    }
  };

  if (loading) {
    return <LoadingSpinner message="설정을 불러오는 중..." />;
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
          <Button variant="ghost" onClick={() => router.push(`/projects/${projectId}`)}>
            <ArrowLeft className="w-4 h-4 mr-2" aria-hidden="true" />
            뒤로
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-text-primary">
              {project?.name} 설정
            </h1>
            <p className="text-sm text-text-secondary mt-1">
              프로젝트의 웹훅 및 알림 설정을 관리합니다.
            </p>
          </div>
        </div>
      </div>

      {/* Webhooks Section */}
      <Card size="md">
        <CardHeader>
          <CardTitle>웹훅 설정</CardTitle>
          <Button size="sm" onClick={() => setShowCreateModal(true)}>
            <Plus className="w-4 h-4 mr-2" aria-hidden="true" />
            웹훅 추가
          </Button>
        </CardHeader>

        <p className="text-sm text-text-secondary mb-4">
          Discord, Slack 등의 웹훅을 설정하여 에러 발생 시 알림을 받으세요.
        </p>

        {webhooks.length === 0 ? (
          <div className="text-center py-8 bg-bg-tertiary rounded-lg border border-bg-primary">
            <p className="text-text-secondary mb-4">설정된 웹훅이 없습니다.</p>
            <Button variant="outline" onClick={() => setShowCreateModal(true)}>
              <Plus className="w-4 h-4 mr-2" aria-hidden="true" />
              첫 웹훅 추가하기
            </Button>
          </div>
        ) : (
          <div className="space-y-3">
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
      </Card>

      {/* Create/Edit Dialog */}
      <WebhookDialog
        open={showCreateModal || !!editingWebhook}
        onClose={() => {
          setShowCreateModal(false);
          setEditingWebhook(null);
        }}
        onSuccess={() => {
          loadWebhooks();
          setShowCreateModal(false);
          setEditingWebhook(null);
        }}
        webhook={editingWebhook}
        projectId={projectId}
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
    <div className="flex items-start justify-between p-4 bg-bg-tertiary rounded-lg border border-bg-primary">
      <div className="flex-1">
        <div className="flex items-center gap-3 mb-2">
          <span className="font-semibold text-text-primary">{webhook.name}</span>
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

        <p className="text-sm font-mono text-text-secondary mb-2 truncate max-w-md">
          {webhook.webhookUrl}
        </p>

        <div className="flex items-center gap-4 text-xs text-text-muted">
          <span>전송: {webhook.totalSent}회</span>
          {webhook.failureCount > 0 && (
            <span className="text-error">실패: {webhook.failureCount}회</span>
          )}
          {webhook.lastTriggeredAt && (
            <span>마지막: {new Date(webhook.lastTriggeredAt).toLocaleString("ko-KR")}</span>
          )}
        </div>
      </div>

      <div className="flex items-center gap-2">
        <Button variant="outline" size="sm" onClick={onTest}>
          <TestTube className="w-4 h-4 mr-1" aria-hidden="true" />
          테스트
        </Button>
        <Button variant="outline" size="sm" onClick={onEdit}>
          <Edit className="w-4 h-4" aria-hidden="true" />
        </Button>
        <Button variant="destructive" size="sm" onClick={onDelete}>
          <Trash2 className="w-4 h-4" aria-hidden="true" />
        </Button>
      </div>
    </div>
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
  const [saving, setSaving] = useState(false);

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
    setSaving(true);

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
    } finally {
      setSaving(false);
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
              placeholder="예: Discord 에러 알림"
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
            <Button type="button" variant="outline" onClick={onClose} disabled={saving}>
              취소
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? "저장 중..." : webhook ? "수정" : "추가"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
