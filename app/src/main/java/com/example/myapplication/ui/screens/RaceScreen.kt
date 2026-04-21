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

val PixelRivalNovato = Color(0xFF616161)
val PixelRivalProfesional = Color(0xFFD32F2F)
val PixelRivalJefeCalle = Color(0xFFB0BEC5)
val PixelWingColor = Color(0xFF1A1A1A)
val PixelBlack = Color(0xFF1A1A1A)
val PixelRoad = Color(0xFF444444)
val PixelRival = Color(0xFFF44336)
val PixelFinish = Color(0xFFE91E63)
val PixelGray = Color(0xFF666666)

@Composable
fun RaceScreen(viewModel: RaceViewModel, car: Car, player: Player, difficulty: String, rivalHp: Int, onBackToGarage: (Player?) -> Unit) {
    val rpm by viewModel.rpm.collectAsState()
    val speed by viewModel.speedKmH.collectAsState()
    val gear by viewModel.gear.collectAsState()
    val distance by viewModel.distanceMeters.collectAsState()
    val aiDistance by viewModel.aiDistanceMeters.collectAsState()
    val time by viewModel.timeSeconds.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val winner by viewModel.winner.collectAsState()
    val isPreRacePhase by viewModel.isPreRacePhase.collectAsState()
    val isWaitingForOpponent by viewModel.isWaitingForOpponent.collectAsState()
    val isPedalPressed by viewModel.isGasPedalPressed.collectAsState()
    val countdownStage by viewModel.countdownStage.collectAsState()
    val updatedPlayer by viewModel.updatedPlayer.collectAsState()

    // Recolectamos AMBOS mensajes (Arranque y Cambios)
    val launchMessage by viewModel.launchMessage.collectAsState()
    val shiftMessage by viewModel.shiftMessage.collectAsState()
    val displayMessage = shiftMessage ?: launchMessage

    LaunchedEffect(Unit) { viewModel.initRace(car, player.id, difficulty, rivalHp) }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. EL JUEGO EN SÍ
        Column(modifier = Modifier.fillMaxSize().background(PixelBlack).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                if (displayMessage != null) {
                    val msgColor = when(displayMessage) {
                        "¡PERFECT SHIFT!", "¡PERFECT LAUNCH!" -> Color.Green
                        "¡DERRAPE!", "¡WHEELSPIN!", "¡CAMBIO TARDÍO!", "MUY PRONTO", "ERROR DE CONEXIÓN" -> Color.Red
                        else -> Color.Yellow
                    }
                    Text(text = displayMessage, color = msgColor, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                } else {
                    Text("TIME: ${String.format("%.2f", time)} s", color = Color.Green, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(modifier = Modifier.fillMaxWidth().height(180.dp).padding(vertical = 16.dp), colors = CardDefaults.cardColors(containerColor = PixelRoad)) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.width(60.dp).fillMaxHeight().padding(10.dp)) {
                        val lightSize = Size(40f, 40f)
                        drawRect(color = PixelBlack)
                        // Semáforo CSR
                        drawRect(color = if (countdownStage >= 0 && isPreRacePhase) Color.Red else Color.DarkGray, topLeft = Offset(10f, 130f), size = lightSize)
                        drawRect(color = if (countdownStage >= 1 && isPreRacePhase) Color.Yellow else Color.DarkGray, topLeft = Offset(10f, 70f), size = lightSize)
                        drawRect(color = if (!isPreRacePhase) Color.Green else Color.DarkGray, topLeft = Offset(10f, 10f), size = lightSize)
                    }

                    Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        drawRect(color = PixelFinish, topLeft = Offset(size.width - 20f, 0f), size = Size(20f, size.height))
                        drawRect(color = Color.DarkGray, topLeft = Offset(0f, size.height / 2f - 2f), size = Size(size.width, 4f))

                        val aiY = size.height * 0.30f - 15f
                        val playerY = size.height * 0.70f - 15f
                        val aiProgress = (aiDistance / 402f).coerceIn(0f, 1f)
                        val aiX = 20f + (aiProgress * (size.width - 80f))
                        val aiCarSize = 30f

                        when (difficulty) {
                            "Novato" -> {
                                drawRect(color = Color.Black, topLeft = Offset(aiX - 2f, aiY - 2f), size = Size(aiCarSize + 10f, aiCarSize + 4f))
                                drawRect(color = PixelRivalNovato, topLeft = Offset(aiX, aiY), size = Size(aiCarSize + 8f, aiCarSize))
                                drawLine(color = Color.White, start = Offset(aiX + 10f, aiY + 8f), end = Offset(aiX + 10f, aiY + aiCarSize - 8f), strokeWidth = 3f)
                                drawLine(color = Color.White, start = Offset(aiX + 15f, aiY + 8f), end = Offset(aiX + 15f, aiY + aiCarSize - 8f), strokeWidth = 3f)
                            }
                            "Profesional" -> {
                                drawRect(color = Color.Black, topLeft = Offset(aiX - 2f, aiY - 2f), size = Size(aiCarSize + 6f, aiCarSize + 4f))
                                drawRect(color = PixelRivalProfesional, topLeft = Offset(aiX, aiY), size = Size(aiCarSize + 4f, aiCarSize))
                                drawRect(color = Color(0xFF111111), topLeft = Offset(aiX + 10f, aiY + 5f), size = Size(aiCarSize - 10f, aiCarSize - 10f))
                                drawRect(color = Color.Black, topLeft = Offset(aiX - 1f, aiY + 2f), size = Size(3f, aiCarSize - 4f))
                            }
                            "Jefe de Calle" -> {
                                drawRect(color = Color.Black, topLeft = Offset(aiX - 2f, aiY - 2f), size = Size(aiCarSize + 8f, aiCarSize + 6f))
                                drawRect(color = PixelRivalJefeCalle, topLeft = Offset(aiX, aiY), size = Size(aiCarSize + 6f, aiCarSize + 2f))
                                drawRect(color = Color(0xFF111111), topLeft = Offset(aiX + 12f, aiY + 6f), size = Size(aiCarSize - 12f, aiCarSize - 10f))
                                drawRect(color = PixelWingColor, topLeft = Offset(aiX - 4f, aiY + 1f), size = Size(7f, aiCarSize))
                                drawCircle(color = Color.Red, radius = 3f, center = Offset(aiX + 2f, aiY + 8f))
                                drawCircle(color = Color.Red, radius = 3f, center = Offset(aiX + 2f, aiY + aiCarSize - 6f))
                            }
                            else -> { drawRect(color = PixelRival, topLeft = Offset(aiX, aiY), size = Size(35f, 30f)) }
                        }

                        val playerProgress = (distance / 402f).coerceIn(0f, 1f)
                        val playerX = 20f + (playerProgress * (size.width - 80f))
                        val carLength = 45f; val carWidth = 30f

                        drawRect(color = Color.Black, topLeft = Offset(playerX - 2f, playerY - 2f), size = Size(carLength + 4f, carWidth + 4f))
                        // Usamos código HEX directo para evitar conflictos
                        drawRect(color = Color(0xFFFFEB3B), topLeft = Offset(playerX, playerY), size = Size(carLength, carWidth))
                        drawRect(color = Color(0xFF222222), topLeft = Offset(playerX + 10f, playerY + 4f), size = Size(carLength - 20f, carWidth - 8f))
                        drawRect(color = Color(0xFFE0E0E0).copy(alpha = 0.8f), topLeft = Offset(playerX + 25f, playerY + 4f), size = Size(5f, carWidth - 8f))
                        drawRect(color = Color.Black, topLeft = Offset(playerX - 2f, playerY + 2f), size = Size(6f, carWidth - 4f))
                    }
                }
            }

            RealisticTachometer(rpm = rpm, maxRpm = car.maxRpm, gear = gear, speed = speed)
            Spacer(modifier = Modifier.height(16.dp))

            // --- CONTROLES Y RESULTADOS ---
            if (isFinished) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (winner == "PLAYER") {
                        if (updatedPlayer != null) {
                            Text("¡VICTORIA!", color = Color(0xFF00E5FF), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                            Text("NUEVO SALDO: $${updatedPlayer!!.cash}", color = Color.Green, fontSize = 24.sp, modifier = Modifier.padding(vertical = 16.dp))
                            Button(onClick = { viewModel.leaveOnlineRoom(); onBackToGarage(updatedPlayer) }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                                Text("VOLVER AL GARAJE", fontSize = 18.sp, color = Color.White)
                            }
                        } else {
                            CircularProgressIndicator(color = Color(0xFF00E5FF))
                        }
                    } else {
                        Text("¡DERROTA!", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.leaveOnlineRoom(); onBackToGarage(player) }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                            Text("HUIR AL GARAJE", fontSize = 18.sp, color = Color.White)
                        }
                    }
                }
            } else if (isPreRacePhase) {
                // FASE DE ARRANQUE: Solo Pedal Centrado
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Box(modifier = Modifier
                        .size(160.dp, 100.dp)
                        .background(if (isPedalPressed) Color.DarkGray else PixelGray, CircleShape)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.any { it.pressed }) viewModel.pressGasPedal()
                                    else if (event.changes.all { !it.pressed }) viewModel.releaseGasPedal()
                                }
                            }
                        }, contentAlignment = Alignment.Center) {
                        Text("ACELERADOR", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // FASE DE CARRERA: Solo Cambios (Shift)
                Button(
                    onClick = { viewModel.shiftGear() },
                    modifier = Modifier.fillMaxWidth().height(100.dp).padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Text("SHIFT +", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // 2. PANTALLA DE CARGA ONLINE
        if (isWaitingForOpponent) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.90f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF00E5FF), strokeWidth = 5.dp, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("BUSCANDO RIVAL...", color = Color(0xFF00E5FF), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Esperando a que otro piloto se conecte...", color = Color.LightGray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = {
                            viewModel.leaveOnlineRoom()
                            onBackToGarage(player)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                        modifier = Modifier.size(150.dp, 50.dp)
                    ) {
                        Text("CANCELAR", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RealisticTachometer(rpm: Float, maxRpm: Int, gear: Int, speed: Float) {
    Box(modifier = Modifier.size(240.dp).padding(16.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val startAngle = 135f; val sweepAngle = 270f; val strokeWidth = 20.dp.toPx()
            val radius = size.minDimension / 2; val center = Offset(size.width / 2, size.height / 2)

            drawArc(color = Color(0xFF222222), startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

            // Zonas del motor al estilo CSR
            drawArc(color = Color.Green, startAngle = startAngle + (sweepAngle * 0.58f), sweepAngle = sweepAngle * 0.14f, useCenter = false, style = Stroke(width = strokeWidth))
            drawArc(color = Color.Yellow, startAngle = startAngle + (sweepAngle * 0.72f), sweepAngle = sweepAngle * 0.15f, useCenter = false, style = Stroke(width = strokeWidth))
            drawArc(color = Color.Red, startAngle = startAngle + (sweepAngle * 0.87f), sweepAngle = sweepAngle * 0.13f, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

            val rpmFraction = (rpm / maxRpm.toFloat()).coerceIn(0f, 1f)
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
            Text("GEAR $gear", color = Color(0xFF00E5FF), fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}