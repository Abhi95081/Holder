package com.example.holder

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Create an Animatable for opacity
    val opacity = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate the opacity from 0 to 1
        opacity.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000) // Duration of the fade-in
        )

        // Delay before navigating to the home screen
        delay(2500)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E0C)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Apply the opacity to the Image
            Image(
                painter = painterResource(id = R.drawable.leo),
                contentDescription = "LeoGuard Logo",
                modifier = Modifier
                    .height(120.dp)
                    .padding(bottom = 16.dp)
                    .graphicsLayer(alpha = opacity.value) // Set the alpha
            )
            // Apply the opacity to the Text
            Text(
                text = "LeoGuard",
                fontSize = 32.sp,
                color = Color(0xFFFFD700), // gold color
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer(alpha = opacity.value) // Set the alpha
            )
            Text(
                text = "Gallery",
                fontSize = 28.sp,
                color = Color(0xFFFFD700), // gold color
                fontWeight = FontWeight.Medium,
                modifier = Modifier.graphicsLayer(alpha = opacity.value) // Set the alpha
            )
        }
    }
}
