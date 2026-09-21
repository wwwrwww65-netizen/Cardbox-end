package com.example.data.remote

import android.content.Context
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val BASE_URL: String = run {
        val configuredUrl: String? = BuildConfig.BASE_URL
        if (!configuredUrl.isNullOrBlank()) {
            if (configuredUrl.endsWith("/")) configuredUrl else "$configuredUrl/"
        } else {
            "https://cardbox.basmasoft.com/api/"
        }
    }

    var authToken: String? = null

    fun initToken(context: Context) {
        val prefs = context.getSharedPreferences("cardbox_pos_prefs", Context.MODE_PRIVATE)
        val savedToken = prefs.getString("auth_token", null)
        if (!savedToken.isNullOrBlank()) {
            authToken = savedToken
        }
    }

    fun saveToken(context: Context, token: String) {
        authToken = token
        val prefs = context.getSharedPreferences("cardbox_pos_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("auth_token", token).apply()
    }

    fun clearToken(context: Context) {
        authToken = null
        val prefs = context.getSharedPreferences("cardbox_pos_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("auth_token").apply()
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            authToken?.let { token ->
                if (token.isNotBlank()) {
                    val bearer = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
                    requestBuilder.header("Authorization", bearer)
                }
            }
            requestBuilder.header("Accept", "application/json")
            chain.proceed(requestBuilder.build())
        }
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: PosBackendApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PosBackendApi::class.java)
    }
}
