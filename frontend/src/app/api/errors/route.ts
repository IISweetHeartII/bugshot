import { proxyToBackend } from "@/lib/server-api";
import { NextRequest } from "next/server";

export async function GET(request: NextRequest) {
  return proxyToBackend(request, "/api/errors");
}
