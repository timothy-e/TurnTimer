package com.example.turntimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.util.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.input.KeyboardType

import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideStatusBarScreen()
            GameTimerScreen()
        }
    }
}

val colorAnimationLength = 500
val darkenFactor = 0.15f
val iconBorders = 30.dp
val iconSize = 40.dp

@Composable
fun HideStatusBarScreen() {
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(Unit) {
        systemUiController.isStatusBarVisible = false  // Hide status bar
    }
}

class Player(var name: String, var currentTime: Int, val initialTime: Int, var color: Color) {
    fun timeString(): String {
        val minutes = (currentTime / 60)
        val seconds = (currentTime % 60)
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun copy(name: String = this.name, currentTime: Int = this.currentTime, initialTime: Int = this.initialTime, color: Color = this.color): Player {
        return Player(name, currentTime, initialTime, color)
    }
}

@Composable
fun MorphingPlayPauseButton(isRunning: Boolean, onClick: () -> Unit) {
    val transition = remember { Animatable(if (isRunning) 0f else 1f) }

    // Trigger animation when state changes
    LaunchedEffect(isRunning) {
        transition.animateTo(
            targetValue = if (isRunning) 0f else 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
    }

    Canvas(
        modifier = Modifier
            .size(iconSize)
            .clickable { onClick() }
    ) {
        val width = size.width
        val height = size.height
        val barWidth = width * 0.25f

        val leftBarX = lerp(width * 0.25f, width * 0.35f, transition.value)
        val rightBarX = lerp(width * 0.75f, width * 0.65f, transition.value)
        val triangleTipX = lerp(width * 0.5f, width * 0.8f, transition.value)
        val triangleTipY = height * 0.5f

        val path = Path().apply {
            if (transition.value < 0.5f) {
                // Pause Bars
                addRect(androidx.compose.ui.geometry.Rect(leftBarX - barWidth / 2, 0f, leftBarX + barWidth / 2, height))
                addRect(androidx.compose.ui.geometry.Rect(rightBarX - barWidth / 2, 0f, rightBarX + barWidth / 2, height))
            } else {
                // Play Triangle
                moveTo(leftBarX, 0f)
                lineTo(triangleTipX, triangleTipY)
                lineTo(leftBarX, height)
                close()
            }
        }

        drawPath(path, Color.White, style = Fill) // Stroke for play triangle
    }
}

fun Color.darken(factor: Float): Color {
    return this.copy(alpha = this.alpha * (1f - factor)).compositeOver(Color.Black)
}

@Composable
fun SettingsButton(isEditingSettings: Boolean, onClick: () -> Unit) {
    val settingsIconRotation = remember {Animatable(0f)}

    LaunchedEffect(isEditingSettings) {
        while (isEditingSettings) {
            settingsIconRotation.animateTo(
                settingsIconRotation.value + 60f,
                animationSpec = tween(500, easing = LinearEasing)
            )
            delay(600)
        }
    }
    Icon(Icons.Default.Settings,
        contentDescription = "Settings",
        tint = Color.White,
        modifier = Modifier.size(iconSize)
            .rotate(settingsIconRotation.value)
            .indication(remember { MutableInteractionSource() }, null)
            .clickable { onClick() }
    )
}

@Composable
fun GameTimerScreen() {
    var players by remember { mutableStateOf(listOf(
        Player("Hannah", 900, 900, Color(0xFF5ce1e6)),
        Player("Tim", 900, 900, Color(0xFFcb6ce6)),
        Player("Rachel", 900, 900, Color(0xffffbd59)),
        Player("Ryan", 900, 900, Color(0xFFff5757))
    )) }

    val backgroundColor = remember { Animatable(players.first().color) }
    val buttonColor = remember { Animatable(players.first().color.darken(darkenFactor)) }
    val buttonBorderColor = remember { Animatable(players[1].color) }
    val resetIconRotation = remember {Animatable(0f)}

    var isRunning by remember { mutableStateOf(false) }
    var isEditingSettings by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Time management
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (isRunning) {
                players = players.toMutableList().apply {
                    this[0] = this[0].copy(currentTime = this[0].currentTime - 1)
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor.value),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(iconBorders))

        Row(horizontalArrangement = Arrangement.Absolute.Right) {
            Spacer(modifier = Modifier.width(iconBorders))
            if (!isRunning)
                SettingsButton(isEditingSettings) { isEditingSettings = !isEditingSettings }
            Spacer(modifier = Modifier.weight(1f))
            if (!isRunning)
                Icon(Icons.Default.Refresh,
                     contentDescription = "Reset",
                     modifier = Modifier.size(iconSize).clickable {
                         players = players.map { player ->
                             player.copy(currentTime = player.initialTime)
                         }
                         coroutineScope.launch {
                             resetIconRotation.animateTo(resetIconRotation.value + 360f, animationSpec = tween(700, easing = FastOutSlowInEasing))
                         }
                     }.rotate(resetIconRotation.value),
                     tint = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            MorphingPlayPauseButton(isRunning) {
                isRunning = !isRunning
                if (isEditingSettings)
                    isEditingSettings = false
                coroutineScope.launch {
                    launch {
                        buttonColor.animateTo(
                            if (isRunning) players[1].color else players[0].color.darken(
                                darkenFactor
                            ),
                            animationSpec = tween(colorAnimationLength)
                        )
                    }
                    launch {
                        buttonBorderColor.animateTo(
                            players[if (isRunning) 0 else 1].color,
                            animationSpec = tween(colorAnimationLength)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(iconBorders))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next Player Button
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
                .background(buttonColor.value, shape = CircleShape)
                .border(10.dp, buttonBorderColor.value.copy(alpha = 0.5f), shape = CircleShape)
                .clickable {
                    coroutineScope.launch {
                        if (isEditingSettings)
                            isEditingSettings = false
                        if (isRunning) {
                            players = players.drop(1) + players.first()
                            launch {
                                backgroundColor.animateTo(
                                    players[0].color,
                                    tween(colorAnimationLength)
                                )
                            }
                        }
                        isRunning = true
                        launch {
                            buttonColor.animateTo(players[1].color, tween(colorAnimationLength))
                        }
                        launch {
                            buttonBorderColor.animateTo(players[0].color, tween(colorAnimationLength))
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (isRunning) "NEXT" else "UNPAUSE",
                     fontSize = 50.sp,
                     fontWeight = FontWeight.Bold,
                     color = Color.White)
                Text(if (isRunning) players[1].name else players.first().name,
                     fontSize = 30.sp,
                     color = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes player list to the bottom

        // Players List
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(players) { index, player ->
                val isActive = player == players.first()
                val fontSize = if (isActive) 30.sp else 20.sp
                val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                Row(modifier = Modifier.fillMaxWidth().background(player.color).padding(16.dp).animateItem(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    if (isEditingSettings) {
                        TextField(value = player.name, onValueChange = { newName -> players = players.toMutableList().apply {
                            this[index] = this[index].copy(name = newName)
                        }})
                        TextField(player.currentTime.toString(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), onValueChange = { newTime -> players = players.toMutableList().apply {
                            this[index] = this[index].copy(initialTime = newTime.toIntOrNull() ?: 0, currentTime = newTime.toIntOrNull() ?: 0)
                        }})
                    } else {
                        Text(player.name, fontSize = fontSize, fontWeight = fontWeight)
                        Text(player.timeString(), fontSize = fontSize, fontWeight = fontWeight)
                    }
                }
            }
        }
    }
}