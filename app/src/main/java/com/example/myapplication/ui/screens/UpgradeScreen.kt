package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.Player
import kotlinx.coroutines.launch

@Composable
fun UpgradeScreen(
    player: Player,
    onBack: (Player) -> Unit
) {
    val car = player.garage.firstOrNull() ?: return
    val scope = rememberCoroutineScope()
    var currentPlayer by remember { mutableStateOf(player) }
    var isLoading by remember { mutableStateOf(false) }

    // Calcula el costo basándose en el perfil más reciente
    val currentCarLevel = currentPlayer.garage.firstOrNull()?.engineLevel ?: 1
    val currentHp = currentPlayer.garage.firstOrNull()?.horsepower ?: 220
    val nextLevelCost = currentCarLevel * 2000

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TALLER DE MEJORAS", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text("CASH DISPONIBLE: $${currentPlayer.cash}", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MOTOR (NIVEL $currentCarLevel)", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Text("POTENCIA ACTUAL: $currentHp HP", color = Color.LightGray)
                LinearProgressIndicator(
                    progress = { (currentHp / 800f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(12.dp).padding(vertical = 12.dp),
                    color = Color.Yellow
                )
                Text("+40 HP en siguiente nivel", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    try {
                        val updated = RetrofitClient.apiService.upgradeEngine(currentPlayer.id)
                        currentPlayer = updated
                        isLoading = false
                    } catch (e: Exception) {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (currentPlayer.cash >= nextLevelCost) Color.Green else Color.DarkGray),
            enabled = !isLoading && currentPlayer.cash >= nextLevelCost
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            } else {
                Text("MEJORAR MOTOR ($$nextLevelCost)", color = if (currentPlayer.cash >= nextLevelCost) Color.Black else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { onBack(currentPlayer) }) {
            Text("VOLVER AL GARAJE", color = Color.Gray, fontSize = 16.sp)
        }
    }
}