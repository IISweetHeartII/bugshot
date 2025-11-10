import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Providers } from "./providers";
import { ToasterProvider } from "@/components/providers/toaster-provider";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "ErrorWatch - 실시간 에러 모니터링",
  description: "Discord 통합, 세션 리플레이가 포함된 강력한 에러 모니터링 서비스",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className={inter.className}>
        <Providers>
          {children}
          <ToasterProvider />
        </Providers>
      </body>
    </html>
  );
}
