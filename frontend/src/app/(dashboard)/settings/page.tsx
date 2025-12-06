"use client";

import { useState } from "react";
import { useSession } from "next-auth/react";
import Image from "next/image";
import { User, CreditCard, Bell } from "lucide-react";
import { Switch } from "@/components/ui/switch";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { toast } from "sonner";

interface NotificationSettings {
  emailNotification: boolean;
  discordNotification: boolean;
  weeklyReport: boolean;
}

export default function SettingsPage() {
  const { data: session } = useSession();
  const [notifications, setNotifications] = useState<NotificationSettings>({
    emailNotification: true,
    discordNotification: true,
    weeklyReport: false,
  });
  const [saving, setSaving] = useState(false);

  const handleNotificationChange = async (
    key: keyof NotificationSettings,
    value: boolean
  ) => {
    // Optimistic update
    setNotifications((prev) => ({ ...prev, [key]: value }));

    try {
      setSaving(true);
      // TODO: API 연동 시 실제 저장 로직 추가
      // await api.updateNotificationSettings({ [key]: value });

      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 300));

      toast.success("알림 설정이 저장되었습니다.");
    } catch (error) {
      // Rollback on error
      setNotifications((prev) => ({ ...prev, [key]: !value }));
      toast.error("설정 저장에 실패했습니다.");
      console.error("Failed to update notification settings:", error);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-bold text-text-primary">설정</h2>
        <p className="text-text-secondary mt-1">
          계정 및 알림 설정을 관리합니다.
        </p>
      </div>

      {/* Profile Section */}
      <Card>
        <div className="flex items-center gap-3 mb-6">
          <User className="w-6 h-6 text-primary" aria-hidden="true" />
          <h3 className="text-xl font-semibold text-text-primary">프로필</h3>
        </div>

        <div className="flex items-center gap-6">
          {session?.user?.image && (
            <Image
              src={session.user.image}
              alt={`${session.user.name || "User"} 프로필 이미지`}
              width={80}
              height={80}
              className="rounded-full"
            />
          )}
          <div>
            <h4 className="text-lg font-semibold text-text-primary">
              {session?.user?.name || "Unknown User"}
            </h4>
            <p className="text-text-secondary">{session?.user?.email}</p>
            <div className="mt-2">
              <span className="inline-block px-3 py-1 bg-primary/20 text-primary rounded-full text-sm font-medium">
                FREE 플랜
              </span>
            </div>
          </div>
        </div>
      </Card>

      {/* Plan Section */}
      <Card>
        <div className="flex items-center gap-3 mb-6">
          <CreditCard className="w-6 h-6 text-primary" aria-hidden="true" />
          <h3 className="text-xl font-semibold text-text-primary">플랜 & 사용량</h3>
        </div>

        <div className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-bg-tertiary rounded-lg p-4">
              <div className="text-sm text-text-muted mb-1">프로젝트</div>
              <div className="text-2xl font-bold text-text-primary">0 / 3</div>
            </div>
            <div className="bg-bg-tertiary rounded-lg p-4">
              <div className="text-sm text-text-muted mb-1">월간 이벤트</div>
              <div className="text-2xl font-bold text-text-primary">0 / 10K</div>
            </div>
            <div className="bg-bg-tertiary rounded-lg p-4">
              <div className="text-sm text-text-muted mb-1">세션 리플레이 보관</div>
              <div className="text-2xl font-bold text-text-primary">7일</div>
            </div>
          </div>

          <div className="pt-4">
            <Button disabled>Pro 플랜으로 업그레이드 (준비 중)</Button>
          </div>
        </div>
      </Card>

      {/* Notifications Section */}
      <Card>
        <div className="flex items-center gap-3 mb-6">
          <Bell className="w-6 h-6 text-primary" aria-hidden="true" />
          <h3 className="text-xl font-semibold text-text-primary">알림 설정</h3>
          {saving && (
            <span className="text-xs text-text-muted animate-pulse">저장 중...</span>
          )}
        </div>

        <div className="space-y-4">
          <div className="flex items-center justify-between py-3 border-b border-bg-primary">
            <div>
              <h4 className="font-medium text-text-primary">이메일 알림</h4>
              <p className="text-sm text-text-muted">
                중요한 에러 발생 시 이메일로 알림을 받습니다.
              </p>
            </div>
            <Switch
              checked={notifications.emailNotification}
              onCheckedChange={(checked) =>
                handleNotificationChange("emailNotification", checked)
              }
              disabled={saving}
              aria-label="이메일 알림 설정"
            />
          </div>

          <div className="flex items-center justify-between py-3 border-b border-bg-primary">
            <div>
              <h4 className="font-medium text-text-primary">Discord 알림</h4>
              <p className="text-sm text-text-muted">
                Discord 채널로 에러 알림을 받습니다.
              </p>
            </div>
            <Switch
              checked={notifications.discordNotification}
              onCheckedChange={(checked) =>
                handleNotificationChange("discordNotification", checked)
              }
              disabled={saving}
              aria-label="Discord 알림 설정"
            />
          </div>

          <div className="flex items-center justify-between py-3">
            <div>
              <h4 className="font-medium text-text-primary">주간 리포트</h4>
              <p className="text-sm text-text-muted">
                매주 월요일 에러 통계를 받습니다.
              </p>
            </div>
            <Switch
              checked={notifications.weeklyReport}
              onCheckedChange={(checked) =>
                handleNotificationChange("weeklyReport", checked)
              }
              disabled={saving}
              aria-label="주간 리포트 설정"
            />
          </div>
        </div>
      </Card>
    </div>
  );
}
