package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.*
import com.example.myapplication.data.api.RetrofitClient
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RaceViewModel : ViewModel() {

    // --- ESTADOS DE LA CARRERA ---
    private val _rpm = MutableStateFlow(900f)
    val rpm: StateFlow<Float> = _rpm.asStateFlow()

    private val _speedKmH = MutableStateFlow(0f)
    val speedKmH: StateFlow<Float> = _speedKmH.asStateFlow()

    private val _gear = MutableStateFlow(1)
    val gear: StateFlow<Int> = _gear.asStateFlow()

    private val _distanceMeters = MutableStateFlow(0f)
    val distanceMeters: StateFlow<Float> = _distanceMeters.asStateFlow()

    private val _timeSeconds = MutableStateFlow(0f)
    val timeSeconds: StateFlow<Float> = _timeSeconds.asStateFlow()

    // --- FASES Y UI ---
    private val _countdownStage = MutableStateFlow(0)
    val countdownStage: StateFlow<Int> = _countdownStage.asStateFlow()

    private val _isPreRacePhase = MutableStateFlow(true)
    val isPreRacePhase: StateFlow<Boolean> = _isPreRacePhase.asStateFlow()

    var isRacing = false
    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    private val _isWaitingForOpponent = MutableStateFlow(false)
    val isWaitingForOpponent: StateFlow<Boolean> = _isWaitingForOpponent.asStateFlow()

    private val _launchMessage = MutableStateFlow<String?>(null)
    val launchMessage: StateFlow<String?> = _launchMessage.asStateFlow()

    private val _shiftMessage = MutableStateFlow<String?>(null)
    val shiftMessage: StateFlow<String?> = _shiftMessage.asStateFlow()

    private val _isGasPedalPressed = MutableStateFlow(false)
    val isGasPedalPressed: StateFlow<Boolean> = _isGasPedalPressed.asStateFlow()

    // --- VARIABLES DE FÍSICA (TOPES EXACTOS CSR) ---
    // Aquí están tus velocidades tope por cada marcha:
    private val gearMaxSpeeds = floatArrayOf(85f, 130f, 175f, 215f, 260f, 320f)

    private val carWeight = 1200f
    private val idleRPM = 900f
    private val maxRPM = 7500f
    private var carHp = 320f
    private var speedMs = 0f
    private var speedBonus = 1.0f

    // --- VARIABLES DE CONTEXTO ---
    private var currentCar: Car? = null
    private var currentPlayerId: Int = 0
    private var currentDifficulty: String = "Novato"
    private val quarterMileMeters = 402f
    private var gameLoopJob: Job? = null

    // IA Rival
    private val _aiDistanceMeters = MutableStateFlow(0f)
    val aiDistanceMeters: StateFlow<Float> = _aiDistanceMeters.asStateFlow()
    private var aiTargetTime = 12.0f

    private val _winner = MutableStateFlow<String?>(null)
    val winner: StateFlow<String?> = _winner.asStateFlow()

    private val _updatedPlayer = MutableStateFlow<Player?>(null)
    val updatedPlayer: StateFlow<Player?> = _updatedPlayer.asStateFlow()

    // --- RED SIGNALR ---
    private val hubConnection = HubConnectionBuilder.create("http://192.168.1.75:5202/racehub").build()
    private var isOnlineMode = false
    private var currentMatchId: String = ""

    init {
        hubConnection.on("ReceiveOpponentData", { opponentDistance ->
            _aiDistanceMeters.value = opponentDistance
        }, Float::class.java)

        hubConnection.on("WaitingForOpponent") {
            _isWaitingForOpponent.value = true
        }

        hubConnection.on("MatchFound", { matchId ->
            currentMatchId = matchId
            _isWaitingForOpponent.value = false
            startCountdownSequence()
        }, String::class.java)
    }

    fun initRace(car: Car, playerId: Int, difficulty: String, rivalHp: Int, online: Boolean = false) {
        currentCar = car; currentPlayerId = playerId; currentDifficulty = difficulty; isOnlineMode = online
        carHp = car.horsepower.toFloat()

        _rpm.value = idleRPM; _speedKmH.value = 0f; _gear.value = 1; _distanceMeters.value = 0f; speedMs = 0f; speedBonus = 1.0f
        _aiDistanceMeters.value = 0f; _timeSeconds.value = 0f; _isFinished.value = false; _winner.value = null
        _countdownStage.value = 0; isRacing = false; _isPreRacePhase.value = true
        _launchMessage.value = null; _shiftMessage.value = null; _updatedPlayer.value = null

        aiTargetTime = when(difficulty) {
            "Novato" -> 14.5f
            "Profesional" -> 11.5f
            "Jefe de Calle" -> 9.5f
            else -> 12.0f
        }

        if (isOnlineMode) {
            _isWaitingForOpponent.value = true
            viewModelScope.launch {
                try {
                    if (hubConnection.connectionState == HubConnectionState.DISCONNECTED) {
                        hubConnection.start().blockingAwait()
                    }
                    hubConnection.send("JoinMatchmaking", currentPlayerId.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                    _isWaitingForOpponent.value = false
                    _launchMessage.value = "ERROR DE CONEXIÓN"
                }
            }
        } else {
            _isWaitingForOpponent.value = false
            startCountdownSequence()
        }
        startGameLoop()
    }

    fun pressGasPedal() { _isGasPedalPressed.value = true }
    fun releaseGasPedal() { _isGasPedalPressed.value = false }

    fun shiftGear() {
        if (!isRacing || _isFinished.value) return

        val maxGear = gearMaxSpeeds.size
        if (_gear.value >= maxGear) return // Tope de 6ta velocidad

        // 1. Evaluar Shift Timing (Perfect Shift)
        val rpmPercent = _rpm.value / maxRPM
        val shiftQuality = when {
            rpmPercent in 0.72f..0.87f -> "¡PERFECT SHIFT!"
            rpmPercent in 0.58f..0.72f -> "¡BUEN CAMBIO!"
            rpmPercent > 0.87f -> "¡CAMBIO TARDÍO!"
            else -> "MUY PRONTO"
        }

        when (shiftQuality) {
            "¡PERFECT SHIFT!" -> speedBonus += 0.08f
            "¡BUEN CAMBIO!" -> speedBonus += 0.02f
            "¡CAMBIO TARDÍO!" -> speedBonus -= 0.02f
            "MUY PRONTO" -> speedBonus -= 0.08f
        }

        viewModelScope.launch {
            _shiftMessage.value = shiftQuality; delay(1000); _shiftMessage.value = null
        }

        // 2. FÍSICA MECÁNICA REAL:
        // Guardamos la velocidad actual, metemos la nueva marcha y calculamos
        // exactamente a dónde deben caer las RPM para coincidir con la velocidad de las llantas.
        val currentSpeed = _speedKmH.value
        _gear.value += 1

        val newMaxSpeed = gearMaxSpeeds.getOrElse(_gear.value - 1) { 320f }

        // Regla de 3: Si 7500 RPM = MaxSpeed, entonces ¿Cuántas RPM son CurrentSpeed?
        val rpmAfterShift = (currentSpeed / newMaxSpeed) * maxRPM
        _rpm.value = rpmAfterShift.coerceAtLeast(idleRPM)
    }

    private fun startCountdownSequence() {
        viewModelScope.launch {
            _isPreRacePhase.value = true
            _countdownStage.value = 0; delay(1000)
            _countdownStage.value = 1; delay(1000)
            _countdownStage.value = 2; // VERDE

            _isPreRacePhase.value = false
            isRacing = true

            val launchPercent = _rpm.value / maxRPM
            val launchQuality = when {
                launchPercent in 0.78f..0.92f -> "¡PERFECT LAUNCH!"
                launchPercent in 0.55f..0.78f -> "BUEN ARRANQUE"
                launchPercent > 0.92f -> "¡WHEELSPIN!"
                else -> "ARRANQUE LENTO"
            }

            if (launchQuality == "¡PERFECT LAUNCH!") speedBonus += 0.10f
            if (launchQuality == "¡WHEELSPIN!") speedBonus -= 0.10f

            // CLUTCH DROP: Al arrancar el motor carga el peso del auto
            if (launchQuality != "¡WHEELSPIN!") {
                _rpm.value = (_rpm.value * 0.60f).coerceAtLeast(2500f)
            }

            _launchMessage.value = launchQuality; delay(2000); _launchMessage.value = null
        }
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

    private fun updatePhysics(dt: Float) {
        if (_isWaitingForOpponent.value || _isFinished.value) return

        if (!isRacing) {
            // --- PRE-CARRERA (NEUTRAL) ---
            val rpmRiseRate = (carHp / carWeight) * 25000f
            val rpmFallRate = rpmRiseRate * 2.5f

            if (_isGasPedalPressed.value) {
                _rpm.value += rpmRiseRate * dt
            } else {
                _rpm.value -= rpmFallRate * dt
            }
            _rpm.value = _rpm.value.coerceIn(idleRPM, maxRPM)

        } else {
            // --- EN CARRERA (FÍSICA AMARRADA A LA VELOCIDAD) ---
            _timeSeconds.value += dt

            val currentMaxSpeed = gearMaxSpeeds.getOrElse(_gear.value - 1) { 320f }

            // 1. Las RPM suben automáticamente (el piloto pisa a fondo)
            val isRedlining = _rpm.value >= maxRPM - 50f // Llegó al tope rojo

            if (!isRedlining) {
                // Sube más lento en marchas altas (cuesta más empujar el aire)
                val racingRpmRise = (carHp / carWeight) * 12000f / _gear.value.toFloat()
                _rpm.value += racingRpmRise * dt * speedBonus
            }
            _rpm.value = _rpm.value.coerceIn(idleRPM, maxRPM)

            // 2. La Velocidad persigue a las RPM (Bloqueo de Transmisión)
            val targetSpeed = (_rpm.value / maxRPM) * currentMaxSpeed

            // Suavizamos el aumento de velocidad (Traction/Grip simulation)
            val accelerationFactor = (carHp / 100f) * dt * 2.5f

            if (targetSpeed > _speedKmH.value) {
                _speedKmH.value += (targetSpeed - _speedKmH.value) * accelerationFactor
            }

            speedMs = _speedKmH.value / 3.6f
            _distanceMeters.value += speedMs * dt

            // 3. IA Oponente
            if (!isOnlineMode) {
                val aiProgress = (_timeSeconds.value / aiTargetTime).coerceIn(0f, 1f)
                val easedProgress = Math.pow(aiProgress.toDouble(), 1.5).toFloat()
                _aiDistanceMeters.value = easedProgress * quarterMileMeters
            } else if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
                hubConnection.send("SendTelemetry", currentMatchId, _distanceMeters.value)
            }

            // 4. Llegada a Meta
            if (_distanceMeters.value >= quarterMileMeters || _aiDistanceMeters.value >= quarterMileMeters) {
                isRacing = false
                _isFinished.value = true
                if (_distanceMeters.value >= quarterMileMeters) {
                    _winner.value = "PLAYER"
                    if(!isOnlineMode) sendRaceResultsToServer()
                } else {
                    _winner.value = "RIVAL"
                }
            }
        }
    }

    private fun sendRaceResultsToServer() {
        viewModelScope.launch {
            try {
                val request = RaceResultRequest(currentPlayerId, _timeSeconds.value, currentDifficulty)
                _updatedPlayer.value = RetrofitClient.apiService.finishRace(request)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun leaveOnlineRoom() {
        if (isOnlineMode) {
            try { hubConnection.stop() } catch (e: Exception) { e.printStackTrace() }
        }
    }
}