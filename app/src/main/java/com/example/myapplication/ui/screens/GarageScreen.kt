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

val AppBackground = Color(0xFF0A0A0A)
val ElevatorGlass = Color(0xFF1E222A)
val NeonCyan = Color(0xFF00E5FF)
val PixelGtiYellow = Color(0xFFFFC107)

@Composable
fun GarageScreen(
    player: Player,
    onRaceClicked: () -> Unit,
    onUpgradeClicked: () -> Unit,
    onStoreClicked: () -> Unit
) {
    val currentCar = player.garage.firstOrNull()

    Column(
        modifier = Modifier.fillMaxSize().background(AppBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)) {
            Text("REDLINE RUSH", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text("VERTICAL SHIFT", color = Color.Gray, fontSize = 12.sp, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("PILOTO: ${player.username.uppercase()} | CASH: $${player.cash}", color = NeonCyan, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(0.85f).background(ElevatorGlass, RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFF2A303C), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
                ElevatorPlatform(isActive = false, car = null)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Aquí llamamos a la plataforma activa y le pasamos el auto
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

        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF12151A)).padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomMenuButton("GARAJE", isActive = true) {}
            BottomMenuButton("MEJORAS", isActive = false) { onUpgradeClicked() }
            BottomMenuButton("MAPA", isActive = false) { onRaceClicked() }
            BottomMenuButton("TIENDA", isActive = false) { onStoreClicked() }
            BottomMenuButton("EQUIPO", isActive = false) {}
        }
    }
}

@Composable
fun ElevatorPlatform(isActive: Boolean, car: Car?) {
    Box(modifier = Modifier.fillMaxWidth(0.8f).height(120.dp), contentAlignment = Alignment.BottomCenter) {
        // La base del elevador
        Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(if (isActive) Color.DarkGray else Color(0xFF1A1A1A), RoundedCornerShape(50))
            .border(1.dp, if (isActive) NeonCyan.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(50)))

        // --- DIBUJO DETALLADO DEL AUTO (VISTA DE PERFIL) ---
        if (isActive && car != null) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).offset(y = (-10).dp)) {
                val bodyColor = PixelGtiYellow
                val wheelColor = Color(0xFF1A1A1A)
                val windowColor = Color(0xFFE0E0E0).copy(alpha = 0.8f)
                val strokeColor = Color.Black

                val scale = 0.6f
                val w = size.width
                val h = size.height
                val centerY = h * 0.75f

                // 1. Chasis Base (Cuerpo)
                drawRoundRect(
                    color = bodyColor,
                    topLeft = Offset(w * 0.1f, centerY - 30.dp.toPx() * scale),
                    size = Size(w * 0.8f, 50.dp.toPx() * scale),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(15.dp.toPx() * scale, 15.dp.toPx() * scale)
                )

                // 2. Lunas y Techo (Hatchback)
                val roofPath = Path().apply {
                    moveTo(w * 0.25f, centerY - 30.dp.toPx() * scale) // Capó
                    lineTo(w * 0.40f, centerY - 70.dp.toPx() * scale) // Parabrisas
                    lineTo(w * 0.70f, centerY - 70.dp.toPx() * scale) // Techo
                    lineTo(w * 0.85f, centerY - 30.dp.toPx() * scale) // Pilar trasero
                    close()
                }
                drawPath(path = roofPath, color = bodyColor)

                // 3. Ventanas de Cristal
                val windowPath = Path().apply {
                    moveTo(w * 0.42f, centerY - 62.dp.toPx() * scale)
                    lineTo(w * 0.68f, centerY - 62.dp.toPx() * scale)
                    lineTo(w * 0.68f, centerY - 35.dp.toPx() * scale)
                    lineTo(w * 0.32f, centerY - 35.dp.toPx() * scale)
                    close()
                }
                drawPath(path = windowPath, color = windowColor)

                // 4. Alerón Trasero
                drawRect(
                    color = wheelColor,
                    topLeft = Offset(w * 0.82f, centerY - 72.dp.toPx() * scale),
                    size = Size(w * 0.06f, 15.dp.toPx() * scale)
                )

                // 5. Neumáticos
                val wheelRadius = 22.dp.toPx() * scale
                val wheelY = centerY + 15.dp.toPx() * scale

                // Rueda Delantera
                drawCircle(color = wheelColor, radius = wheelRadius, center = Offset(w * 0.28f, wheelY))
                drawCircle(color = Color.LightGray, radius = wheelRadius * 0.4f, center = Offset(w * 0.28f, wheelY))

                // Rueda Trasera
                drawCircle(color = wheelColor, radius = wheelRadius, center = Offset(w * 0.72f, wheelY))
                drawCircle(color = Color.LightGray, radius = wheelRadius * 0.4f, center = Offset(w * 0.72f, wheelY))

                // 6. Contorno general (Estilo dibujo)
                drawRoundRect(
                    color = strokeColor,
                    topLeft = Offset(w * 0.1f, centerY - 30.dp.toPx() * scale),
                    size = Size(w * 0.8f, 50.dp.toPx() * scale),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(15.dp.toPx() * scale, 15.dp.toPx() * scale),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun BottomMenuButton(text: String, isActive: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(8.dp)) {
        Text(text = text, color = if (isActive) NeonCyan else Color.Gray, fontSize = 10.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
        if (isActive) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.width(20.dp).height(2.dp).background(NeonCyan))
        }
    }
}