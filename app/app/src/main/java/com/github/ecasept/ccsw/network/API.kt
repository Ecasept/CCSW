package com.github.ecasept.ccsw.network

import android.content.Context
import com.github.ecasept.ccsw.data.GoodHistory
import com.github.ecasept.ccsw.data.preferences.PreferencesDataStore
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

class ApiClient(
    private val api: API
) {
    suspend fun registerToken(fcmToken: String, userId: String): Response<Unit> {
        val requestBody = RegisterTokenRequestBody(fcmToken, userId)
        return api.registerToken(requestBody)
    }

    suspend fun getGoodHistory(
        userId: String,
        limit: Int = 5,
        offset: Int = 0
    ): Response<List<GoodHistory>> {
        return api.getGoodHistory(userId, limit, offset)
    }
}

class RegisterTokenRequestBody(
    val fcmToken: String,
    val userId: String
)

interface API {
    @POST("/api/register")
    suspend fun registerToken(
        @Body body: RegisterTokenRequestBody
    ): Response<Unit>

    @GET("/api/goodHistory")
    suspend fun getGoodHistory(
        @Query("userId") userId: String,
        @Query("limit") limit: Int = 5,
        @Query("offset") offset: Int = 0
    ): Response<List<GoodHistory>>
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