/**
 * Server-side API utility for BFF pattern
 *
 * This module handles authenticated requests from Next.js API routes to the backend.
 * It validates the session and adds the internal API secret for secure server-to-server communication.
 */

import { auth } from "@/auth";
import { NextResponse } from "next/server";

const BACKEND_URL = process.env.BACKEND_URL || "http://localhost:8081";
const INTERNAL_API_SECRET = process.env.INTERNAL_API_SECRET;

export interface ProxyOptions {
  requireAuth?: boolean;
}

/**
 * Validates session and returns user ID
 * Returns null if not authenticated
 */
export async function getAuthenticatedUserId(): Promise<string | null> {
  const session = await auth();
  return session?.user?.id || null;
}

/**
 * Creates headers for backend requests with authentication
 */
export function createBackendHeaders(userId: string | null, additionalHeaders?: HeadersInit): Headers {
  const headers = new Headers(additionalHeaders);
  headers.set("Content-Type", "application/json");

  if (userId) {
    headers.set("X-User-Id", userId);
  }

  // Internal secret for server-to-server authentication
  if (INTERNAL_API_SECRET) {
    headers.set("X-Internal-Secret", INTERNAL_API_SECRET);
  }

  return headers;
}

/**
 * Makes an authenticated request to the backend
 */
export async function backendFetch(
  path: string,
  options: RequestInit = {},
  userId: string | null = null
): Promise<Response> {
  const url = `${BACKEND_URL}${path}`;
  const headers = createBackendHeaders(userId, options.headers);

  return fetch(url, {
    ...options,
    headers,
  });
}

/**
 * Proxies a request to the backend with session validation
 */
export async function proxyToBackend(
  request: Request,
  backendPath: string,
  options: ProxyOptions = { requireAuth: true }
): Promise<NextResponse> {
  // Validate session if authentication is required
  const userId = await getAuthenticatedUserId();

  if (options.requireAuth && !userId) {
    return NextResponse.json(
      { success: false, message: "Unauthorized", data: null, timestamp: new Date().toISOString() },
      { status: 401 }
    );
  }

  // Forward the request to backend
  const url = new URL(request.url);
  const backendUrl = `${BACKEND_URL}${backendPath}${url.search}`;

  const headers = createBackendHeaders(userId);

  // Forward original headers that are safe to pass
  const safeHeaders = ["accept", "accept-language"];
  safeHeaders.forEach((header) => {
    const value = request.headers.get(header);
    if (value) headers.set(header, value);
  });

  const fetchOptions: RequestInit = {
    method: request.method,
    headers,
  };

  // Include body for non-GET requests
  if (request.method !== "GET" && request.method !== "HEAD") {
    try {
      const body = await request.text();
      if (body) {
        fetchOptions.body = body;
      }
    } catch {
      // No body to forward
    }
  }

  try {
    const backendResponse = await fetch(backendUrl, fetchOptions);
    const data = await backendResponse.json();

    return NextResponse.json(data, { status: backendResponse.status });
  } catch (error) {
    console.error("Backend request failed:", error);
    return NextResponse.json(
      { success: false, message: "Backend service unavailable", data: null, timestamp: new Date().toISOString() },
      { status: 503 }
    );
  }
}
