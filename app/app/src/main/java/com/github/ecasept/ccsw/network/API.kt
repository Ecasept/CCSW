package com.github.ecasept.ccsw.network

import android.content.Context
import android.util.Log
import com.github.ecasept.ccsw.data.ActionsSnapshot
import com.github.ecasept.ccsw.data.ApiResponse
import com.github.ecasept.ccsw.data.Snapshot
import com.github.ecasept.ccsw.data.preferences.PreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Converts a Retrofit response object to an api response for easier error handling.
 */
inline fun <reified T> Response<ApiResponse<T>>.toApiResponse(): ApiResponse<T> {
    return if (isSuccessful) {
        body() ?: ApiResponse.error("Empty response body")
    } else {
        val errorBody = errorBody()?.string()
        val errorMessage: String
        val errorCode = code()
        if (errorBody != null) {
            try {
                // Attempt to parse the error body as ApiResponse
                val errorResponse = Json.decodeFromString<ApiResponse<T>>(errorBody)
                errorMessage =
                    errorResponse.on(success = { throw SerializationException("Expected error response, but got success") },
                        error = { it })
                return ApiResponse.error(errorMessage)
            } catch (e: SerializationException) {
                Log.e("API", "Failed to parse error response", e)
                return ApiResponse.error("Failed to parse error response: ${e.message ?: "Unknown error"}")
            }
        } else {
            return ApiResponse.error(
                "HTTP error $errorCode: ${message() ?: "Unknown error"}"
            )
        }
    }
}

typealias Res<T> = Response<ApiResponse<T>>

class ApiClient(
    private val api: API,
    private val dataStore: PreferencesDataStore
) {
    /**
     * Returns an error response indicating that the user is not logged in.
     * This is used to avoid boilerplate code in every API call that requires authentication.
     */
    private fun <T> notLoggedInErr() = ApiResponse.error<T>("Not logged in")

    private suspend fun getSessionToken(): String? {
        return dataStore.prefs.first().sessionToken
    }

    private suspend fun getInstanceId(): String? {
        return dataStore.prefs.first().instanceId
    }

    /**
     * Converts any object [body] to a [RequestBody] for use in Retrofit requests.
     */
    private inline fun <reified T> toRequestBody(body: T): RequestBody {
        return Json.encodeToString(body)
            .toRequestBody("application/json".toMediaType())
    }

    private fun authHeader(sessionToken: String): String {
        return "Bearer $sessionToken"
    }

    suspend fun createSession(instanceId: String, accessCode: String): ApiResponse<String> {
        @Serializable
        class Body(
            val accessCode: String,
            val instanceId: String,
            val type: String,
        )

        val requestBody = toRequestBody(Body(accessCode, instanceId, "accessCode"))

        return dispatch {
            api.createSession(requestBody)
        }
    }

    suspend fun addDeviceToken(
        fcmToken: String,
        instanceId: String,
        sessionToken: String?
    ): ApiResponse<String> {
        @Serializable
        class Body(
            val fcmToken: String, val instanceId: String
        )

        val st = sessionToken ?: getSessionToken() ?: return notLoggedInErr()

        val requestBody = toRequestBody(Body(fcmToken, instanceId))

        return dispatch {
            api.addDeviceToken(requestBody, authHeader(st))
        }
    }

    suspend fun getGoodHistory(
        limit: Int = 5, offset: Int = 0
    ): ApiResponse<List<Snapshot>> {
        val sessionToken = getSessionToken() ?: return notLoggedInErr()
        val instanceId = getInstanceId() ?: return notLoggedInErr()

        return dispatch {
            api.getGoodHistory(instanceId, limit, offset, authHeader(sessionToken))
        }
    }

    suspend fun getActions(
        limit: Int = 5, offset: Int = 0
    ): ApiResponse<List<ActionsSnapshot>> {
        val sessionToken = getSessionToken() ?: return notLoggedInErr()
        val instanceId = getInstanceId() ?: return notLoggedInErr()

        return dispatch {
            api.getActions(instanceId, limit, offset, authHeader(sessionToken))
        }
    }

    /**
     * Dispatches a request and handles exceptions.
     * This is useful to avoid boilerplate try-catch blocks in every API call.
     */
    private inline fun <reified T> dispatch(
        request: () -> Res<T>
    ): ApiResponse<T> {
        try {
            val res = request()
            val url = res.raw().request.url.toString()
            val response = res.toApiResponse()
            val str = response.toResult().toString()
            Log.d("ApiClient", "Request to $url returned: $str")
            return response
        } catch (e: SerializationException) {
            Log.e("ApiClient", "Failed to parse response", e)
            return ApiResponse.error("Failed to parse response: ${e.message ?: "Unknown error"}")
        } catch (e: Exception) {
            Log.e("ApiClient", "Error during API call", e)
            return ApiResponse.error("Error during API call: ${e.message ?: "Unknown error"}")
        }
    }
}

/** * API interface for the CCSW application.
 *
 * Note on return types:
 * - The CCSW Api always returns an `ApiResponse<T>` for easy error handling.
 * - Retrofit wraps this into a `Response<ApiResponse<T>>` to access network attributes
 * - The Res<T> type alias is used to simplify the return type in function signatures.
 */
interface API {
    @POST("/api/token")
    suspend fun addDeviceToken(
        @Body body: RequestBody,
        @Header("Authorization") authHeader: String
    ): Res<String>

    @GET("/api/goodHistory")
    suspend fun getGoodHistory(
        @Query("instanceId") instanceId: String,
        @Query("limit") limit: Int = 5,
        @Query("offset") offset: Int = 0,
        @Header("Authorization") authHeader: String
    ): Res<List<Snapshot>>

    @POST("/api/auth/session")
    suspend fun createSession(
        @Body body: RequestBody,
    ): Res<String>

    @GET("/api/actions")
    suspend fun getActions(
        @Query("instanceId") instanceId: String,
        @Query("limit") limit: Int = 5,
        @Query("offset") offset: Int = 0,
        @Header("Authorization") authHeader: String
    ): Res<List<ActionsSnapshot>>
}

var apiSingleton: ApiClient? = null

fun createAPI(
    dataStore: PreferencesDataStore
): ApiClient {
    if (apiSingleton == null) {
        val retrofit = createRetrofit(dataStore)
        apiSingleton = ApiClient(retrofit.create(API::class.java), dataStore)
    }
    return apiSingleton!!
}

fun createAPI(
    context: Context
): ApiClient {
    val dataStore = PreferencesDataStore(context)
    return createAPI(dataStore)
}