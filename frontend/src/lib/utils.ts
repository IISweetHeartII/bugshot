import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Merge Tailwind CSS classes with conflict resolution
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * Format date to relative time (e.g., "2 hours ago")
 */
export function formatRelativeTime(date: Date | string): string {
  const now = new Date();
  const target = typeof date === "string" ? new Date(date) : date;
  const diffInSeconds = Math.floor((now.getTime() - target.getTime()) / 1000);

  if (diffInSeconds < 60) {
    return "방금 전";
  }

  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) {
    return `${diffInMinutes}분 전`;
  }

  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) {
    return `${diffInHours}시간 전`;
  }

  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 7) {
    return `${diffInDays}일 전`;
  }

  const diffInWeeks = Math.floor(diffInDays / 7);
  if (diffInWeeks < 4) {
    return `${diffInWeeks}주 전`;
  }

  const diffInMonths = Math.floor(diffInDays / 30);
  return `${diffInMonths}개월 전`;
}

/**
 * Get severity badge color
 */
export function getSeverityColor(severity: string): string {
  switch (severity.toUpperCase()) {
    case "CRITICAL":
      return "bg-severity-critical text-white";
    case "HIGH":
      return "bg-severity-high text-gray-900";
    case "MEDIUM":
      return "bg-severity-medium text-gray-900";
    case "LOW":
      return "bg-severity-low text-white";
    default:
      return "bg-gray-500 text-white";
  }
}

/**
 * Get severity emoji
 */
export function getSeverityEmoji(severity: string): string {
  switch (severity.toUpperCase()) {
    case "CRITICAL":
      return "🔴";
    case "HIGH":
      return "🟡";
    case "MEDIUM":
      return "🟢";
    case "LOW":
      return "⚪";
    default:
      return "⚫";
  }
}

/**
 * Format number with commas (e.g., 1234 -> "1,234")
 */
export function formatNumber(num: number): string {
  return num.toLocaleString("ko-KR");
}

/**
 * Truncate text with ellipsis
 */
export function truncate(text: string, maxLength: number): string {
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + "...";
}

/**
 * Copy text to clipboard
 */
export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch (error) {
    console.error("Failed to copy:", error);
    return false;
  }
}
