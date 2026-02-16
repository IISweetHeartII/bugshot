"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { Play, Download, ArrowLeft, User, Clock, Globe } from "lucide-react";
import { formatRelativeTime, formatFileSize } from "@/lib/utils";
import { MESSAGES, API_CONFIG } from "@/lib/constants";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { toast } from "sonner";
import type { SessionReplayResponse } from "@/types/api";

export default function SessionReplayPage() {
  const params = useParams();
  const router = useRouter();
  const errorId = params.id as string;

  const [replay, setReplay] = useState<SessionReplayResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    loadReplay();
  }, [errorId]);

  const loadReplay = async () => {
    try {
      setLoading(true);
      const data = await api.getSessionReplay(errorId);
      setReplay(data);
    } catch (error) {
      console.error("Failed to load session replay:", error);
      toast.error(MESSAGES.ERROR.LOAD_REPLAY);
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async () => {
    if (!replay) return;

    try {
      setDownloading(true);
      const downloadUrl = await api.getReplayDownloadUrl(errorId, API_CONFIG.REPLAY_DOWNLOAD_EXPIRATION_SECONDS);

      // 다운로드 링크 열기
      window.open(downloadUrl, "_blank");
      toast.success(MESSAGES.SUCCESS.DOWNLOAD_STARTED);
    } catch (error) {
      console.error("Failed to download replay:", error);
      toast.error(MESSAGES.ERROR.DOWNLOAD_REPLAY);
    } finally {
      setDownloading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message={MESSAGES.LOADING.REPLAY} />;
  }

  if (!replay) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] space-y-4">
        <p className="text-text-secondary">세션 리플레이가 없습니다.</p>
        <Button variant="outline" onClick={() => router.back()}>
          <ArrowLeft className="w-4 h-4 mr-2" />
          돌아가기
        </Button>
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
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => router.back()}>
            <ArrowLeft className="w-4 h-4 mr-2" />
            뒤로
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-text-primary">세션 리플레이</h1>
            <p className="text-sm text-text-secondary mt-1">
              에러 발생 시점의 사용자 행동 녹화
            </p>
          </div>
        </div>

        <Button onClick={handleDownload} disabled={downloading}>
          <Download className="w-4 h-4 mr-2" />
          {downloading ? "다운로드 중..." : "다운로드"}
        </Button>
      </div>

      {/* Replay Info */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <InfoCard
          icon={Clock}
          label="녹화 시간"
          value={formatRelativeTime(replay.recordedAt)}
        />
        <InfoCard
          icon={Play}
          label="재생 시간"
          value={`${replay.duration}초`}
        />
        <InfoCard
          icon={Globe}
          label="파일 크기"
          value={formatFileSize(replay.size)}
        />
        <InfoCard
          icon={User}
          label="사용자 ID"
          value={replay.userInfo.userId || "익명"}
        />
      </div>

      {/* User Info */}
      <div className="bg-bg-secondary rounded-lg p-6 border border-bg-primary">
        <h2 className="text-lg font-semibold text-text-primary mb-4">사용자 정보</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <InfoRow label="사용자 ID" value={replay.userInfo.userId || "N/A"} />
          <InfoRow label="IP 주소" value={replay.userInfo.ip || "N/A"} />
          <InfoRow label="브라우저" value={replay.userInfo.browser || "N/A"} />
          <InfoRow label="운영체제" value={replay.userInfo.os || "N/A"} />
          <InfoRow
            label="User Agent"
            value={replay.userInfo.userAgent || "N/A"}
            span={2}
          />
        </div>
      </div>

      {/* Replay Player Placeholder */}
      <div className="bg-bg-secondary rounded-lg p-12 border border-bg-primary text-center">
        <Play className="w-16 h-16 mx-auto mb-4 text-text-secondary" />
        <h3 className="text-lg font-semibold text-text-primary mb-2">
          세션 리플레이 플레이어
        </h3>
        <p className="text-text-secondary mb-6">
          세션 리플레이를 재생하려면 다운로드하여 별도의 플레이어를 사용하세요.
        </p>
        <p className="text-sm text-text-secondary">
          향후 업데이트에서 웹 기반 플레이어가 추가될 예정입니다.
        </p>
      </div>
    </motion.div>
  );
}

function InfoCard({
  icon: Icon,
  label,
  value,
}: {
  icon: React.ElementType;
  label: string;
  value: string;
}) {
  return (
    <div className="bg-bg-secondary rounded-lg p-4 border border-bg-primary">
      <div className="flex items-center gap-3">
        <div className="p-2 bg-primary/10 rounded-lg">
          <Icon className="w-5 h-5 text-primary" />
        </div>
        <div>
          <p className="text-xs text-text-secondary">{label}</p>
          <p className="text-lg font-semibold text-text-primary">{value}</p>
        </div>
      </div>
    </div>
  );
}

function InfoRow({
  label,
  value,
  span,
}: {
  label: string;
  value: string;
  span?: number;
}) {
  return (
    <div className={span === 2 ? "col-span-2" : ""}>
      <dt className="text-sm text-text-secondary mb-1">{label}</dt>
      <dd className="text-sm font-mono text-text-primary bg-bg-tertiary px-3 py-2 rounded border border-bg-primary break-all">
        {value}
      </dd>
    </div>
  );
}
