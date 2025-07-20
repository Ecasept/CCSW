import { ErrorResponse } from "./types";

export function errorResponse(message: string, status: number = 400): Response {
    return new Response(JSON.stringify({
        success: false,
        error: message,
    } satisfies ErrorResponse), {
        status,
        headers: { "Content-Type": "application/json" }
    });
}
