import { createClient } from 'npm:@supabase/supabase-js@2'
import { JWT } from 'npm:google-auth-library@9'
import serviceAccount from '../service-account.json' with { type: 'json' }
import { Database } from "./database.types.ts";

interface Notification {
    id: string
    user_id: string
    body: string
}

interface WebhookPayload {
    type: 'INSERT'
    table: string
    record: Notification
    schema: 'public'
}

const supabase = createClient<Database>(
    Deno.env.get('SUPABASE_URL')!,
    Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!
)

function errorResponse(message: string, status: number = 400) {
    return new Response(JSON.stringify({ error: message }), {
        status,
        headers: { 'Content-Type': 'application/json' },
    })
}

Deno.serve(async (req) => {
    const payload: WebhookPayload = await req.json()

    const { data, error } = await supabase
        .from('profiles')
        .select('fcm_token')
        .eq('id', payload.record.user_id)
        .maybeSingle()

    if (error) {
        console.error('Error fetching FCM token:', error)
        return errorResponse('Error fetching FCM token', 500)
    } else if (!data || !data.fcm_token) {
        console.warn('No FCM token found for user:', payload.record.user_id)
        return errorResponse('No FCM token found for user', 404)
    }

    const fcmToken = data.fcm_token;

    const accessToken = await getAccessToken({
        clientEmail: serviceAccount.client_email,
        privateKey: serviceAccount.private_key,
    })

    const res = await fetch(
        `https://fcm.googleapis.com/v1/projects/${serviceAccount.project_id}/messages:send`,
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${accessToken}`,
            },
            body: JSON.stringify({
                message: {
                    token: fcmToken,
                    notification: {
                        title: `Notification from Supabase`,
                        body: payload.record.body,
                    },
                },
            }),
        }
    )

    const resData = await res.json()
    if (res.status < 200 || 299 < res.status) {
        throw resData
    }

    return new Response(JSON.stringify(resData), {
        headers: { 'Content-Type': 'application/json' },
    })
})

const getAccessToken = ({
    clientEmail,
    privateKey,
}: {
    clientEmail: string
    privateKey: string
}): Promise<string> => {
    return new Promise((resolve, reject) => {
        const jwtClient = new JWT({
            email: clientEmail,
            key: privateKey,
            scopes: ['https://www.googleapis.com/auth/firebase.messaging'],
        })
        jwtClient.authorize((err, tokens) => {
            if (err) {
                reject(err)
                return
            }
            resolve(tokens!.access_token!)
        })
    })
}
