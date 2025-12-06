"use client";

import { motion } from "framer-motion";
import Link from "next/link";
import {
  Zap,
  Shield,
  Bell,
  PlayCircle,
  Code,
  Check,
  ArrowRight,
  Terminal,
  Copy,
  Github,
} from "lucide-react";
import { useState } from "react";

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-bg-tertiary">
      {/* Navigation */}
      <nav className="fixed top-0 left-0 right-0 z-50 bg-bg-tertiary/80 backdrop-blur-md border-b border-bg-primary">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2">
            <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
              <Zap className="w-5 h-5 text-white" />
            </div>
            <span className="text-xl font-bold text-text-primary">BugShot</span>
          </Link>

          <div className="hidden md:flex items-center gap-8">
            <a href="#features" className="text-text-secondary hover:text-text-primary transition-colors">
              기능
            </a>
            <a href="#quickstart" className="text-text-secondary hover:text-text-primary transition-colors">
              시작하기
            </a>
            <a href="#pricing" className="text-text-secondary hover:text-text-primary transition-colors">
              가격
            </a>
          </div>

          <div className="flex items-center gap-4">
            <Link
              href="/login"
              className="text-text-secondary hover:text-text-primary transition-colors"
            >
              로그인
            </Link>
            <Link
              href="/login"
              className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-lg font-medium transition-colors"
            >
              무료로 시작
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="pt-32 pb-20 px-6">
        <div className="max-w-5xl mx-auto text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
          >
            <div className="inline-flex items-center gap-2 bg-bg-secondary px-4 py-2 rounded-full text-sm text-text-secondary mb-8">
              <span className="w-2 h-2 bg-success rounded-full animate-pulse" />
              에러 모니터링의 새로운 기준
            </div>

            <h1 className="text-5xl md:text-7xl font-bold text-text-primary mb-6 leading-tight">
              에러 모니터링,
              <br />
              <span className="text-primary">5분이면 끝.</span>
            </h1>

            <p className="text-xl text-text-secondary mb-8 max-w-2xl mx-auto">
              Sentry보다 쉽고, 비용은 1/10.
              <br />
              설치 3줄, 설정 0개. 지금 바로 시작하세요.
            </p>
          </motion.div>

          {/* Install Command */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className="mb-8"
          >
            <InstallCommand />
          </motion.div>

          {/* CTA Buttons */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            className="flex flex-col sm:flex-row gap-4 justify-center"
          >
            <Link
              href="/login"
              className="inline-flex items-center justify-center gap-2 bg-primary hover:bg-primary-dark text-white px-8 py-4 rounded-xl font-semibold text-lg transition-all hover:scale-105"
            >
              무료로 시작하기
              <ArrowRight className="w-5 h-5" />
            </Link>
            <a
              href="#quickstart"
              className="inline-flex items-center justify-center gap-2 bg-bg-secondary hover:bg-bg-primary text-text-primary px-8 py-4 rounded-xl font-semibold text-lg transition-all border border-bg-primary"
            >
              <Code className="w-5 h-5" />
              문서 보기
            </a>
          </motion.div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 px-6 bg-bg-secondary">
        <div className="max-w-6xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <h2 className="text-4xl font-bold text-text-primary mb-4">
              왜 BugShot인가요?
            </h2>
            <p className="text-text-secondary text-lg">
              복잡한 설정 없이, 핵심 기능만 담았습니다.
            </p>
          </motion.div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            <FeatureCard
              icon={Zap}
              title="자동 에러 캡처"
              description="JavaScript 에러, Promise rejection, 네트워크 에러를 자동으로 수집합니다."
              delay={0}
            />
            <FeatureCard
              icon={PlayCircle}
              title="세션 리플레이"
              description="에러 발생 전 사용자의 행동을 녹화하여 재현 없이 디버깅하세요."
              delay={0.1}
            />
            <FeatureCard
              icon={Bell}
              title="실시간 알림"
              description="Slack, Discord, 이메일로 즉시 알림을 받으세요."
              delay={0.2}
            />
            <FeatureCard
              icon={Shield}
              title="소스맵 지원"
              description="프로덕션 에러도 원본 코드 위치를 정확히 파악합니다."
              delay={0.3}
            />
            <FeatureCard
              icon={Code}
              title="React 지원"
              description="Error Boundary와 Hooks로 React 앱에 쉽게 통합하세요."
              delay={0.4}
            />
            <FeatureCard
              icon={Terminal}
              title="TypeScript 완벽 지원"
              description="타입 정의가 포함되어 있어 자동완성이 됩니다."
              delay={0.5}
            />
          </div>
        </div>
      </section>

      {/* Quick Start Section */}
      <section id="quickstart" className="py-20 px-6">
        <div className="max-w-4xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <h2 className="text-4xl font-bold text-text-primary mb-4">
              3줄이면 끝
            </h2>
            <p className="text-text-secondary text-lg">
              복잡한 설정? 필요 없습니다.
            </p>
          </motion.div>

          <div className="space-y-8">
            {/* JavaScript/React */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
            >
              <h3 className="text-xl font-semibold text-text-primary mb-4 flex items-center gap-2">
                <span className="w-8 h-8 bg-primary/20 rounded-lg flex items-center justify-center text-primary text-sm font-bold">
                  1
                </span>
                JavaScript / TypeScript
              </h3>
              <CodeBlock
                code={`import BugShot from '@bugshot/browser-sdk';

BugShot.init({
  apiKey: 'your-api-key-here'
});

// 끝! 이제 모든 에러가 자동으로 수집됩니다.`}
                language="typescript"
              />
            </motion.div>

            {/* React */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
            >
              <h3 className="text-xl font-semibold text-text-primary mb-4 flex items-center gap-2">
                <span className="w-8 h-8 bg-primary/20 rounded-lg flex items-center justify-center text-primary text-sm font-bold">
                  2
                </span>
                React
              </h3>
              <CodeBlock
                code={`import { BugShotProvider, ErrorBoundary } from '@bugshot/react';

function App() {
  return (
    <BugShotProvider config={{ apiKey: 'your-api-key' }}>
      <ErrorBoundary>
        <YourApp />
      </ErrorBoundary>
    </BugShotProvider>
  );
}`}
                language="tsx"
              />
            </motion.div>

            {/* Manual Capture */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
            >
              <h3 className="text-xl font-semibold text-text-primary mb-4 flex items-center gap-2">
                <span className="w-8 h-8 bg-primary/20 rounded-lg flex items-center justify-center text-primary text-sm font-bold">
                  3
                </span>
                수동 에러 캡처
              </h3>
              <CodeBlock
                code={`try {
  await riskyOperation();
} catch (error) {
  BugShot.captureError(error);
}`}
                language="typescript"
              />
            </motion.div>
          </div>
        </div>
      </section>

      {/* Pricing Section */}
      <section id="pricing" className="py-20 px-6 bg-bg-secondary">
        <div className="max-w-6xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <h2 className="text-4xl font-bold text-text-primary mb-4">
              심플한 가격
            </h2>
            <p className="text-text-secondary text-lg">
              숨겨진 비용 없이, 필요한 만큼만 지불하세요.
            </p>
          </motion.div>

          <div className="grid md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            <PricingCard
              name="Free"
              price="₩0"
              period="영구 무료"
              description="사이드 프로젝트에 완벽"
              features={[
                "월 10,000 에러",
                "3개 프로젝트",
                "7일 데이터 보관",
                "이메일 알림",
                "기본 대시보드",
              ]}
              delay={0}
            />
            <PricingCard
              name="Pro"
              price="$9"
              period="/월"
              description="성장하는 팀을 위한"
              features={[
                "월 100,000 에러",
                "10개 프로젝트",
                "30일 데이터 보관",
                "Slack, Discord 알림",
                "세션 리플레이",
                "소스맵 업로드",
                "우선 지원",
              ]}
              highlighted
              delay={0.1}
            />
            <PricingCard
              name="Team"
              price="$29"
              period="/월"
              description="대규모 프로젝트용"
              features={[
                "월 1,000,000 에러",
                "50개 프로젝트",
                "90일 데이터 보관",
                "모든 알림 채널",
                "세션 리플레이",
                "팀 멤버 관리",
                "API 접근",
                "전용 지원",
              ]}
              delay={0.2}
            />
          </div>

          <motion.p
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            className="text-center text-text-muted mt-8"
          >
            Enterprise 플랜이 필요하신가요?{" "}
            <a href="mailto:contact@bugshot.com" className="text-primary hover:underline">
              문의하기
            </a>
          </motion.p>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 px-6">
        <div className="max-w-4xl mx-auto text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
          >
            <h2 className="text-4xl font-bold text-text-primary mb-4">
              5분 후, 첫 번째 에러를 잡아보세요
            </h2>
            <p className="text-text-secondary text-lg mb-8">
              신용카드 없이 시작할 수 있습니다.
            </p>
            <Link
              href="/login"
              className="inline-flex items-center justify-center gap-2 bg-primary hover:bg-primary-dark text-white px-8 py-4 rounded-xl font-semibold text-lg transition-all hover:scale-105"
            >
              무료로 시작하기
              <ArrowRight className="w-5 h-5" />
            </Link>
          </motion.div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-12 px-6 border-t border-bg-primary">
        <div className="max-w-6xl mx-auto">
          <div className="flex flex-col md:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
                <Zap className="w-5 h-5 text-white" />
              </div>
              <span className="text-xl font-bold text-text-primary">BugShot</span>
            </div>

            <div className="flex items-center gap-6 text-text-muted text-sm">
              <a href="#" className="hover:text-text-primary transition-colors">
                문서
              </a>
              <a href="#" className="hover:text-text-primary transition-colors">
                API
              </a>
              <a href="#" className="hover:text-text-primary transition-colors">
                이용약관
              </a>
              <a href="#" className="hover:text-text-primary transition-colors">
                개인정보처리방침
              </a>
            </div>

            <a
              href="https://github.com/bugshot"
              target="_blank"
              rel="noopener noreferrer"
              className="text-text-muted hover:text-text-primary transition-colors"
            >
              <Github className="w-6 h-6" />
            </a>
          </div>

          <div className="mt-8 text-center text-text-muted text-sm">
            © 2025 BugShot. All rights reserved.
          </div>
        </div>
      </footer>
    </div>
  );
}

// Install Command Component
function InstallCommand() {
  const [copied, setCopied] = useState(false);
  const command = "npm install @bugshot/browser-sdk";

  const handleCopy = () => {
    navigator.clipboard.writeText(command);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="inline-flex items-center gap-4 bg-bg-primary px-6 py-4 rounded-xl border border-bg-secondary">
      <Terminal className="w-5 h-5 text-text-muted" />
      <code className="text-text-primary font-mono">{command}</code>
      <button
        onClick={handleCopy}
        className="text-text-muted hover:text-text-primary transition-colors"
        title="복사"
      >
        {copied ? (
          <Check className="w-5 h-5 text-success" />
        ) : (
          <Copy className="w-5 h-5" />
        )}
      </button>
    </div>
  );
}

// Feature Card Component
function FeatureCard({
  icon: Icon,
  title,
  description,
  delay,
}: {
  icon: React.ElementType;
  title: string;
  description: string;
  delay: number;
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true }}
      transition={{ delay }}
      className="bg-bg-tertiary p-6 rounded-xl border border-bg-primary hover:border-primary/50 transition-colors"
    >
      <div className="w-12 h-12 bg-primary/20 rounded-xl flex items-center justify-center mb-4">
        <Icon className="w-6 h-6 text-primary" />
      </div>
      <h3 className="text-lg font-semibold text-text-primary mb-2">{title}</h3>
      <p className="text-text-secondary">{description}</p>
    </motion.div>
  );
}

// Code Block Component
function CodeBlock({ code, language }: { code: string; language: string }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="relative bg-bg-primary rounded-xl border border-bg-secondary overflow-hidden">
      <div className="flex items-center justify-between px-4 py-2 bg-bg-secondary border-b border-bg-primary">
        <span className="text-xs text-text-muted">{language}</span>
        <button
          onClick={handleCopy}
          className="text-text-muted hover:text-text-primary transition-colors text-sm flex items-center gap-1"
        >
          {copied ? (
            <>
              <Check className="w-4 h-4 text-success" />
              복사됨
            </>
          ) : (
            <>
              <Copy className="w-4 h-4" />
              복사
            </>
          )}
        </button>
      </div>
      <pre className="p-4 overflow-x-auto">
        <code className="text-sm text-text-primary font-mono">{code}</code>
      </pre>
    </div>
  );
}

