package com.github.ecasept.ccsw.network

import android.content.Context
import android.util.Log
import com.github.ecasept.ccsw.data.ApiResponse
import com.github.ecasept.ccsw.data.Snapshot
import com.github.ecasept.ccsw.data.preferences.PreferencesDataStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
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
    private val api: API
) {
    suspend fun registerToken(fcmToken: String, userId: String): ApiResponse<String> {
        @Serializable
        class RegisterTokenRequest(
            val fcmToken: String, val userId: String
        )

        val body = RegisterTokenRequest(fcmToken, userId)
        val json = Json.encodeToString(body)
        val requestBody = json.toRequestBody("application/json".toMediaType())
        return dispatch {
            api.registerToken(requestBody).toApiResponse()
        }
    }

    suspend fun getGoodHistory(
        userId: String, limit: Int = 5, offset: Int = 0
    ): ApiResponse<List<Snapshot>> {
        return api.getGoodHistory(userId, limit, offset).toApiResponse()
    }

    /**
     * Dispatches a request and handles serialization exceptions.
     * This is useful to avoid boilerplate try-catch blocks in every API call.
     */
    private suspend fun <T> dispatch(
        request: suspend () -> ApiResponse<T>
    ): ApiResponse<T> {
        try {
            return request()
        } catch (e: SerializationException) {
            Log.e("ApiClient", "Failed to parse response", e)
            return ApiResponse.error("Failed to parse response: ${e.message ?: "Unknown error"}")
        } catch (e: Exception) {
            Log.e("ApiClient", "Error registering token", e)
            return ApiResponse.error("Error registering token: ${e.message ?: "Unknown error"}")
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
    @POST("/api/register")
    suspend fun registerToken(
        @Body body: RequestBody
    ): Res<String>

    @GET("/api/goodHistory")
    suspend fun getGoodHistory(
        @Query("userId") userId: String,
        @Query("limit") limit: Int = 5,
        @Query("offset") offset: Int = 0
    ): Res<List<Snapshot>>
}

var apiSingleton: ApiClient? = null

fun createAPI(
    dataStore: PreferencesDataStore
): ApiClient {
    if (apiSingleton == null) {
        val retrofit = createRetrofit(dataStore)
        apiSingleton = ApiClient(retrofit.create(API::class.java))
    }
    return apiSingleton!!
}

fun createAPI(
    context: Context
): ApiClient {
    val dataStore = PreferencesDataStore(context)
    return createAPI(dataStore)
}