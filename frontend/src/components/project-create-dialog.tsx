"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { api } from "@/lib/api";
import { toast } from "sonner";

interface CreateProjectDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

export function CreateProjectDialog({
  open,
  onOpenChange,
  onSuccess,
}: CreateProjectDialogProps) {
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    environment: "DEVELOPMENT",
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      toast.error("프로젝트 이름을 입력해주세요.");
      return;
    }

    try {
      setLoading(true);
      await api.createProject(formData);
      toast.success("프로젝트가 생성되었습니다!");
      onSuccess();
      setFormData({ name: "", description: "", environment: "DEVELOPMENT" });
    } catch (error) {
      console.error("Failed to create project:", error);
      toast.error("프로젝트 생성에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>새 프로젝트 생성</DialogTitle>
          <DialogDescription>
            프로젝트 정보를 입력하고 에러 모니터링을 시작하세요.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">프로젝트 이름 *</Label>
            <Input
              id="name"
              value={formData.name}
              onChange={(e) =>
                setFormData({ ...formData, name: e.target.value })
              }
              placeholder="My Application"
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">설명</Label>
            <textarea
              id="description"
              value={formData.description}
              onChange={(e) =>
                setFormData({ ...formData, description: e.target.value })
              }
              className="flex min-h-[80px] w-full rounded-lg border border-bg-primary bg-bg-tertiary px-3 py-2 text-sm text-text-primary ring-offset-bg-secondary placeholder:text-text-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              placeholder="프로젝트 설명을 입력하세요..."
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="environment">환경</Label>
            <select
              id="environment"
              value={formData.environment}
              onChange={(e) =>
                setFormData({ ...formData, environment: e.target.value })
              }
              className="flex h-10 w-full rounded-lg border border-bg-primary bg-bg-tertiary px-3 py-2 text-sm text-text-primary ring-offset-bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <option value="DEVELOPMENT">Development</option>
              <option value="STAGING">Staging</option>
              <option value="PRODUCTION">Production</option>
            </select>
          </div>

          <DialogFooter className="gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={loading}
            >
              취소
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? "생성 중..." : "생성"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
