import { Type } from '@google/genai';
import { z } from 'zod';
import { minimum } from 'zod/v4-mini';

const GOOD_COUNT = 18; // Total number of stocks
const GOOD_MIN = 1; // Minimum possible value for a stock

export const InstanceIdSchema = z
    .string()
    .min(1, "ID is required")
    .max(50, "ID must be at most 50 characters")
    .regex(/^[a-z0-9-]+$/, "ID can only contain lowercase letters, numbers, and hyphens")
    .refine((v) => !v.startsWith("-") && !v.endsWith("-"), "ID cannot start or end with a hyphen");

// Action schema for stock recommendations
export const ActionSchema = z.object({
    good: z.number().int().min(0).max(GOOD_COUNT - 1),
    value: z.number().min(GOOD_MIN), // Current value of the stock
    thresh: z.number().min(GOOD_MIN), // Threshold value (buy or sell threshold)
    type: z.enum(['buy', 'sell', 'missed_buy', 'missed_sell']),
});

// Good item schema combining value and bought status
export const GoodSchema = z.object({
    value: z.number().min(GOOD_MIN), // Current value of the stock
    bought: z.boolean() // Whether the stock is bought or not
});

export const TokenSendSchema = z.object({
    instanceId: InstanceIdSchema,
    fcmToken: z.string().min(1),
});

// Main data push schema
export const DataPushSchema = z.object({
    timestamp: z.string().datetime({ offset: true }), // ISO 8601 format with timezone offset
    goods: z.array(GoodSchema).length(GOOD_COUNT),
    actions: z.array(ActionSchema).max(GOOD_COUNT),
    instanceId: InstanceIdSchema,
});

export const ImageProcessSchema = z.object({
    image: z.string().min(1), // Base64 encoded image string
    instanceId: InstanceIdSchema,
});



export const JWTSchema = z.union([
    z.object({
        type: z.literal("apiKey"),
        instanceId: InstanceIdSchema,
        iat: z.number().int().nonnegative(),
    }),
    z.object({
        type: z.literal("accessCode"),
        instanceId: InstanceIdSchema,
        iat: z.number().int().nonnegative(),
    }),
]);

export type JWTPayload = z.infer<typeof JWTSchema>;
export type InstanceId = z.infer<typeof InstanceIdSchema>;
export type ValidationResult<T> = { success: true; data: T } | { success: false; response: Response };


// Infer TypeScript types from Zod schemas
export type Action = z.infer<typeof ActionSchema>;

export interface ErrorResponse {
    success: false;
    error: string;
}

export interface SuccessResponse {
    success: true;
    data?: any; // Optional data field for success responses
}

export type ApiResponse = ErrorResponse | SuccessResponse;

export const geminiResponseSchema = {
    type: Type.OBJECT,
    properties: {
        goods: {
            type: Type.ARRAY,
            items: {
                type: Type.OBJECT,
                properties: {
                    value: {
                        type: Type.NUMBER,
                        minimum: GOOD_MIN,
                    },
                    bought: { type: Type.BOOLEAN }
                },
                required: ["value", "bought"]
            },
            minItems: GOOD_COUNT,
            maxItems: GOOD_COUNT,
            nullable: false,
        },
    },
    nullable: true,
}

export const geminiPrompt = `
You are provided with an image of goods on a stock market.
From left to right, top to bottom, parse the information into an object.
The object should contain the following fields:
- "goods": an array of objects, each containing:
    - "value": a float representing the current value of the stock
    - "bought": a boolean indicating whether the stock is bought (true) or not (false)
Return null if the image can't be parsed.
Example:
{
        "goods": [
                {"value": 10.43, "bought": true},
                {"value": 28.84, "bought": false},
                {"value": 54.12, "bought": false},
                {"value": 22.98, "bought": true},
                ...
        ]
}
`.trim();
