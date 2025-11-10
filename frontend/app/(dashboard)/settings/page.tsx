"use client";

import { useSession } from "next-auth/react";
import { User, CreditCard, Bell } from "lucide-react";

export default function SettingsPage() {
  const { data: session } = useSession();

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
      <div className="bg-bg-secondary rounded-xl p-6 border border-bg-primary">
        <div className="flex items-center gap-3 mb-6">
          <User className="w-6 h-6 text-primary" />
          <h3 className="text-xl font-semibold text-text-primary">프로필</h3>
        </div>

        <div className="flex items-center gap-6">
          {session?.user?.image && (
            <img
              src={session.user.image}
              alt={session.user.name || "User"}
              className="w-20 h-20 rounded-full"
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
      </div>

      {/* Plan Section */}
      <div className="bg-bg-secondary rounded-xl p-6 border border-bg-primary">
        <div className="flex items-center gap-3 mb-6">
          <CreditCard className="w-6 h-6 text-primary" />
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
            <button className="bg-primary hover:bg-primary-dark text-white px-6 py-2 rounded-lg transition-colors">
              Pro 플랜으로 업그레이드
            </button>
          </div>
        </div>
      </div>

      {/* Notifications Section */}
      <div className="bg-bg-secondary rounded-xl p-6 border border-bg-primary">
        <div className="flex items-center gap-3 mb-6">
          <Bell className="w-6 h-6 text-primary" />
          <h3 className="text-xl font-semibold text-text-primary">알림 설정</h3>
        </div>

        <div className="space-y-4">
          <div className="flex items-center justify-between py-3 border-b border-bg-primary">
            <div>
              <h4 className="font-medium text-text-primary">이메일 알림</h4>
              <p className="text-sm text-text-muted">
                중요한 에러 발생 시 이메일로 알림을 받습니다.
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" className="sr-only peer" defaultChecked />
              <div className="w-11 h-6 bg-bg-tertiary peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
          </div>

          <div className="flex items-center justify-between py-3 border-b border-bg-primary">
            <div>
              <h4 className="font-medium text-text-primary">Discord 알림</h4>
              <p className="text-sm text-text-muted">
                Discord 채널로 에러 알림을 받습니다.
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" className="sr-only peer" defaultChecked />
              <div className="w-11 h-6 bg-bg-tertiary peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
          </div>

          <div className="flex items-center justify-between py-3">
            <div>
              <h4 className="font-medium text-text-primary">주간 리포트</h4>
              <p className="text-sm text-text-muted">
                매주 월요일 에러 통계를 받습니다.
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" className="sr-only peer" />
              <div className="w-11 h-6 bg-bg-tertiary peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
          </div>
        </div>
      </div>
    </div>
  );
}
