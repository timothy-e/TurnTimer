package com.example.turntimer

import android.R.attr.fontWeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.util.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType

import com.google.accompanist.systemuicontroller.rememberSystemUiController

val sigmarFont = FontFamily(Font(R.font.sigmar_regular))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideStatusBarScreen()
            GameTimerScreen()
        }
    }
}

val niceColors = listOf(
    Color(0xFFff595e),
    Color(0xFFff924c),
    Color(0xFFffca3a),
    Color(0xFF8ac926),
    Color(0xFF52a675),
    Color(0xFF1982c4),
    Color(0xFF4064a0),
    Color(0xFF6a4c93),
)

const val colorAnimationLength = 500
const val darkenFactor = 0.15f
val iconBorders = 30.dp
val iconSize = 40.dp

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

fun updateAvailableColors(niceColors: List<Color>): List<Color> {
    return niceColors.shuffled()
}

var availableColors = updateAvailableColors(niceColors)

@Composable
fun GameTimerScreen() {
    fun getAvailableColor(): Color {
        if (availableColors.isEmpty())
            availableColors = updateAvailableColors(niceColors)
        val color = availableColors.first()
        availableColors = availableColors.drop(1)
        return color
    }

    var players by remember { mutableStateOf(listOf(
        Player("Hannah", 900, 900, getAvailableColor()),
        Player("Tim", 900, 900, getAvailableColor()),
        Player("Rachel", 900, 900, getAvailableColor()),
        Player("Ryan", 900, 900, getAvailableColor())
    )) }

    val backgroundColor = remember { Animatable(players.first().color) }
    val buttonColor = remember { Animatable(players.first().color.darken(darkenFactor)) }
    val buttonBorderColor = remember { Animatable(players[1].color) }

    var isRunning by remember { mutableStateOf(false) }
    var isEditingSettings by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun setButtonColor(isRunning: Boolean) {
        coroutineScope.launch {
            buttonColor.animateTo(
                if (isRunning) players[1].color else players[0].color.darken(darkenFactor),
                animationSpec = tween(colorAnimationLength)
            )
        }
        coroutineScope.launch {
            buttonBorderColor.animateTo(
                players[if (isRunning) 0 else 1].color,
                animationSpec = tween(colorAnimationLength)
            )
        }
    }

    fun setBackgroundColor() {
        coroutineScope.launch {
            backgroundColor.animateTo(
                players[0].color,
                animationSpec = tween(colorAnimationLength)
            )
        }
    }

    EverySecond {
        if (isRunning) {
            players = players.toMutableList().apply {
                this[0] = this[0].copy(currentTime = this[0].currentTime - 1)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor.value),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(iconBorders))

        // Top Buttons
        Row(horizontalArrangement = Arrangement.Absolute.Right) {
            if (!isRunning) {
                Spacer(modifier = Modifier.width(iconBorders))
                SettingsButton(isEditingSettings) { isEditingSettings = !isEditingSettings }
                Spacer(modifier = Modifier.weight(1f))
                RestartButton { players = players.map { player ->
                    player.copy(currentTime = player.initialTime)
                }}
            }
            Spacer(modifier = Modifier.weight(1f))
            MorphingPlayPauseButton(isRunning) {
                isRunning = !isRunning
                if (isEditingSettings)
                    isEditingSettings = false
                setButtonColor(isRunning)
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
                        isEditingSettings = false
                        if (isRunning) {
                            players = players.drop(1) + players.first()
                            setBackgroundColor()
                        }
                        isRunning = true
                        setButtonColor(true)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (isRunning) "NEXT" else "UNPAUSE",
                     fontSize = 50.sp,
                     fontWeight = FontWeight.Bold,
                     color = Color.White,
                     style = TextStyle(fontFamily = sigmarFont))
                Text(if (isRunning) players[1].name else players.first().name,
                     fontSize = 30.sp,
                     color = Color.White,
                     style = TextStyle(fontFamily = sigmarFont))
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes player list to the bottom


        if (isEditingSettings && availableColors.isNotEmpty())
            Row(horizontalArrangement = Arrangement.Absolute.Right) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Add, "Add Player", tint = Color.White, modifier = Modifier.size(iconSize).clickable {
                    players = players + Player("New Player", players.first().initialTime, players.first().initialTime, getAvailableColor())
                })
            }

        PlayerList(isEditingSettings, players) { updatedPlayers ->
            players = updatedPlayers
            // We may need to change the background color
            setBackgroundColor()
            setButtonColor(isRunning)
        }
    }
}

