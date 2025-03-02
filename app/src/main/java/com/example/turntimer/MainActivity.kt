package com.example.turntimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameTimerScreen()
        }
    }
}

class Player(val name: String, var time: Double, var color: Color)

@Composable
fun GameTimerScreen() {
    var players by remember { mutableStateOf(listOf(
        Player("Hannah", 900.0, Color.Cyan), // 15 minutes in seconds
        Player("Tim", 900.0, Color(0xFFA750D9)),
        Player("Rachel", 900.0, Color(0xFFFFC14D)),
        Player("Ryan", 900.0, Color(0xFFFF5959))
    )) }

    val backgroundColor = remember { Animatable(players[0].color) }
    val buttonColor = remember { Animatable(players[1].color) }

    var isRunning by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(players.first()) {
        isRunning = true
        while (isRunning && players.first().time > 0) {
            delay(1000L)
            players = players.toMutableList().apply {
                this[0] = Player(this[0].name, this[0].time - 1, this[0].color)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor.value),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Next Player Button
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(buttonColor.value, shape = CircleShape)
                .clickable {
                    coroutineScope.launch {
                        isRunning = false
                        players = players.drop(1) + players.first()
                        backgroundColor.animateTo(players[0].color, animationSpec = tween(500))
                        buttonColor.animateTo(players[1].color, animationSpec = tween(500))
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NEXT", fontSize = 50.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(players[1 % players.size].name, fontSize = 30.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes player list to the bottom

        // Players List
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(players, key = { it.name }) { player ->
                val isActive = player == players.first()
                val fontSize = if (isActive) 30.sp else 20.sp
                val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                Row(modifier = Modifier.fillMaxWidth().background(player.color).padding(16.dp).animateItem(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(player.name, fontSize = fontSize, fontWeight = fontWeight)
                    Text(
                        String.format(
                            "%02d:%02d",
                            (player.time / 60).toInt(),
                            (player.time % 60).toInt()
                        ), fontSize = fontSize, fontWeight = fontWeight
                    )
                }
            }
        }
    }
}