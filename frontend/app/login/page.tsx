"use client";

import { signIn } from "next-auth/react";
import { Github, Mail, CheckCircle } from "lucide-react";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";

const fadeInUp = {
  initial: { opacity: 0, y: 20 },
  animate: { opacity: 1, y: 0 },
  transition: { duration: 0.5 }
};

const staggerContainer = {
  animate: {
    transition: {
      staggerChildren: 0.1
    }
  }
};

const scaleIn = {
  initial: { scale: 0 },
  animate: { scale: 1 },
  transition: { type: "spring", duration: 0.5 }
};

export default function LoginPage() {
  const handleGithubLogin = () => {
    signIn("github", { callbackUrl: "/dashboard" });
  };

  const handleGoogleLogin = () => {
    signIn("google", { callbackUrl: "/dashboard" });
  };

  return (
    <div className="min-h-screen bg-bg-tertiary flex items-center justify-center p-4">
      <motion.div
        className="w-full max-w-md"
        initial="initial"
        animate="animate"
        variants={staggerContainer}
      >
        {/* Logo & Header */}
        <motion.div className="text-center mb-8" variants={fadeInUp}>
          <motion.div
            className="inline-flex items-center justify-center w-16 h-16 bg-primary rounded-2xl mb-4"
            variants={scaleIn}
            whileHover={{ scale: 1.05, rotate: 5 }}
            whileTap={{ scale: 0.95 }}
          >
            <span className="text-3xl">⚡</span>
          </motion.div>
          <h1 className="text-3xl font-bold text-text-primary mb-2">
            ErrorWatch
          </h1>
          <p className="text-text-secondary">
            실시간 에러 모니터링 & 세션 리플레이
          </p>
        </motion.div>

        {/* Login Card */}
        <motion.div
          className="bg-bg-secondary rounded-xl p-8 shadow-2xl border border-bg-primary"
          variants={fadeInUp}
        >
          <h2 className="text-xl font-semibold text-text-primary mb-6 text-center">
            로그인하여 시작하기
          </h2>

          {/* Login Buttons */}
          <motion.div className="space-y-3" variants={staggerContainer}>
            <motion.div variants={fadeInUp}>
              <Button
                onClick={handleGithubLogin}
                className="w-full bg-[#24292F] hover:bg-[#1B1F23] text-white h-12"
                size="lg"
              >
                <Github className="w-5 h-5" />
                GitHub으로 계속하기
              </Button>
            </motion.div>

            <motion.div variants={fadeInUp}>
              <Button
                onClick={handleGoogleLogin}
                className="w-full bg-white hover:bg-gray-100 text-gray-900 h-12"
                size="lg"
                variant="outline"
              >
                <Mail className="w-5 h-5" />
                Google로 계속하기
              </Button>
            </motion.div>
          </motion.div>

          {/* Divider */}
          <motion.div className="relative my-6" variants={fadeInUp}>
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-bg-primary"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-4 bg-bg-secondary text-text-muted">
                무료로 시작하세요
              </span>
            </div>
          </motion.div>

          {/* Features */}
          <motion.div
            className="space-y-2 text-sm text-text-secondary"
            variants={staggerContainer}
          >
            <motion.div className="flex items-center gap-2" variants={fadeInUp}>
              <CheckCircle className="w-4 h-4 text-success" />
              <span>무료 플랜: 월 10,000 에러 이벤트</span>
            </motion.div>
            <motion.div className="flex items-center gap-2" variants={fadeInUp}>
              <CheckCircle className="w-4 h-4 text-success" />
              <span>세션 리플레이 포함 (7일 보관)</span>
            </motion.div>
            <motion.div className="flex items-center gap-2" variants={fadeInUp}>
              <CheckCircle className="w-4 h-4 text-success" />
              <span>Discord/Slack 알림 연동</span>
            </motion.div>
          </motion.div>
        </motion.div>

        {/* Footer */}
        <motion.p
          className="text-center text-text-muted text-sm mt-6"
          variants={fadeInUp}
        >
          로그인하면{" "}
          <a href="#" className="text-primary hover:underline">
            서비스 약관
          </a>
          과{" "}
          <a href="#" className="text-primary hover:underline">
            개인정보 처리방침
          </a>
          에 동의하는 것으로 간주됩니다.
        </motion.p>
      </motion.div>
    </div>
  );
}
