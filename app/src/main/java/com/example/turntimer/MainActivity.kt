package com.example.turntimer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
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

@Composable
fun GameTimerScreen() {
    var players by remember { mutableStateOf(listOf("Hannah" to 14.42, "Tim" to 15.0, "Rachel" to 15.0, "Ryan" to 15.0)) }
    var currentPlayerIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Cyan),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Next Player Button
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color(0xFFA750D9), shape = CircleShape)
                .clickable {
                    currentPlayerIndex = (currentPlayerIndex + 1) % players.size
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NEXT", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(players[currentPlayerIndex].first, fontSize = 18.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Players List
        players.forEachIndexed { index, player ->
            PlayerRow(player.first, player.second, isActive = index == currentPlayerIndex)
        }
    }
}

@Composable
fun PlayerRow(name: String, time: Double, isActive: Boolean) {
    val backgroundColor = when (name) {
        "Hannah" -> Color.Cyan
        "Tim" -> Color(0xFFA750D9)
        "Rachel" -> Color(0xFFFFC14D)
        "Ryan" -> Color(0xFFFF5959)
        else -> Color.LightGray
    }
    Row(
        modifier = Modifier.fillMaxWidth().background(backgroundColor).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, fontSize = 20.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
        Text(String.format("%.2f", time), fontSize = 20.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
    }
}
