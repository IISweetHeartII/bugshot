"use client";

import { Toaster } from "sonner";

export function ToasterProvider() {
  return (
    <Toaster
      position="top-right"
      toastOptions={{
        style: {
          background: "#2F3136",
          color: "#FFFFFF",
          border: "1px solid #36393F",
        },
        className: "sonner-toast",
      }}
      theme="dark"
    />
  );
}
