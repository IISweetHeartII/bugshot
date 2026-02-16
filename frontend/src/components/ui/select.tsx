"use client";

import * as React from "react";
import { cn } from "@/lib/utils";

interface SelectOption {
  value: string;
  label: string;
}

interface SelectProps extends Omit<React.SelectHTMLAttributes<HTMLSelectElement>, 'onChange'> {
  /** Select options */
  options: readonly SelectOption[] | SelectOption[];
  /** Value change handler */
  onChange?: (value: string) => void;
  /** Placeholder option (shown when value is empty) */
  placeholder?: string;
  /** Additional class names */
  className?: string;
}

/**
 * Styled select component matching the design system
 *
 * @example
 * ```tsx
 * <Select
 *   value={environment}
 *   onChange={setEnvironment}
 *   options={ENVIRONMENTS}
 *   aria-label="환경 선택"
 * />
 * ```
 */
export function Select({
  options,
  onChange,
  placeholder,
  className,
  value,
  ...props
}: SelectProps) {
  const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    onChange?.(e.target.value);
  };

  return (
    <select
      value={value}
      onChange={handleChange}
      className={cn(
        "flex h-10 w-full rounded-lg border border-bg-primary bg-bg-tertiary px-3 py-2",
        "text-sm text-text-primary ring-offset-bg-secondary",
        "focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2",
        "disabled:cursor-not-allowed disabled:opacity-50",
        className
      )}
      {...props}
    >
      {placeholder && (
        <option value="" disabled>
          {placeholder}
        </option>
      )}
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  );
}

/**
 * Native select with filter styling (for filter dropdowns)
 */
export function FilterSelect({
  options,
  onChange,
  placeholder,
  className,
  value,
  ...props
}: SelectProps) {
  const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    onChange?.(e.target.value);
  };

  return (
    <select
      value={value}
      onChange={handleChange}
      className={cn(
        "bg-bg-tertiary border border-bg-primary rounded-lg px-4 py-2",
        "text-text-primary focus:outline-none focus:border-primary",
        className
      )}
      {...props}
    >
      {placeholder && <option value="">{placeholder}</option>}
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  );
}
