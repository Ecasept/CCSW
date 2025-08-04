import { ensureMethod, ensureRequest, supabase } from "../utils";
import { z } from 'zod';
import { errorResponse, successResponse } from "../utils";

// Schema for goodHistory query parameters
const GoodHistoryQuerySchema = z.object({
    userId: z.string().min(1),
    limit: z.number().int().min(1).max(100).default(5),
    offset: z.number().int().min(0).default(0)
});

export async function goodHistory(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const res = ensureMethod(request, "GET");
    if (res.success === false) {
        return res.response;
    }

    const url = new URL(request.url);
    const queryParams = {
        userId: url.searchParams.get('userId'),
        limit: url.searchParams.get('limit'),
        offset: url.searchParams.get('offset')
    };

    // Validate query parameters
    const validation = GoodHistoryQuerySchema.safeParse(queryParams);
    if (!validation.success) {
        return errorResponse("Invalid query parameters", 400);
    }

    const { userId, limit, offset } = validation.data;

    // Query the database for user's value history
    const { data, error } = await supabase
        .from("value_history")
        .select("timestamp, goods")
        .eq("userId", userId)
        .order("timestamp", { ascending: false })
        .range(offset, offset + limit - 1);

    if (error) {
        console.error("Error fetching good history:", error);
        return errorResponse("Failed to fetch good history", 500);
    }

    return successResponse(data || []);
}
