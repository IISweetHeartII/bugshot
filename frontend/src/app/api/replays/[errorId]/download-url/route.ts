import { proxyToBackend } from "@/lib/server-api";
import { NextRequest } from "next/server";
import { ErrorIdRouteContext } from "@/types/api";

export async function GET(request: NextRequest, context: ErrorIdRouteContext) {
  const { errorId } = await context.params;
  return proxyToBackend(request, `/api/replays/${errorId}/download-url`);
}
