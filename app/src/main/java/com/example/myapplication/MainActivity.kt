package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.model.Player
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.RaceViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // Controladores de estado (ViewModels)
                val authViewModel: AuthViewModel = viewModel()
                val raceViewModel: RaceViewModel = viewModel()

                // Variables de Navegación
                var currentScreen by remember { mutableStateOf("Login") }
                var currentPlayer by remember { mutableStateOf<Player?>(null) }

                // Variables para preparar la carrera
                var selectedDifficulty by remember { mutableStateOf("Novato") }
                var selectedRivalHp by remember { mutableStateOf(200) }
                var isOnlineRace by remember { mutableStateOf(false) }

                // --- SISTEMA DE NAVEGACIÓN ---
                when (currentScreen) {
                    "Login" -> {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { jugadorLogeado ->
                                currentPlayer = jugadorLogeado
                                currentScreen = "Garage"
                            }
                        )
                    }

                    // AGRUPAMOS TODAS LAS PANTALLAS QUE NECESITAN AL JUGADOR
                    "Garage", "Map", "Store", "Upgrade" -> {

                        // --- MODO DE EMERGENCIA (OFFLINE) ---
                        // Si el servidor falla y no hay jugador, creamos uno de prueba
                        // para que la app no se rompa y puedas seguir diseñando.
                        val displayPlayer = currentPlayer ?: Player(
                            id = -1,
                            username = "Invitado Local",
                            cash = 5000,
                            garage = mutableListOf(
                                Car(1, "Auto de Prueba", 150, 6000, 400, 1) // Te damos un auto para que no explote la carrera
                            )
                        )

                        when (currentScreen) {
                            "Garage" -> {
                                GarageScreen(
                                    player = displayPlayer,
                                    onRaceClicked = { currentScreen = "Map" },
                                    onUpgradeClicked = { currentScreen = "Upgrade" }, // Ya conectado
                                    onStoreClicked = { currentScreen = "Store" }      // Ya conectado
                                )
                            }
                            "Map" -> {
                                MapScreen(
                                    player = displayPlayer,
                                    onOfflineRace = { difficulty, rivalHp ->
                                        selectedDifficulty = difficulty
                                        selectedRivalHp = rivalHp
                                        isOnlineRace = false
                                        currentScreen = "Race"
                                    },
                                    onOnlineRace = {
                                        selectedDifficulty = "Online"
                                        selectedRivalHp = 750
                                        isOnlineRace = true
                                        currentScreen = "Race"
                                    },
                                    onBack = { currentScreen = "Garage" }
                                )
                            }
                            "Store" -> {
                                // Asumiendo que tu StoreScreen pide (player, onBack)
                                StoreScreen(
                                    player = displayPlayer,
                                    onBack = { currentScreen = "Garage" }
                                )
                            }
                            "Upgrade" -> {
                                // Asumiendo que tu UpgradeScreen pide (player, onBack)
                                UpgradeScreen(
                                    player = displayPlayer,
                                    onBack = { currentScreen = "Garage" }
                                )
                            }
                        }
                    }

                    "Race" -> {
                        // Para la carrera, usamos el jugador actual (o el de emergencia si entraste forzado)
                        val racePlayer = currentPlayer ?: Player(id = -1, username = "Test", cash = 0, garage = mutableListOf(Car(1, "TestCar", 150, 6000, 400, 1)))

                        if (racePlayer.garage.isNotEmpty()) {
                            val activeCar = racePlayer.garage.first()

                            LaunchedEffect(Unit) {
                                raceViewModel.initRace(
                                    car = activeCar,
                                    playerId = racePlayer.id,
                                    difficulty = selectedDifficulty,
                                    rivalHp = selectedRivalHp,
                                    online = isOnlineRace
                                )
                            }

                            RaceScreen(
                                viewModel = raceViewModel,
                                car = activeCar,
                                player = racePlayer,
                                difficulty = selectedDifficulty,
                                rivalHp = selectedRivalHp,
                                onBackToGarage = { updatedPlayer ->
                                    raceViewModel.leaveOnlineRoom()
                                    if (updatedPlayer != null && currentPlayer != null) {
                                        currentPlayer = updatedPlayer
                                    }
                                    currentScreen = "Garage"
                                }
                            )
                        } else {
                            currentScreen = "Garage"
                        }
                    }
                }
            }
        }
    }
}