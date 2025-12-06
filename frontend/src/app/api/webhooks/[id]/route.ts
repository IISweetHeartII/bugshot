import { proxyToBackend } from "@/lib/server-api";
import { NextRequest } from "next/server";
import { IdRouteContext } from "@/types/api";

export async function PUT(request: NextRequest, context: IdRouteContext) {
  const { id } = await context.params;
  return proxyToBackend(request, `/api/webhooks/${id}`);
}

export async function DELETE(request: NextRequest, context: IdRouteContext) {
  const { id } = await context.params;
  return proxyToBackend(request, `/api/webhooks/${id}`);
}
