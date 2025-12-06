"use client";

import { useSession } from "next-auth/react";
import { useRouter, usePathname } from "next/navigation";
import { LayoutDashboard, Folder, AlertCircle, Settings, LogOut } from "lucide-react";
import Link from "next/link";

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
        <div className="text-text-secondary">로딩 중...</div>
      </div>
    );
  }

  if (status === "unauthenticated") {
    router.push("/login");
    return null;
  }

  return (
    <div className="min-h-screen bg-bg-tertiary">
      {/* Sidebar */}
      <aside className="fixed left-0 top-0 h-full w-64 bg-bg-secondary border-r border-bg-primary">
        {/* Logo */}
        <div className="h-16 flex items-center px-6 border-b border-bg-primary">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center">
              <span className="text-xl">⚡</span>
            </div>
            <div>
              <h1 className="text-lg font-bold text-text-primary">BugShot</h1>
              <p className="text-xs text-text-muted">Error Monitoring</p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="p-4 space-y-2">
          {navigation.map((item) => {
            const isActive = pathname === item.href;
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
              >
                <item.icon className="w-5 h-5" />
                <span className="font-medium">{item.name}</span>
              </Link>
            );
          })}
        </nav>

        {/* User Info */}
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-bg-primary">
          <div className="flex items-center gap-3 px-2 py-2">
            {session?.user?.image && (
              <img
                src={session.user.image}
                alt={session.user.name || "User"}
                className="w-10 h-10 rounded-full"
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
              onClick={() => router.push("/api/auth/signout")}
              className="p-2 hover:bg-bg-primary rounded-lg transition-colors"
              title="로그아웃"
            >
              <LogOut className="w-4 h-4 text-text-muted" />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="ml-64 min-h-screen">
        {/* Top Bar */}
        <header className="h-16 bg-bg-secondary border-b border-bg-primary flex items-center justify-between px-8">
          <div>
            <h2 className="text-xl font-semibold text-text-primary">
              {navigation.find((item) => item.href === pathname)?.name || "대시보드"}
            </h2>
          </div>
        </header>

        {/* Page Content */}
        <div className="p-8">{children}</div>
      </main>
    </div>
  );
}