@Composable
fun EverySecond(everySecond: () -> Unit) {
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            everySecond()
        }
    }
}

@Composable
fun HideStatusBarScreen() {
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(Unit) {
        systemUiController.isStatusBarVisible = false  // Hide status bar
    }
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
fun RestartButton(onClick: () -> Unit) {
    val resetIconRotation = remember {Animatable(0f)}
    val coroutineScope = rememberCoroutineScope()
    Icon(Icons.Default.Refresh,
        contentDescription = "Reset",
        tint = Color.White,
        modifier = Modifier.size(iconSize).rotate(resetIconRotation.value).clickable {
            coroutineScope.launch {
                resetIconRotation.animateTo(resetIconRotation.value + 360f, animationSpec = tween(700, easing = FastOutSlowInEasing))
            }
            onClick()
        })
}


@Composable
fun PlayerList(isEditingSettings: Boolean, players: List<Player>, onPlayersChange: (List<Player>) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(players) { index, player ->
            val isActive = player == players.first()
            val fontSize = if (isActive) 50.sp else 20.sp
            val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            Row(modifier = Modifier.fillMaxWidth()
                    .background(player.color)
                    .padding(horizontal = if (isEditingSettings) 0.dp else 16.dp, vertical = if (isEditingSettings) 8.dp else 16.dp)
                    .animateItem(),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (isEditingSettings) {
                    if (index != 0) // can't move the first player up
                        Icon(Icons.Default.KeyboardArrowUp,
                             contentDescription = "Move Up",
                             tint = Color.White,
                             modifier = Modifier.size(iconSize).clickable {
                            onPlayersChange(players.take(index - 1) + players[index] + players[index - 1] + players.drop(index + 1))
                        })
                    else
                        Spacer(modifier = Modifier.width(iconSize))
                    TextField(value = player.name, modifier = Modifier.weight(3f), onValueChange = { newName ->
                        onPlayersChange(players.toMutableList().apply {
                            this[index] = this[index].copy(name = newName)
                        })
                    })
                    TextField(player.currentTime.toString(), modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), onValueChange = { newTime ->
                        onPlayersChange(players.toMutableList().apply {
                            this[index] = this[index].copy(
                                initialTime = newTime.toIntOrNull() ?: 0,
                                currentTime = newTime.toIntOrNull() ?: 0
                            )
                        })
                    })
                    if (players.size > 2)
                        Icon(Icons.Default.Clear,
                             contentDescription =  "Delete Item",
                             tint = Color.White,
                             modifier = Modifier.size(iconSize).clickable {
                            onPlayersChange(players.take(index) + players.drop(index + 1))
                        })
                    else
                        Spacer(modifier = Modifier.size(iconSize))
                } else if (index == 0) {
                    Column {
                        Text(player.timeString(), fontSize = fontSize, fontWeight = fontWeight, color = Color.White,
                            style = TextStyle(fontFamily = sigmarFont), modifier = Modifier.padding(bottom = 0.dp))
                        Text(player.name, fontSize = fontSize * 1.5, fontWeight = fontWeight, color = Color.White,
                            style = TextStyle(fontFamily = sigmarFont), modifier = Modifier.padding(top = 0.dp))
                    }
                } else { // !isEditingSettings
                    Text(player.name, fontSize = fontSize, fontWeight = fontWeight, color = Color.White,
                            style = TextStyle(fontFamily = sigmarFont))
                    Text(player.timeString(), fontSize = fontSize, fontWeight = fontWeight, color = Color.White,
                            style = TextStyle(fontFamily = sigmarFont))
                }
            }
        }
    }
}