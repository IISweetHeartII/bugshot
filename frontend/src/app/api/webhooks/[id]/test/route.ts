import { proxyToBackend } from "@/lib/server-api";
import { NextRequest } from "next/server";

type RouteContext = { params: Promise<{ id: string }> };

export async function POST(request: NextRequest, context: RouteContext) {
  const { id } = await context.params;
  return proxyToBackend(request, `/api/webhooks/${id}/test`);
}
