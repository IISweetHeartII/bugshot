import { proxyToBackend } from "@/lib/server-api";
import { NextRequest } from "next/server";

type RouteContext = { params: Promise<{ errorId: string }> };

export async function GET(request: NextRequest, context: RouteContext) {
  const { errorId } = await context.params;
  return proxyToBackend(request, `/api/replays/${errorId}/download-url`);
}
