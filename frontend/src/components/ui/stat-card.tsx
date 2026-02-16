"use client";

import * as React from "react";
import { motion, type Variants } from "framer-motion";
import { cn } from "@/lib/utils";
import type { LucideIcon } from "lucide-react";

interface StatCardProps {
  /** Card title/label */
  title: string;
  /** Display value */
  value: string | number;
  /** Lucide icon component */
  icon: LucideIcon;
  /** Icon color class (e.g., "text-error", "text-primary") */
  iconColor?: string;
  /** Optional subtitle text */
  subtitle?: string;
  /** Enable animations */
  animated?: boolean;
  /** Additional class names */
  className?: string;
}

const cardVariants: Variants = {
  hidden: { opacity: 0, y: 20 },
  visible: { opacity: 1, y: 0 },
};

/**
 * Reusable stat card component for displaying metrics
 *
 * @example
 * ```tsx
 * <StatCard
 *   title="Total Errors"
 *   value={formatNumber(stats.totalErrors)}
 *   icon={AlertCircle}
 *   iconColor="text-error"
 *   animated
 * />
 * ```
 */
export function StatCard({
  title,
  value,
  icon: Icon,
  iconColor = "text-text-muted",
  subtitle,
  animated = false,
  className,
}: StatCardProps) {
  const cardClassName = cn(
    "bg-bg-secondary rounded-xl p-6 border border-bg-primary hover:border-primary transition-colors",
    className
  );

  const content = (
    <>
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-text-secondary text-sm font-medium">{title}</h3>
        <Icon className={cn("w-5 h-5", iconColor)} aria-hidden="true" />
      </div>
      <div className="text-3xl font-bold text-text-primary mb-1">{value}</div>
      {subtitle && <p className="text-xs text-text-muted">{subtitle}</p>}
    </>
  );

  if (animated) {
    return (
      <motion.div
        className={cardClassName}
        variants={cardVariants}
        whileHover={{ y: -5 }}
        transition={{ type: "spring" as const, stiffness: 300 }}
      >
        {content}
      </motion.div>
    );
  }

  return <div className={cardClassName}>{content}</div>;
}

/**
 * Animated stat card with stagger support for grid layouts
 */
export function AnimatedStatCard(props: StatCardProps) {
  return <StatCard {...props} animated />;
}
