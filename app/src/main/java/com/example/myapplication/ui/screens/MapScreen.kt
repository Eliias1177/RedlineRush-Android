package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.Player

@Composable
fun MapScreen(
    player: Player,
    onOfflineRace: (String, Int) -> Unit, // Dificultad, Vida del Jefe
    onOnlineRace: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("ZONA DE CARRERAS", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text("SELECCIONA TU EVENTO", color = Color.Gray, fontSize = 14.sp, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(32.dp))

        // Botón MULTIJUGADOR ONLINE
        Button(
            onClick = onOnlineRace,
            modifier = Modifier.fillMaxWidth().height(80.dp).border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E222A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🌐 1 VS 1 ONLINE", color = Color(0xFF00E5FF), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Compite contra otro jugador en tiempo real", color = Color.LightGray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("--- JEFES DE CALLE (OFFLINE) ---", color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Jefe 1: Novato
        Button(
            onClick = { onOfflineRace("Novato", 200) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242))
        ) {
            Text("VS Muscle Car (Novato)", color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Jefe 2: Profesional
        Button(
            onClick = { onOfflineRace("Profesional", 450) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("VS Mustang GT (Profesional)", color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Jefe 3: Jefe de Calle
        Button(
            onClick = { onOfflineRace("Jefe de Calle", 750) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0BEC5))
        ) {
            Text("VS GT-R Nismo (Jefe de Calle)", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("VOLVER AL GARAJE", color = Color.White)
        }
    }
}