// Pricing Card Component
function PricingCard({
  name,
  price,
  period,
  description,
  features,
  highlighted,
  delay,
}: {
  name: string;
  price: string;
  period: string;
  description: string;
  features: string[];
  highlighted?: boolean;
  delay: number;
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true }}
      transition={{ delay }}
      className={`relative p-8 rounded-2xl border ${
        highlighted
          ? "bg-primary/10 border-primary"
          : "bg-bg-tertiary border-bg-primary"
      }`}
    >
      {highlighted && (
        <div className="absolute -top-3 left-1/2 -translate-x-1/2 bg-primary text-white px-4 py-1 rounded-full text-sm font-medium">
          인기
        </div>
      )}
      <div className="text-center mb-6">
        <h3 className="text-xl font-semibold text-text-primary mb-2">{name}</h3>
        <div className="flex items-baseline justify-center gap-1">
          <span className="text-4xl font-bold text-text-primary">{price}</span>
          <span className="text-text-muted">{period}</span>
        </div>
        <p className="text-text-secondary text-sm mt-2">{description}</p>
      </div>

      <ul className="space-y-3 mb-8">
        {features.map((feature, i) => (
          <li key={i} className="flex items-center gap-3 text-text-secondary">
            <Check className="w-5 h-5 text-success flex-shrink-0" />
            {feature}
          </li>
        ))}
      </ul>

      <Link
        href="/login"
        className={`block w-full text-center py-3 rounded-xl font-medium transition-colors ${
          highlighted
            ? "bg-primary hover:bg-primary-dark text-white"
            : "bg-bg-secondary hover:bg-bg-primary text-text-primary border border-bg-primary"
        }`}
      >
        시작하기
      </Link>
    </motion.div>
  );
}
