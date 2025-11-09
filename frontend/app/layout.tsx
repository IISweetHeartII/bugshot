import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Error Monitor - 에러 모니터링 서비스",
  description: "다국어 지원 에러 모니터링 서비스",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.Node;
}>) {
  return (
    <html lang="ko">
      <body className="antialiased">
        {children}
      </body>
    </html>
  );
}
