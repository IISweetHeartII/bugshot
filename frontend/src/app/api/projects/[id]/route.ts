import { proxyToBackend } from "@/lib/server-api";
import { NextRequest } from "next/server";

type RouteContext = { params: Promise<{ id: string }> };

export async function GET(request: NextRequest, context: RouteContext) {
  const { id } = await context.params;
  return proxyToBackend(request, `/api/projects/${id}`);
}

export async function PUT(request: NextRequest, context: RouteContext) {
  const { id } = await context.params;
  return proxyToBackend(request, `/api/projects/${id}`);
}

export async function DELETE(request: NextRequest, context: RouteContext) {
  const { id } = await context.params;
  return proxyToBackend(request, `/api/projects/${id}`);
}
