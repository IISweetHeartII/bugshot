import type { Config } from "tailwindcss";

export default {
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        // Brand Colors (Discord-inspired)
        primary: {
          DEFAULT: "#5865F2",
          dark: "#4752C4",
          light: "#7289DA",
        },
        // Background Colors
        bg: {
          primary: "#36393F",
          secondary: "#2F3136",
          tertiary: "#202225",
        },
        // Text Colors
        text: {
          primary: "#FFFFFF",
          secondary: "#B9BBBE",
          muted: "#72767D",
        },
        // Severity Colors
        severity: {
          critical: "#ED4245",
          high: "#FEE75C",
          medium: "#57F287",
          low: "#99AAB5",
        },
        // Status Colors
        success: "#3BA55D",
        warning: "#FAA81A",
        error: "#ED4245",
        info: "#5865F2",
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
        mono: ["JetBrains Mono", "Fira Code", "monospace"],
      },
    },
  },
  plugins: [],
} satisfies Config;
