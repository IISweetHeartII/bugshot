"use client";

import { useSession } from "next-auth/react";
import { useRouter, usePathname } from "next/navigation";
import { LayoutDashboard, Folder, AlertCircle, Settings, LogOut } from "lucide-react";
import Link from "next/link";
import Image from "next/image";
import { LoadingSpinner } from "@/components/ui/loading-spinner";

const navigation = [
  { name: "대시보드", href: "/dashboard", icon: LayoutDashboard },
  { name: "프로젝트", href: "/projects", icon: Folder },
  { name: "에러", href: "/errors", icon: AlertCircle },
  { name: "설정", href: "/settings", icon: Settings },
];

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { data: session, status } = useSession();
  const router = useRouter();
  const pathname = usePathname();

  if (status === "loading") {
    return (
      <div className="min-h-screen bg-bg-tertiary flex items-center justify-center">
        <LoadingSpinner message="인증 확인 중..." />
      </div>
    );
  }

  if (status === "unauthenticated") {
    router.push("/login");
    return null;
  }

  const handleLogout = () => {
    router.push("/api/auth/signout");
  };

  return (
    <div className="min-h-screen bg-bg-tertiary">
      {/* Sidebar */}
      <aside
        className="fixed left-0 top-0 h-full w-64 bg-bg-secondary border-r border-bg-primary"
        role="navigation"
        aria-label="메인 네비게이션"
      >
        {/* Logo */}
        <div className="h-16 flex items-center px-6 border-b border-bg-primary">
          <Link href="/dashboard" className="flex items-center gap-3">
            <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center">
              <span className="text-xl" role="img" aria-label="BugShot 로고">⚡</span>
            </div>
            <div>
              <h1 className="text-lg font-bold text-text-primary">BugShot</h1>
              <p className="text-xs text-text-muted">Error Monitoring</p>
            </div>
          </Link>
        </div>

        {/* Navigation */}
        <nav className="p-4 space-y-2" aria-label="주요 메뉴">
          {navigation.map((item) => {
            const isActive = pathname === item.href || pathname.startsWith(item.href + "/");
            return (
              <Link
                key={item.name}
                href={item.href}
                className={`
                  flex items-center gap-3 px-4 py-2.5 rounded-lg transition-colors
                  ${
                    isActive
                      ? "bg-primary text-white"
                      : "text-text-secondary hover:bg-bg-primary hover:text-text-primary"
                  }
                `}
                aria-current={isActive ? "page" : undefined}
              >
                <item.icon className="w-5 h-5" aria-hidden="true" />
                <span className="font-medium">{item.name}</span>
              </Link>
            );
          })}
        </nav>

        {/* User Info */}
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-bg-primary">
          <div className="flex items-center gap-3 px-2 py-2">
            {session?.user?.image && (
              <Image
                src={session.user.image}
                alt={`${session.user.name || "User"} 프로필 이미지`}
                width={40}
                height={40}
                className="rounded-full"
              />
            )}
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-text-primary truncate">
                {session?.user?.name || "Unknown User"}
              </p>
              <p className="text-xs text-text-muted truncate">
                {session?.user?.email || ""}
              </p>
            </div>
            <button
              onClick={handleLogout}
              className="p-2 hover:bg-bg-primary rounded-lg transition-colors"
              aria-label="로그아웃"
            >
              <LogOut className="w-4 h-4 text-text-muted" aria-hidden="true" />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="ml-64 min-h-screen" role="main">
        {/* Top Bar */}
        <header className="h-16 bg-bg-secondary border-b border-bg-primary flex items-center justify-between px-8">
          <div>
            <h2 className="text-xl font-semibold text-text-primary">
              {navigation.find((item) => pathname.startsWith(item.href))?.name || "대시보드"}
            </h2>
          </div>
        </header>

        {/* Page Content */}
        <div className="p-8">{children}</div>
      </main>
    </div>
  );
}
