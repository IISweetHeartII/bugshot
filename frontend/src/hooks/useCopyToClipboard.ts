"use client";

import { useState, useCallback, useRef, useEffect } from "react";
import { copyToClipboard } from "@/lib/utils";
import { MESSAGES } from "@/lib/constants";
import { toast } from "sonner";

interface UseCopyToClipboardOptions {
  /** Duration in ms before resetting copied state (default: 2000) */
  resetDelay?: number;
  /** Custom success message (default: MESSAGES.SUCCESS.API_KEY_COPIED) */
  successMessage?: string;
  /** Show toast on success (default: true) */
  showToast?: boolean;
}

interface UseCopyToClipboardReturn {
  /** Whether content was recently copied */
  copied: boolean;
  /** The value that was copied (useful when tracking multiple items) */
  copiedValue: string | null;
  /** Copy function - returns true if successful */
  copy: (text: string) => Promise<boolean>;
  /** Reset copied state manually */
  reset: () => void;
}

/**
 * Hook for copying text to clipboard with state management
 *
 * @example
 * ```tsx
 * // Simple usage
 * const { copied, copy } = useCopyToClipboard();
 * <Button onClick={() => copy(apiKey)}>
 *   {copied ? "Copied!" : "Copy"}
 * </Button>
 *
 * // Track which item was copied (for lists)
 * const { copiedValue, copy } = useCopyToClipboard();
 * {projects.map(p => (
 *   <Button onClick={() => copy(p.apiKey)}>
 *     {copiedValue === p.apiKey ? "Copied!" : "Copy"}
 *   </Button>
 * ))}
 * ```
 */
export function useCopyToClipboard(
  options: UseCopyToClipboardOptions = {}
): UseCopyToClipboardReturn {
  const {
    resetDelay = 2000,
    successMessage = MESSAGES.SUCCESS.API_KEY_COPIED,
    showToast = true,
  } = options;

  const [copiedValue, setCopiedValue] = useState<string | null>(null);
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  const reset = useCallback(() => {
    setCopiedValue(null);
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }
  }, []);

  const copy = useCallback(
    async (text: string): Promise<boolean> => {
      const success = await copyToClipboard(text);

      if (success) {
        // Clear any existing timeout
        if (timeoutRef.current) {
          clearTimeout(timeoutRef.current);
        }

        setCopiedValue(text);

        if (showToast) {
          toast.success(successMessage);
        }

        // Auto-reset after delay
        timeoutRef.current = setTimeout(() => {
          setCopiedValue(null);
          timeoutRef.current = null;
        }, resetDelay);
      }

      return success;
    },
    [resetDelay, successMessage, showToast]
  );

  return {
    copied: copiedValue !== null,
    copiedValue,
    copy,
    reset,
  };
}
