package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.model.RaceEvent
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.RaceViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var currentScreen by remember { mutableStateOf("Login") }
                    var selectedCar by remember { mutableStateOf<Car?>(null) }
                    var selectedEvent by remember { mutableStateOf<RaceEvent?>(null) } // Guarda el evento elegido del mapa

                    val authViewModel: AuthViewModel = viewModel()
                    val raceViewModel: RaceViewModel = viewModel()

                    when (currentScreen) {
                        "Login" -> LoginScreen(authViewModel, onLoginSuccess = { currentScreen = "Garage" })

                        "Garage" -> {
                            val player = authViewModel.loggedInPlayer.collectAsState().value
                            if (player != null) {
                                GarageScreen(player,
                                    onRaceClicked = { currentScreen = "Map" },
                                    onUpgradeClicked = { currentScreen = "Upgrades" },
                                    onStoreClicked = { currentScreen = "Store" }
                                )
                            }
                        }

                        "Map" -> {
                            MapScreen(
                                onEventSelected = { event ->
                                    selectedEvent = event
                                    // Seleccionamos el primer auto del garaje antes de ir a correr
                                    selectedCar = authViewModel.loggedInPlayer.value?.garage?.firstOrNull()
                                    currentScreen = "Race"
                                },
                                onBack = { currentScreen = "Garage" }
                            )
                        }

                        "Race" -> {
                            val player = authViewModel.loggedInPlayer.collectAsState().value
                            if (selectedCar != null && player != null && selectedEvent != null) {
                                RaceScreen(
                                    viewModel = raceViewModel,
                                    car = selectedCar!!,
                                    player = player,
                                    difficulty = selectedEvent!!.difficulty, // ¡AQUÍ LE PASAMOS LOS DATOS!
                                    rivalHp = selectedEvent!!.rivalHp,       // ¡AQUÍ LE PASAMOS LOS DATOS!
                                    onBackToGarage = { updatedPlayer ->
                                        if (updatedPlayer != null) authViewModel.updatePlayer(updatedPlayer)
                                        currentScreen = "Garage"
                                    }
                                )
                            }
                        }

                        "Upgrades" -> {
                            val player = authViewModel.loggedInPlayer.collectAsState().value
                            if (player != null) UpgradeScreen(player, onBack = { authViewModel.updatePlayer(it); currentScreen = "Garage" })
                        }

                        "Store" -> {
                            val player = authViewModel.loggedInPlayer.collectAsState().value
                            if (player != null) StoreScreen(player, onBack = { authViewModel.updatePlayer(it); currentScreen = "Garage" })
                        }
                    }
                }
            }
        }
    }
}