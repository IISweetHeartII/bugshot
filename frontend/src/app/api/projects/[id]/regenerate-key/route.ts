import { proxyToBackend } from "@/lib/server-api";
import { NextRequest } from "next/server";
import { IdRouteContext } from "@/types/api";

export async function POST(request: NextRequest, context: IdRouteContext) {
  const { id } = await context.params;
  return proxyToBackend(request, `/api/projects/${id}/regenerate-key`);
}
