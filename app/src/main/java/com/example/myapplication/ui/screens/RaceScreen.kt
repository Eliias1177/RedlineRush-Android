package com.example.myapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.model.Player
import com.example.myapplication.ui.viewmodel.RaceViewModel

val PixelBlack = Color(0xFF1A1A1A)
val PixelRoad = Color(0xFF444444)
val PixelRival = Color(0xFFF44336)
val PixelFinish = Color(0xFFE91E63)
val PixelGray = Color(0xFF666666)

@Composable
fun RaceScreen(
    viewModel: RaceViewModel,
    car: Car,
    player: Player,
    difficulty: String,
    rivalHp: Int,
    onBackToGarage: (Player?) -> Unit
) {
    val rpm by viewModel.rpm.collectAsState()
    val speed by viewModel.speedKmH.collectAsState()
    val gear by viewModel.gear.collectAsState()
    val distance by viewModel.distanceMeters.collectAsState()
    val aiDistance by viewModel.aiDistanceMeters.collectAsState()

    val time by viewModel.timeSeconds.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val winner by viewModel.winner.collectAsState()

    val isPreRacePhase by viewModel.isPreRacePhase.collectAsState()
    val isPedalPressed by viewModel.isGasPedalPressed.collectAsState()
    val countdownStage by viewModel.countdownStage.collectAsState()
    val updatedPlayer by viewModel.updatedPlayer.collectAsState()

    LaunchedEffect(Unit) { viewModel.initRace(car, player.id, difficulty, rivalHp) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TIME: ${String.format("%.2f", time)} s", color = Color.Green, fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = PixelRoad)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Semáforo
                Canvas(modifier = Modifier.width(60.dp).fillMaxHeight().padding(10.dp)) {
                    val lightSize = Size(40f, 40f)
                    drawRect(color = PixelBlack)
                    drawRect(color = if (!isPreRacePhase && countdownStage == 0) Color.Red else Color.DarkGray, topLeft = Offset(10f, 130f), size = lightSize)
                    drawRect(color = if (!isPreRacePhase && countdownStage == 1) Color.Yellow else Color.DarkGray, topLeft = Offset(10f, 70f), size = lightSize)
                    drawRect(color = if (!isPreRacePhase && countdownStage == 2) Color.Green else Color.DarkGray, topLeft = Offset(10f, 10f), size = lightSize)
                }

                // Pista y Autos
                Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    drawRect(color = PixelFinish, topLeft = Offset(size.width - 20f, 0f), size = Size(20f, size.height))
                    drawRect(color = Color.DarkGray, topLeft = Offset(0f, size.height / 2f - 2f), size = Size(size.width, 4f))

                    // Auto Oponente (Rojo)
                    val aiProgress = (aiDistance / 402f).coerceIn(0f, 1f)
                    drawRect(color = PixelRival, topLeft = Offset(20f + (aiProgress * (size.width - 80f)), size.height * 0.25f - 15f), size = Size(35f, 30f))

                    // --- TU AUTO DETALLADO (VISTA SUPERIOR) ---
                    val playerProgress = (distance / 402f).coerceIn(0f, 1f)
                    val playerX = 20f + (playerProgress * (size.width - 80f))
                    val playerY = size.height * 0.75f - 15f
                    val carLength = 45f
                    val carWidth = 30f

                    // 1. Sombra / Borde
                    drawRect(color = Color.Black, topLeft = Offset(playerX - 2f, playerY - 2f), size = Size(carLength + 4f, carWidth + 4f))

                    // 2. Chasis (Amarillo)
                    drawRect(color = PixelGtiYellow, topLeft = Offset(playerX, playerY), size = Size(carLength, carWidth))

                    // 3. Cabina / Techo (Gris oscuro)
                    drawRect(color = Color(0xFF222222), topLeft = Offset(playerX + 10f, playerY + 4f), size = Size(carLength - 20f, carWidth - 8f))

                    // 4. Parabrisas (Cristal claro)
                    drawRect(color = Color(0xFFE0E0E0).copy(alpha = 0.8f), topLeft = Offset(playerX + 25f, playerY + 4f), size = Size(5f, carWidth - 8f))

                    // 5. Alerón (Negro)
                    drawRect(color = Color.Black, topLeft = Offset(playerX - 2f, playerY + 2f), size = Size(6f, carWidth - 4f))
                }
            }
        }

        RealisticTachometer(rpm = rpm, maxRpm = car.maxRpm, gear = gear, speed = speed)

        Spacer(modifier = Modifier.height(16.dp))

        if (isFinished) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (winner == "PLAYER") {
                    if (updatedPlayer != null) {
                        Text("¡VICTORIA!", color = NeonCyan, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        Text("NUEVO SALDO: $${updatedPlayer!!.cash}", color = Color.Green, fontSize = 24.sp, modifier = Modifier.padding(vertical = 16.dp))
                        Button(onClick = { onBackToGarage(updatedPlayer) }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                            Text("VOLVER AL GARAJE", fontSize = 18.sp, color = Color.White)
                        }
                    } else {
                        CircularProgressIndicator(color = NeonCyan)
                    }
                } else {
                    Text("¡DERROTA!", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { onBackToGarage(player) }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                        Text("HUIR AL GARAJE", fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        } else if (isPreRacePhase) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Box(modifier = Modifier.size(140.dp, 80.dp).background(if (isPedalPressed) Color.DarkGray else PixelGray, CircleShape)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.changes.any { it.pressed }) viewModel.pressGasPedal()
                                else if (event.changes.all { !it.pressed }) viewModel.releaseGasPedal()
                            }
                        }
                    }, contentAlignment = Alignment.Center) { Text("PEDAL", color = Color.Black, fontWeight = FontWeight.Bold) }
                Button(onClick = { viewModel.startRace() }, modifier = Modifier.size(120.dp, 60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) {
                    Text("¡GO!", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            if (!viewModel.isRacing) {
                Text("¡PREPÁRATE!", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            } else {
                Button(onClick = { viewModel.shiftGear() }, modifier = Modifier.fillMaxWidth().height(80.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))) {
                    Text("SHIFT +", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RealisticTachometer(rpm: Float, maxRpm: Int, gear: Int, speed: Float) {
    Box(modifier = Modifier.size(240.dp).padding(16.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val startAngle = 135f
            val sweepAngle = 270f
            val strokeWidth = 20.dp.toPx()
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            drawArc(color = Color(0xFF222222), startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            drawArc(color = Color.Green, startAngle = startAngle + (sweepAngle * 0.75f), sweepAngle = sweepAngle * 0.15f, useCenter = false, style = Stroke(width = strokeWidth))
            drawArc(color = Color.Red, startAngle = startAngle + (sweepAngle * 0.90f), sweepAngle = sweepAngle * 0.10f, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

            val rpmFraction = (rpm / maxRpm).coerceIn(0f, 1f)
            val needleAngle = startAngle + (sweepAngle * rpmFraction)
            val needleAngleRad = Math.toRadians(needleAngle.toDouble())
            val needleLength = radius - 10.dp.toPx()
            val needleEndX = center.x + (needleLength * Math.cos(needleAngleRad)).toFloat()
            val needleEndY = center.y + (needleLength * Math.sin(needleAngleRad)).toFloat()

            drawLine(color = Color.Red, start = center, end = Offset(needleEndX, needleEndY), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(color = Color.White, radius = 12.dp.toPx(), center = center)
            drawCircle(color = Color.Red, radius = 6.dp.toPx(), center = center)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = 30.dp)) {
            Text("${speed.toInt()}", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
            Text("KM/H", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("GEAR $gear", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}