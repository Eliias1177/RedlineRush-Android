package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.BuyCarRequest
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.model.Player
import kotlinx.coroutines.launch

@Composable
fun StoreScreen(
    player: Player,
    onBack: (Player) -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentPlayer by remember { mutableStateOf(player) }
    var catalog by remember { mutableStateOf<List<Car>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isBuying by remember { mutableStateOf(false) }

    // Descargar los autos al abrir la tienda
    LaunchedEffect(Unit) {
        try {
            catalog = RetrofitClient.apiService.getStoreCatalog()
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CONCESIONARIO", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 16.dp))
        Text("CASH DISPONIBLE: $${currentPlayer.cash}", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.padding(top = 40.dp))
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(catalog) { car ->
                    val price = car.horsepower * 25 // Fórmula: 25 USD por HP
                    val canAfford = currentPlayer.cash >= price

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(car.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("${car.horsepower} HP | ${car.maxRpm} RPM", color = Color.LightGray, fontSize = 14.sp)
                            }
                            Button(
                                onClick = {
                                    isBuying = true
                                    scope.launch {
                                        try {
                                            val request = BuyCarRequest(currentPlayer.id, car.id)
                                            val updated = RetrofitClient.apiService.buyCar(request)
                                            currentPlayer = updated // Actualizamos el jugador con su nuevo auto y menos dinero
                                            isBuying = false
                                        } catch (e: Exception) {
                                            isBuying = false
                                        }
                                    }
                                },
                                enabled = canAfford && !isBuying,
                                colors = ButtonDefaults.buttonColors(containerColor = if (canAfford) Color.Green else Color.DarkGray)
                            ) {
                                if (isBuying) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                                else Text("$$price", color = if (canAfford) Color.Black else Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onBack(currentPlayer) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("VOLVER AL GARAJE", color = Color.White)
        }
    }
}