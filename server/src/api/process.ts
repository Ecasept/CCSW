import { GoogleGenAI } from "@google/genai";
import { geminiPrompt, geminiResponseSchema, ImageProcessSchema } from "../types";
import { ensureRequest, errorResponse, successResponse } from "../utils";
import { mime, success } from "zod/v4";
import { verifyJWT } from "./auth/verify";

export async function processImg(
    request: Request,
    env: Env,
    ctx: ExecutionContext
): Promise<Response> {
    const res = await ensureRequest(request, "POST", ImageProcessSchema);
    if (res.success === false) {
        return res.response;
    }
    const { image, instanceId } = res.data;

    const authRes = await verifyJWT(request, env, "apiKey", instanceId);
    if (authRes.success === false) {
        return authRes.response;
    }

    const ai = new GoogleGenAI({
        apiKey: env.AI_STUDIO_API_KEY,
    });

    const contents = [
        {
            inlineData: {
                mimeType: "image/png",
                data: image,
            }
        },
        {
            text: geminiPrompt,
        },
    ];

    const response = await ai.models.generateContent({
        model: "gemini-2.5-flash",
        contents,
        config: {
            responseMimeType: "application/json",
            responseSchema: geminiResponseSchema
        }
    });
    const parsed = response.text
    if (!parsed) {
        return errorResponse("Failed to get response from Gemini", 500);
    } else if (parsed == "null") {
        return errorResponse("Could not parse image", 400);
    }
    return successResponse(JSON.parse(parsed));
}
