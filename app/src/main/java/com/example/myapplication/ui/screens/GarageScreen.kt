package com.example.myapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.model.Player

// Aquí se declaran los colores globales de tu auto
val AppBackground = Color(0xFF0A0A0A)
val ElevatorGlass = Color(0xFF1E222A)
val NeonCyan = Color(0xFF00E5FF)
val PixelGtiYellow = Color(0xFFFFC107)

@Composable
fun GarageScreen(player: Player, onRaceClicked: () -> Unit, onUpgradeClicked: () -> Unit, onStoreClicked: () -> Unit) {
    val currentCar = player.garage.firstOrNull()
    Column(modifier = Modifier.fillMaxSize().background(AppBackground), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)) {
            Text("REDLINE RUSH", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text("VERTICAL SHIFT", color = Color.Gray, fontSize = 12.sp, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("PILOTO: ${player.username.uppercase()} | CASH: $${player.cash}", color = NeonCyan, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth(0.85f).background(ElevatorGlass, RoundedCornerShape(16.dp)).border(2.dp, Color(0xFF2A303C), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
                ElevatorPlatform(isActive = false, car = null)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ElevatorPlatform(isActive = true, car = currentCar)
                    if (currentCar != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(currentCar.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("HP: ${currentCar.horsepower} | RPM: ${currentCar.maxRpm}", color = Color.LightGray, fontSize = 14.sp)
                    }
                }
                ElevatorPlatform(isActive = false, car = null)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF12151A)).padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            BottomMenuButton("GARAJE", isActive = true) {}
            BottomMenuButton("MEJORAS", isActive = false) { onUpgradeClicked() }
            BottomMenuButton("MAPA", isActive = false) { onRaceClicked() }
            BottomMenuButton("TIENDA", isActive = false) { onStoreClicked() }
        }
    }
}

@Composable
fun ElevatorPlatform(isActive: Boolean, car: Car?) {
    Box(modifier = Modifier.fillMaxWidth(0.8f).height(120.dp), contentAlignment = Alignment.BottomCenter) {
        Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(if (isActive) Color.DarkGray else Color(0xFF1A1A1A), RoundedCornerShape(50)).border(1.dp, if (isActive) NeonCyan.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(50)))
        if (isActive && car != null) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).offset(y = (-10).dp)) {
                val scale = 0.6f; val w = size.width; val centerY = size.height * 0.75f

                drawRoundRect(color = PixelGtiYellow, topLeft = Offset(w * 0.1f, centerY - 30.dp.toPx() * scale), size = Size(w * 0.8f, 50.dp.toPx() * scale), cornerRadius = androidx.compose.ui.geometry.CornerRadius(15.dp.toPx() * scale))
                val roofPath = Path().apply { moveTo(w * 0.25f, centerY - 30.dp.toPx() * scale); lineTo(w * 0.40f, centerY - 70.dp.toPx() * scale); lineTo(w * 0.70f, centerY - 70.dp.toPx() * scale); lineTo(w * 0.85f, centerY - 30.dp.toPx() * scale); close() }
                drawPath(path = roofPath, color = PixelGtiYellow)
                val windowPath = Path().apply { moveTo(w * 0.42f, centerY - 62.dp.toPx() * scale); lineTo(w * 0.68f, centerY - 62.dp.toPx() * scale); lineTo(w * 0.68f, centerY - 35.dp.toPx() * scale); lineTo(w * 0.32f, centerY - 35.dp.toPx() * scale); close() }
                drawPath(path = windowPath, color = Color(0xFFE0E0E0).copy(alpha = 0.8f))
                drawRect(color = Color(0xFF1A1A1A), topLeft = Offset(w * 0.82f, centerY - 72.dp.toPx() * scale), size = Size(w * 0.06f, 15.dp.toPx() * scale))

                // Llantas Detalladas
                val wheelRadius = 24.dp.toPx() * scale
                val rimRadius = 14.dp.toPx() * scale
                val wheelY = centerY + 15.dp.toPx() * scale

                drawCircle(color = Color(0xFF151515), radius = wheelRadius, center = Offset(w * 0.28f, wheelY))
                drawCircle(color = Color(0xFF151515), radius = wheelRadius, center = Offset(w * 0.72f, wheelY))

                drawCircle(color = Color(0xFF888888), radius = rimRadius, center = Offset(w * 0.28f, wheelY), style = Stroke(width = 3.dp.toPx()))
                drawCircle(color = Color(0xFF888888), radius = rimRadius, center = Offset(w * 0.72f, wheelY), style = Stroke(width = 3.dp.toPx()))

                drawCircle(color = Color(0xFF444444), radius = 4.dp.toPx() * scale, center = Offset(w * 0.28f, wheelY))
                drawCircle(color = Color(0xFF444444), radius = 4.dp.toPx() * scale, center = Offset(w * 0.72f, wheelY))

                drawRoundRect(color = Color.Black, topLeft = Offset(w * 0.1f, centerY - 30.dp.toPx() * scale), size = Size(w * 0.8f, 50.dp.toPx() * scale), cornerRadius = androidx.compose.ui.geometry.CornerRadius(15.dp.toPx() * scale), style = Stroke(width = 3.dp.toPx()))
            }
        }
    }
}

@Composable
fun BottomMenuButton(text: String, isActive: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(8.dp)) {
        Text(text = text, color = if (isActive) NeonCyan else Color.Gray, fontSize = 10.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
        if (isActive) { Spacer(modifier = Modifier.height(4.dp)); Box(modifier = Modifier.width(20.dp).height(2.dp).background(NeonCyan)) }
    }
}