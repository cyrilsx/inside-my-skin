package dev.healthhub.companion.data.api

import dev.healthhub.companion.api.apis.CompanionApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient

object ApiFactory {

    fun companionApi(baseUrl: String, token: String): CompanionApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request = if (token.isNotBlank()) {
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            })
            .build()
        return CompanionApi(basePath = baseUrl, client = client)
    }
}
