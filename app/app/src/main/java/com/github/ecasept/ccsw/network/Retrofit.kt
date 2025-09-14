package com.github.ecasept.ccsw.network

import com.github.ecasept.ccsw.data.preferences.PDSRepo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class HostSelectionInterceptor(private val dataStore: PDSRepo) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val prefs = runBlocking { dataStore.prefs.first() }
        val serverUrl = prefs.serverUrl.toHttpUrlOrNull()
            ?: throw IllegalStateException("Invalid server URL: ${prefs.serverUrl}")
        val oldPath = originalRequest.url.encodedPath
        val oldQuery = originalRequest.url.query
        val newUrl = serverUrl.newBuilder()
            .encodedPath(oldPath) // Preserve the original path
            .query(oldQuery) // Preserve the original query parameters
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}

fun createOkHttpClient(dataStore: PDSRepo): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(HostSelectionInterceptor(dataStore))
        .build()
}

fun createRetrofit(
    okHttpClient: OkHttpClient,
): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://example.com/") // This will be overridden by the interceptor
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .client(okHttpClient)
        .build()
}