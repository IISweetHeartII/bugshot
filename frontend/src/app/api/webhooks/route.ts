import { proxyToBackend } from "@/lib/server-api";
import { NextRequest } from "next/server";

export async function GET(request: NextRequest) {
  return proxyToBackend(request, "/api/webhooks");
}

export async function POST(request: NextRequest) {
  return proxyToBackend(request, "/api/webhooks");
}
