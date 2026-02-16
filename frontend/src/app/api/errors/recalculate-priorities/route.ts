import { proxyToBackend } from "@/lib/server-api";
import { NextRequest } from "next/server";

export async function POST(request: NextRequest) {
  return proxyToBackend(request, "/api/errors/recalculate-priorities");
}
