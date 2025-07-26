import { z } from 'zod';

const GOOD_COUNT = 18; // Total number of stocks


// Action schema for stock recommendations
export const ActionSchema = z.object({
    good: z.number().int().min(0).max(GOOD_COUNT - 1),
    value: z.number().min(0), // Current value of the stock
    thresh: z.number().min(0), // Threshold value (buy or sell threshold)
    type: z.enum(['buy', 'sell']), // Action type (only buy/sell, no hold in Python code)
});

// Main data push schema
export const DataPushSchema = z.object({
    userId: z.string().min(1),
    timestamp: z.string().datetime({ offset: true }), // ISO 8601 format with timezone offset
    values: z.array(z.number()).length(GOOD_COUNT),
    bought: z.array(z.boolean()).length(GOOD_COUNT),
    actions: z.array(ActionSchema).max(GOOD_COUNT)
});

export const RegisterSchema = z.object({
    userId: z.string().min(1),
    fcmToken: z.string().min(1),
});

// Infer TypeScript types from Zod schemas
export type Action = z.infer<typeof ActionSchema>;
export type DataPushRequest = z.infer<typeof DataPushSchema>;
export type RegisterRequest = z.infer<typeof RegisterSchema>;

export interface ErrorResponse {
    success: false;
    error: string;
}

export interface SuccessResponse {
    success: true;
    data?: any; // Optional data field for success responses
}

export type ApiResponse = ErrorResponse | SuccessResponse;
