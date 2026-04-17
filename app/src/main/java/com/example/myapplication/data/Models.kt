package com.example.myapplication.data.model

data class Car(
    val id: Int,
    val name: String,
    val maxRpm: Int,
    val horsepower: Int,
    val shiftTimeMs: Int,
    val engineLevel: Int = 1
)

data class Player(
    val id: Int,
    val username: String,
    val cash: Int,
    val garage: List<Car>
)

data class AuthRequest(val username: String, val password: String)

data class RaceResultRequest(
    val playerId: Int,
    val timeSeconds: Float,
    val difficulty: String // ¡NUEVO!
)

data class BuyCarRequest(val playerId: Int, val carId: Int)

// NUEVO: Modelo para las opciones del mapa
data class RaceEvent(
    val title: String,
    val difficulty: String,
    val reward: Int,
    val rivalHp: Int
)