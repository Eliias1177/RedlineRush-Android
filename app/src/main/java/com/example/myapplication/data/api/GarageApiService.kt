package com.example.myapplication.data.api

import com.example.myapplication.data.model.AuthRequest
import com.example.myapplication.data.model.BuyCarRequest
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.model.Player
import com.example.myapplication.data.model.RaceResultRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GarageApiService {
    @GET("api/garage/{playerId}")
    suspend fun getPlayerGarage(@Path("playerId") playerId: Int): Player

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): Player

    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): Player

    @POST("api/race/finish")
    suspend fun finishRace(@Body request: RaceResultRequest): Player

    @POST("api/garage/upgrade/engine")
    suspend fun upgradeEngine(@Body playerId: Int): Player

    // NUEVO: Rutas de la Tienda
    @GET("api/store/catalog")
    suspend fun getStoreCatalog(): List<Car>

    @POST("api/store/buy")
    suspend fun buyCar(@Body request: BuyCarRequest): Player
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5202/"

    val apiService: GarageApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GarageApiService::class.java)
    }
}