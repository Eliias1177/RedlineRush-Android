package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.RaceEvent

@Composable
fun MapScreen(
    onEventSelected: (RaceEvent) -> Unit,
    onBack: () -> Unit
) {
    val events = listOf(
        RaceEvent("CALLES LOCALES", "Novato", 200, 220),
        RaceEvent("CIRCUITO NOCTURNO", "Profesional", 600, 450),
        RaceEvent("DUELO DE JEFES", "Jefe de Calle", 2000, 850)
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MAPA DE CARRERAS", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(vertical = 24.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(events) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onEventSelected(event) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Dificultad: ${event.difficulty}", color = when(event.difficulty) {
                                "Jefe de Calle" -> Color.Red
                                "Profesional" -> Color.Yellow
                                else -> Color.Green
                            }, fontSize = 14.sp)
                        }
                        Text("PREMIO: $${event.reward}", color = Color.Cyan, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
            Text("VOLVER AL GARAJE")
        }
    }
}