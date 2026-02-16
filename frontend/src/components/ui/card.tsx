import * as React from "react";
import { cn } from "@/lib/utils";

/**
 * Card component for consistent container styling
 * Replaces repeated patterns like: bg-bg-secondary rounded-xl p-6 border border-bg-primary
 */

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  /** Card size variant */
  size?: "sm" | "md" | "lg";
  /** Whether the card has hover effects */
  hoverable?: boolean;
  /** Additional class names */
  className?: string;
  children: React.ReactNode;
}

const sizeStyles = {
  sm: "rounded-lg p-4",
  md: "rounded-lg p-6",
  lg: "rounded-xl p-6",
} as const;

/**
 * Base Card component with consistent styling
 *
 * @example
 * ```tsx
 * <Card>
 *   <h2>Title</h2>
 *   <p>Content</p>
 * </Card>
 *
 * <Card size="sm" hoverable>
 *   <p>Clickable card</p>
 * </Card>
 * ```
 */
export function Card({
  size = "lg",
  hoverable = false,
  className,
  children,
  ...props
}: CardProps) {
  return (
    <div
      className={cn(
        "bg-bg-secondary border border-bg-primary",
        sizeStyles[size],
        hoverable && "cursor-pointer hover:border-primary transition-colors",
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
}

/**
 * Card Header component for title sections
 */
interface CardHeaderProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  className?: string;
}

export function CardHeader({ className, children, ...props }: CardHeaderProps) {
  return (
    <div
      className={cn("flex items-center justify-between mb-4", className)}
      {...props}
    >
      {children}
    </div>
  );
}

/**
 * Card Title component
 */
interface CardTitleProps extends React.HTMLAttributes<HTMLHeadingElement> {
  children: React.ReactNode;
  className?: string;
  as?: "h1" | "h2" | "h3" | "h4";
}

export function CardTitle({
  as: Component = "h2",
  className,
  children,
  ...props
}: CardTitleProps) {
  return (
    <Component
      className={cn("text-lg font-semibold text-text-primary", className)}
      {...props}
    >
      {children}
    </Component>
  );
}

/**
 * Card Content component for body content
 */
interface CardContentProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  className?: string;
}

export function CardContent({
  className,
  children,
  ...props
}: CardContentProps) {
  return (
    <div className={cn(className)} {...props}>
      {children}
    </div>
  );
}

/**
 * Card Footer component
 */
interface CardFooterProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  className?: string;
}

export function CardFooter({
  className,
  children,
  ...props
}: CardFooterProps) {
  return (
    <div
      className={cn("flex items-center gap-2 mt-4", className)}
      {...props}
    >
      {children}
    </div>
  );
}
