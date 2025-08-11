import z from "zod";
import { ensureRequest, errorResponse, successResponse, supabase } from "../../utils";
import bcrypt from "bcryptjs";
import { InstanceIdSchema } from "../../types";

const CreateInstanceSchema = z.object({
    name: z.string().min(1, "Name is required"),
    id: InstanceIdSchema,
});

export async function createInstance(
    request: Request,
    env: Env,
    ctx: ExecutionContext
): Promise<Response> {
    const res = await ensureRequest(request, "POST", CreateInstanceSchema);
    if (res.success === false) {
        return res.response;
    }
    const { name, id } = res.data;
    const instanceId = id.trim();

    // Check if the instance already exists
    const { data, error } = await supabase
        .from("instances")
        .select("id")
        .eq("id", instanceId)
        .maybeSingle();

    if (error) {
        console.error("Error checking instance existence:", error);
        return errorResponse("Failed to create instance", 500);
    }
    if (data) {
        return errorResponse("Instance with this id already exists", 400);
    }

    const apiKey = crypto.randomUUID();
    const accessCodeUUID = crypto.randomUUID();
    const accessCode = accessCodeUUID.slice(0, 8); // Generate a random access code of 8 characters

    const accessCodeHash = await bcrypt.hash(accessCode, 10);
    const apiKeyHash = await bcrypt.hash(apiKey, 10);

    const { data: newInstance, error: insertError } = await supabase
        .from("instances")
        .insert({
            id: instanceId,
            name,
            api_key_hash: apiKeyHash,
            access_code_hash: accessCodeHash,
        })
        .select()
        .single();

    if (insertError) {
        console.error("Error creating instance:", insertError);
        return errorResponse("Failed to create instance", 500);
    }

    // Return success response with created instance data
    return successResponse({
        apiKey,
        accessCode,
        instance: {
            id: newInstance.id,
            name: newInstance.name,
        }
    });
}
