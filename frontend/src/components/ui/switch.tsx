"use client";

import * as React from "react";
import { cn } from "@/lib/utils";

interface SwitchProps {
  /** Controlled checked state */
  checked?: boolean;
  /** Default checked state for uncontrolled usage */
  defaultChecked?: boolean;
  /** Change handler */
  onCheckedChange?: (checked: boolean) => void;
  /** Disable the switch */
  disabled?: boolean;
  /** Accessible label */
  "aria-label"?: string;
  /** Additional class names */
  className?: string;
  /** Input name for form submission */
  name?: string;
  /** Input id */
  id?: string;
}

/**
 * Toggle switch component for boolean settings
 *
 * @example
 * ```tsx
 * // Controlled
 * <Switch
 *   checked={emailEnabled}
 *   onCheckedChange={setEmailEnabled}
 *   aria-label="Enable email notifications"
 * />
 *
 * // Uncontrolled
 * <Switch defaultChecked aria-label="Enable feature" />
 * ```
 */
export function Switch({
  checked,
  defaultChecked,
  onCheckedChange,
  disabled = false,
  className,
  name,
  id,
  "aria-label": ariaLabel,
}: SwitchProps) {
  const [internalChecked, setInternalChecked] = React.useState(defaultChecked ?? false);

  const isControlled = checked !== undefined;
  const isChecked = isControlled ? checked : internalChecked;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newChecked = e.target.checked;

    if (!isControlled) {
      setInternalChecked(newChecked);
    }

    onCheckedChange?.(newChecked);
  };

  return (
    <label
      className={cn(
        "relative inline-flex items-center cursor-pointer",
        disabled && "cursor-not-allowed opacity-50",
        className
      )}
    >
      <input
        type="checkbox"
        className="sr-only peer"
        checked={isChecked}
        onChange={handleChange}
        disabled={disabled}
        name={name}
        id={id}
        aria-label={ariaLabel}
      />
      <div
        className={cn(
          "w-11 h-6 bg-bg-tertiary rounded-full peer",
          "peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-primary peer-focus:ring-offset-2 peer-focus:ring-offset-bg-secondary",
          "peer-checked:after:translate-x-full peer-checked:after:border-white",
          "after:content-[''] after:absolute after:top-[2px] after:left-[2px]",
          "after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all",
          "peer-checked:bg-primary"
        )}
      />
    </label>
  );
}
