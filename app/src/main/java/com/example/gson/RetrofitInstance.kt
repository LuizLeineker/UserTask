package com.example.gson

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitInstance {
    // URL DE BASE
    private const val BASE_URL  = "https://jsonplaceholder.typicode.com/"

    val api: Api by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(Api::class.java)

    }
}