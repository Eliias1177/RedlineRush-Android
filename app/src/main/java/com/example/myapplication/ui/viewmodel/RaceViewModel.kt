package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.model.Player
import com.example.myapplication.data.model.RaceResultRequest
import com.example.myapplication.data.api.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RaceViewModel : ViewModel() {
    private val _rpm = MutableStateFlow(1000f)
    val rpm: StateFlow<Float> = _rpm.asStateFlow()
    private val _speedKmH = MutableStateFlow(0f)
    val speedKmH: StateFlow<Float> = _speedKmH.asStateFlow()
    private val _gear = MutableStateFlow(1)
    val gear: StateFlow<Int> = _gear.asStateFlow()
    private val _distanceMeters = MutableStateFlow(0f)
    val distanceMeters: StateFlow<Float> = _distanceMeters.asStateFlow()

    private val _aiDistanceMeters = MutableStateFlow(0f)
    val aiDistanceMeters: StateFlow<Float> = _aiDistanceMeters.asStateFlow()

    private var aiRpm = 1000f
    private var aiSpeedKmH = 0f
    private var aiGear = 1
    private var rivalCar: Car? = null

    private val _timeSeconds = MutableStateFlow(0f)
    val timeSeconds: StateFlow<Float> = _timeSeconds.asStateFlow()
    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    private val _winner = MutableStateFlow<String?>(null)
    val winner: StateFlow<String?> = _winner.asStateFlow()

    private val _countdownStage = MutableStateFlow(0)
    val countdownStage: StateFlow<Int> = _countdownStage.asStateFlow()
    private val _isPreRacePhase = MutableStateFlow(false)
    val isPreRacePhase: StateFlow<Boolean> = _isPreRacePhase.asStateFlow()

    private val _updatedPlayer = MutableStateFlow<Player?>(null)
    val updatedPlayer: StateFlow<Player?> = _updatedPlayer.asStateFlow()

    var isRacing = false
    private val _isGasPedalPressed = MutableStateFlow(false)
    val isGasPedalPressed: StateFlow<Boolean> = _isGasPedalPressed.asStateFlow()

    private var currentCar: Car? = null
    private var currentPlayerId: Int = 0
    private var currentDifficulty: String = "Novato"
    private val quarterMileMeters = 402f
    private var gameLoopJob: Job? = null

    fun initRace(car: Car, playerId: Int, difficulty: String, rivalHp: Int) {
        currentCar = car
        currentPlayerId = playerId
        currentDifficulty = difficulty

        // Configuramos al rival según la dificultad elegida en el mapa
        rivalCar = Car(id = 99, name = "Rival", maxRpm = 7000, horsepower = rivalHp, shiftTimeMs = 300)

        // Reset completo
        _rpm.value = 1000f; _speedKmH.value = 0f; _gear.value = 1; _distanceMeters.value = 0f
        aiRpm = 1000f; aiSpeedKmH = 0f; aiGear = 1; _aiDistanceMeters.value = 0f
        _timeSeconds.value = 0f; _isFinished.value = false; _winner.value = null
        _countdownStage.value = 0; _isPreRacePhase.value = true; isRacing = false
        _updatedPlayer.value = null

        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (true) {
                val currentTime = System.currentTimeMillis()
                val dt = (currentTime - lastTime) / 1000f
                lastTime = currentTime
                updatePhysics(dt)
                delay(16)
            }
        }
    }

    fun pressGasPedal() { _isGasPedalPressed.value = true }
    fun releaseGasPedal() { _isGasPedalPressed.value = false }

    fun startRace() {
        if (isRacing || _isFinished.value || currentCar == null || !_isPreRacePhase.value) return
        _isPreRacePhase.value = false
        viewModelScope.launch {
            _countdownStage.value = 0; delay(1000)
            _countdownStage.value = 1; delay(1000)
            _countdownStage.value = 2; isRacing = true
        }
    }

    private fun updatePhysics(dt: Float) {
        val car = currentCar ?: return
        val aiCar = rivalCar ?: return

        if (_isGasPedalPressed.value || isRacing) {
            _rpm.value += (car.horsepower * 15f * dt) / _gear.value
        } else {
            _rpm.value -= 3000f * dt
        }
        if (_rpm.value < 1000f) _rpm.value = 1000f
        if (_rpm.value > car.maxRpm) _rpm.value = car.maxRpm.toFloat() - 200f

        if (isRacing) {
            _timeSeconds.value += dt
            // Físicas Jugador
            val targetSpeed = (_rpm.value / car.maxRpm) * (_gear.value * 60f)
            _speedKmH.value += (targetSpeed - _speedKmH.value) * dt * 3f
            _distanceMeters.value += (_speedKmH.value / 3.6f) * dt

            // Físicas IA
            aiRpm += (aiCar.horsepower * 15f * dt) / aiGear
            if (aiRpm >= aiCar.maxRpm - 200f && aiGear < 6) { aiGear++; aiRpm = 4000f }
            val aiTargetSpeed = (aiRpm / aiCar.maxRpm) * (aiGear * 60f)
            aiSpeedKmH += (aiTargetSpeed - aiSpeedKmH) * dt * 3f
            _aiDistanceMeters.value += (aiSpeedKmH / 3.6f) * dt

            if (_distanceMeters.value >= quarterMileMeters || _aiDistanceMeters.value >= quarterMileMeters) {
                isRacing = false; _isFinished.value = true
                if (_distanceMeters.value >= quarterMileMeters) {
                    _winner.value = "PLAYER"
                    sendRaceResultsToServer()
                } else {
                    _winner.value = "RIVAL"
                }
            }
        }
    }

    fun shiftGear() {
        if (!isRacing || _isFinished.value || currentCar == null) return
        if (_gear.value < 6) { _gear.value++; _rpm.value = 4000f }
    }

    private fun sendRaceResultsToServer() {
        viewModelScope.launch {
            try {
                val request = RaceResultRequest(currentPlayerId, _timeSeconds.value, currentDifficulty)
                val newPlayer = RetrofitClient.apiService.finishRace(request)
                _updatedPlayer.value = newPlayer
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}