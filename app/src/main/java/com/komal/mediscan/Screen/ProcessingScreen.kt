package com.komal.mediscan.Screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.komal.mediscan.MVVM.MediScanViewModel

@Composable
fun ProcessingScreen(navController: NavController, vm: MediScanViewModel) {

    BackHandler(enabled = true) { /* blocked */ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A1F44),
                        Color(0xFF123C69),
                        Color(0xFF1E6091)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Animated Loader (Glow + Arc)
            val infiniteTransition = rememberInfiniteTransition(label = "spin")

            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    tween(1200, easing = LinearEasing)
                ),
                label = "angle"
            )

            Box(
                contentAlignment = Alignment.Center
            ) {

                // Glow circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Color(0xFF22C55E).copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                )

                // Spinning arc
                Canvas(modifier = Modifier.size(80.dp)) {
                    drawArc(
                        color = Color(0xFF22C55E),
                        startAngle = angle,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = Stroke(
                            width = 6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Processing Text
            Text(
                text = vm.processingPhase,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Analyzing your medical report...",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Error UI (Styled)
            vm.errorMessage?.let { error ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(
                            Color(0xFF2A2F3A),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        error,
                        color = Color(0xFFFF6B6B),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        border = BorderStroke(1.dp, Color.White.copy(0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}