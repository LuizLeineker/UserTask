package com.example.gson

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface Api {

    @GET("users")
    fun getUsers(): Call<List<User>>


    @GET("users/{id}")
    fun getUserById(@Path("id") id: Int): Call<User>

    @GET("users/{id}/todos")
    fun getTasks(@Path("id") userId: Int): Call<List<Tasks>>

}