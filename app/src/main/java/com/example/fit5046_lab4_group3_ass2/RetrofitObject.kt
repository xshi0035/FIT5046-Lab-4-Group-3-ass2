package com.example.retrofittesting

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitObject {
    private val BASE_URL = "https://api.openelectricity.org.au/"
    val retrofitService: RetrofitInterface by lazy {
        //val interceptor = Interceptor { chain ->
        //    val request = chain.request().newBuilder()
        //        .addHeader("Authorization", "Bearer oe_3ZLaQvrd2UEJYyLDh9GnHego")
        //        .build()
        //    chain.proceed(request)
        //}

        //val logging = HttpLoggingInterceptor().apply {
        //    level = HttpLoggingInterceptor.Level.BODY
        //}

        val client = OkHttpClient.Builder()
            //.addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer oe_3ZLaQvrd2UEJYyLDh9GnHego")
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RetrofitInterface::class.java)
    }
}