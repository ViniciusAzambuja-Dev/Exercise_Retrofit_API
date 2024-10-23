package com.example.calculadorainvest

import retrofit2.Call

import retrofit2.http.GET
import okhttp3.ResponseBody
import retrofit2.http.Path

interface RetrofitInterface {
    @GET("acoes/{codigo}")
    fun getAcao(@Path("codigo") codigo: String): Call<ResponseBody>
}
