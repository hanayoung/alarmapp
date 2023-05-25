package com.example.alarmapp

import retrofit2.Call
import retrofit2.http.GET

interface TimeApi {
   @GET("time")
   fun getAllData() : Call<List<Time>>

}