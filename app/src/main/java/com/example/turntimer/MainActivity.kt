package com.example.turntimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        Player("Hannah", 14.42, Color.Cyan),
        Player("Tim", 15.0, Color(0xFFA750D9)),
        Player("Rachel", 15.0, Color(0xFFFFC14D)),
        Player("Ryan", 15.0, Color(0xFFFF5959))
    )) }
    var currentPlayerIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().background(players[currentPlayerIndex].color),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Next Player Button
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(players[(currentPlayerIndex + 1) % players.size].color, shape = CircleShape)
                .clickable {
                    currentPlayerIndex = (currentPlayerIndex + 1) % players.size
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NEXT", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(players[(currentPlayerIndex + 1) % players.size].name, fontSize = 18.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Players List
        players.forEachIndexed { index, player ->
            PlayerRow(player, isActive = index == currentPlayerIndex)
        }
    }
}

@Composable
fun PlayerRow(player: Player, isActive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().background(player.color).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(player.name, fontSize = 20.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
        Text(String.format("%.2f", player.time), fontSize = 20.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
    }
}
