import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Merge Tailwind CSS classes with conflict resolution
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// ============================================================================
// Internationalization Helpers
// ============================================================================

type DateInput = Date | string;

/**
 * Get user's locale from browser (auto-detect, no hardcoding)
 */
function getLocale(): string {
  if (typeof navigator !== "undefined" && navigator.language) {
    return navigator.language;
  }
  return "en";
}

/**
 * Parse date input to Date object
 */
function toDate(date: DateInput): Date {
  return typeof date === "string" ? new Date(date) : date;
}

// ============================================================================
// Date & Time Formatting (Auto-internationalized)
// ============================================================================

/**
 * Format date with automatic locale detection
 *
 * @param date - Date object or ISO string (UTC)
 * @param style - "date" (date only), "short" (compact), "long" (verbose)
 *
 * @example
 * formatDate("2025-12-07T10:00:00Z")           // "2025년 12월 7일" (Korea)
 * formatDate("2025-12-07T10:00:00Z", "short")  // "2025. 12. 7. 오후 7:00"
 * formatDate("2025-12-07T10:00:00Z", "long")   // "2025년 12월 7일 오후 7:00"
 */
export function formatDate(
  date: DateInput,
  style: "date" | "short" | "long" = "date"
): string {
  const target = toDate(date);
  const locale = getLocale();

  const options: Intl.DateTimeFormatOptions =
    style === "date"
      ? { year: "numeric", month: "long", day: "numeric" }
      : style === "short"
        ? { year: "numeric", month: "numeric", day: "numeric", hour: "2-digit", minute: "2-digit" }
        : { year: "numeric", month: "long", day: "numeric", hour: "2-digit", minute: "2-digit" };

  return target.toLocaleString(locale, options);
}

/**
 * Format relative time (e.g., "3 hours ago", "2일 전")
 * Automatically uses user's browser locale
 *
 * @example
 * // In Korea: "3시간 전", "2일 전", "1개월 전"
 * // In US: "3 hours ago", "2 days ago", "1 month ago"
 */
export function formatRelativeTime(date: DateInput): string {
  const target = toDate(date);
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - target.getTime()) / 1000);

  const locale = getLocale();
  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: "auto" });

  // Find the appropriate unit
  const units: { unit: Intl.RelativeTimeFormatUnit; seconds: number }[] = [
    { unit: "year", seconds: 31536000 },
    { unit: "month", seconds: 2592000 },
    { unit: "week", seconds: 604800 },
    { unit: "day", seconds: 86400 },
    { unit: "hour", seconds: 3600 },
    { unit: "minute", seconds: 60 },
    { unit: "second", seconds: 1 },
  ];

  for (const { unit, seconds } of units) {
    if (diffInSeconds >= seconds) {
      const value = Math.floor(diffInSeconds / seconds);
      return rtf.format(-value, unit);
    }
  }

  return rtf.format(0, "second"); // "just now" / "방금"
}

// ============================================================================
// Number Formatting (Auto-internationalized)
// ============================================================================

/**
 * Format number with locale-aware separators
 *
 * @example
 * // In Korea: "1,234,567"
 * // In Germany: "1.234.567"
 */
export function formatNumber(num: number): string {
  return num.toLocaleString(getLocale());
}

// ============================================================================
// Severity & Status Helpers
// ============================================================================

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

/**
 * Get Badge variant based on severity
 */
export function getSeverityBadgeVariant(
  severity: string
): "destructive" | "default" | "secondary" | "outline" {
  switch (severity.toUpperCase()) {
    case "CRITICAL":
    case "HIGH":
      return "destructive";
    case "MEDIUM":
      return "default";
    case "LOW":
      return "secondary";
    default:
      return "outline";
  }
}

/**
 * Get status display info
 */
export function getStatusInfo(status: string): { label: string; color: string } {
  switch (status.toUpperCase()) {
    case "UNRESOLVED":
      return { label: "미해결", color: "text-red-400" };
    case "RESOLVED":
      return { label: "해결됨", color: "text-green-400" };
    case "IGNORED":
      return { label: "무시됨", color: "text-gray-400" };
    default:
      return { label: status, color: "text-gray-400" };
  }
}

/**
 * Extract error message from unknown error
 */
export function getErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  if (typeof error === "string") {
    return error;
  }
  if (error && typeof error === "object" && "message" in error) {
    return String((error as { message: unknown }).message);
  }
  return "알 수 없는 오류가 발생했습니다.";
}

// ============================================================================
// File Size Formatting
// ============================================================================

/**
 * Format file size to human readable string
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i];
}
