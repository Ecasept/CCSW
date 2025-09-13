import { ensureMethod, errorResponse, successResponse, supabase } from "../utils";
import { z } from "zod";
import { verifyJWT } from "./auth/verify";

const ActionsQuerySchema = z.object({
    instanceId: z.string().min(1),
    limit: z.coerce.number().int().min(1).max(100).default(10),
    offset: z.coerce.number().int().min(0).default(0),
});

export async function actions(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const res = ensureMethod(request, "GET");
    if (res.success === false) {
        return res.response;
    }

    const url = new URL(request.url);
    const queryParams = {
        instanceId: url.searchParams.get("instanceId"),
        limit: url.searchParams.get("limit"),
        offset: url.searchParams.get("offset"),
    };

    const validation = ActionsQuerySchema.safeParse(queryParams);
    if (!validation.success) {
        console.error("Invalid query parameters:", validation.error);
        return errorResponse("Invalid query parameters", 400);
    }

    const { instanceId, limit, offset } = validation.data;

    const authRes = await verifyJWT(request, env, "accessCode", instanceId);
    if (authRes.success === false) {
        return authRes.response;
    }

    const { data, error } = await supabase
        .from("value_history")
        .select("timestamp, actions")
        .eq("instance_id", instanceId)
        .order("timestamp", { ascending: false })
        .range(offset, offset + limit - 1);

    if (error) {
        console.error("Error fetching actions:", error);
        return errorResponse("Failed to fetch actions", 500);
    }

    return successResponse(data || []);
}
