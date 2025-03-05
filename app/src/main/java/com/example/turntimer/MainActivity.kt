package com.example.turntimer

import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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

class Player(var name: String, var current_time: Double, val initial_time : Double, var color: Color) {
    fun timeString(): String {
        val minutes = (current_time / 60).toInt()
        val seconds = (current_time % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun copy(name: String = this.name, current_time: Double = this.current_time, initial_time: Double = this.initial_time, color: Color = this.color): Player {
        return Player(name, current_time, initial_time, color)
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
fun GameTimerScreen() {
    var players by remember { mutableStateOf(listOf(
        Player("Hannah", 900.0, 900.0, Color(0xFF5ce1e6)), // 15 minutes in seconds
        Player("Tim", 900.0, 900.0, Color(0xFFcb6ce6)),
        Player("Rachel", 900.0, 900.0, Color(0xffffbd59)),
        Player("Ryan", 900.0, 900.0, Color(0xFFff5757))
    )) }

    val backgroundColor = remember { Animatable(players.first().color) }
    val buttonColor = remember { Animatable(players.first().color.darken(darkenFactor)) }
    val buttonBorderColor = remember { Animatable(players[1].color) }

    var isRunning by remember { mutableStateOf(false) }
    var isEditingSettings by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Time management
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (isRunning) {
                Log.d("GameTimerScreen", "Updating time")
                players = players.toMutableList().apply {
                    this[0] = Player(
                        this[0].name,
                        this[0].current_time - 1,
                        this[0].initial_time,
                        this[0].color
                    )
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
                Icon(Icons.Default.Settings,
                     contentDescription = "Settings",
                     modifier = Modifier.size(iconSize).clickable {
                         isEditingSettings = !isEditingSettings
                     },
                     tint = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            if (!isRunning)
                Icon(Icons.Default.Refresh,
                     contentDescription = "Restart",
                     modifier = Modifier.size(iconSize).clickable {
                         players = players.map { player ->
                             player.copy(current_time = player.initial_time)
                         }
                     },
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
                            this[index] = Player(
                                newName,
                                this[index].current_time,
                                this[index].initial_time,
                                this[index].color
                            )
                        } })
                        TextField(player.current_time.toString(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), onValueChange = { newTime -> players = players.toMutableList().apply {
                            this[index] = Player(
                                this[index].name,
                                newTime.toDouble(),
                                newTime.toDouble(),
                                this[index].color
                            )
                        } })
                    } else {
                        Text(player.name, fontSize = fontSize, fontWeight = fontWeight)
                        Text(player.timeString(), fontSize = fontSize, fontWeight = fontWeight)
                    }
                }
            }
        }
    }
}