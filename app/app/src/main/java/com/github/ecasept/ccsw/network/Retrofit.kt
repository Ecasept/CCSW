package com.github.ecasept.ccsw.network

import com.github.ecasept.ccsw.data.preferences.PreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory


object ServerUrlStorage {
    var serverUrl: String = "" // eg. https://example.com/abc
}


class HostSelectionInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val serverUrl = ServerUrlStorage.serverUrl.toHttpUrlOrNull()
            ?: throw IllegalStateException("Invalid server URL: ${ServerUrlStorage.serverUrl}")
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

fun createOkHttpClient(dataStore: PreferencesDataStore): OkHttpClient {
    // Load the server URL from the data store
    ServerUrlStorage.serverUrl = runBlocking {
        dataStore.prefs.first().serverUrl
    }

    return OkHttpClient.Builder()
        .addInterceptor(HostSelectionInterceptor())
        .build()
}

fun createRetrofit(
    dataStore: PreferencesDataStore,
): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://example.com/") // This will be overridden by the interceptor
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .client(createOkHttpClient(dataStore))
        .build()
}