import z from "zod";
import bcrypt from "bcryptjs";
import { ensureRequest, errorResponse, successResponse, supabase } from "../../utils";
import { InstanceIdSchema, JWTPayload } from "../../types";
import jwt from "@tsndr/cloudflare-worker-jwt"


const CreateSessionWithApiKeySchema = z.object({
    type: z.literal("apiKey"),
    apiKey: z.string().min(1, "API key is required"),
    instanceId: InstanceIdSchema,
});
const CreateSessionWithAccessCodeSchema = z.object({
    type: z.literal("accessCode"),
    accessCode: z.string().min(1, "Access code is required"),
    instanceId: InstanceIdSchema,
});

type CreateSessionWithApiKey = z.infer<typeof CreateSessionWithApiKeySchema>;
type CreateSessionWithAccessCode = z.infer<typeof CreateSessionWithAccessCodeSchema>;

const CreateSessionSchema = z.union(
    [CreateSessionWithApiKeySchema, CreateSessionWithAccessCodeSchema])

export async function createSession(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const res = await ensureRequest(request, "POST", CreateSessionSchema);
    if (res.success === false) {
        return res.response;
    }

    const rateLimitKey = `createSession:${res.data.instanceId}`;
    const { success } = await env.SESSION_RATE_LIMITER.limit({ key: rateLimitKey })
    if (!success) {
        return errorResponse("Rate limit exceeded", 429);
    }

    if (res.data.type === "apiKey") {
        return await createSessionWithApiKey(res.data, env);
    }
    else if (res.data.type === "accessCode") {
        return await createSessionWithAccessCode(res.data, env);
    }
    return errorResponse("Invalid session type", 400);
}

function nowEpoch(): number { return Math.floor(Date.now() / 1000); }

async function issueToken(payloadType: "apiKey" | "accessCode", instanceId: string, env: Env): Promise<string> {
    const iat = nowEpoch();
    const payload: JWTPayload = {
        type: payloadType,
        instanceId,
        iat
    };
    return await jwt.sign(payload, env.JWT_SECRET);
}

async function createSessionWithApiKey(data: CreateSessionWithApiKey, env: Env): Promise<Response> {
    // Fetch stored hash for the instance and compare
    const { data: instance, error } = await supabase
        .from("instances")
        .select("id, api_key_hash")
        .eq("id", data.instanceId)
        .maybeSingle();
    if (error) {
        console.error("DB error fetching instance for API key auth:", error);
        return errorResponse("Internal Server Error", 500);
    }
    if (!instance) {
        return errorResponse("Invalid API key or instance ID", 401);
    }

    const ok = await bcrypt.compare(data.apiKey, instance.api_key_hash);
    if (!ok) {
        return errorResponse("Invalid API key or instance ID", 401);
    }

    const token = await issueToken("apiKey", data.instanceId, env);
    return successResponse(token);
}

async function createSessionWithAccessCode(data: CreateSessionWithAccessCode, env: Env): Promise<Response> {
    // Fetch stored hash for the instance and compare
    const { data: instance, error } = await supabase
        .from("instances")
        .select("id, access_code_hash")
        .eq("id", data.instanceId)
        .maybeSingle();

    if (error) {
        console.error("DB error fetching instance for access code auth:", error);
        return errorResponse("Internal Server Error", 500);
    }

    if (!instance) {
        return errorResponse("Invalid access code or instance ID", 401);
    }

    const ok = await bcrypt.compare(data.accessCode, instance.access_code_hash);
    if (!ok) {
        return errorResponse("Invalid access code or instance ID", 401);
    }

    const token = await issueToken("accessCode", data.instanceId, env);
    return successResponse(token);
}
