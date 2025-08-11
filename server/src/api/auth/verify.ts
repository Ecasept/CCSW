import { InstanceId, JWTSchema, ValidationResult } from "../../types";
import { ensureSchema, errorResponse } from "../../utils";
import jwt from "@tsndr/cloudflare-worker-jwt";

type JWTType = "apiKey" | "accessCode";

/**
 * Verifies that the `request` contains a valid JWT for the specified `type` and `instanceId`.
 * Either returns an error response, or the instance ID that the JWT is valid for.
 */
export async function verifyJWT(request: Request, env: Env, type: JWTType, instanceId: InstanceId): Promise<ValidationResult<string>> {
    // Always return an unauthorized error, no matter what the actual error is
    const err = errorResponse("Unauthorized", 401);

    // Apply rate limiting to request verification
    const rateLimitKey = `auth:${instanceId}`;
    const { success } = await env.AUTH_RATE_LIMITER.limit({ key: rateLimitKey });
    if (!success) {
        return {
            success: false,
            response: errorResponse("Rate limit exceeded", 429)
        };
    }

    const authHeader = request.headers.get("Authorization");
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
        return {
            success: false,
            response: err
        }
    }

    const token = authHeader.slice(7).trim();
    try {
        const tk = await jwt.verify(token, env.JWT_SECRET);
        if (!tk) {
            return {
                success: false,
                response: err
            };
        }
        const res = ensureSchema(JWTSchema, tk.payload);
        if (!res.success) {
            // Invalid JWT structure
            return {
                success: false,
                response: err
            };
        }
        if (res.data.type !== type || res.data.instanceId !== instanceId) {
            return {
                success: false,
                response: err
            };
        }
        return {
            success: true,
            data: res.data.instanceId
        }
    } catch (error) {
        // Invalid token
        return {
            success: false,
            response: err
        };
    }

}
