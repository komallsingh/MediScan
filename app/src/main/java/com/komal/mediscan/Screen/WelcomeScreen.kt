package com.komal.mediscan.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.komal.mediscan.R
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.komal.mediscan.Navigation.Screen

@Composable
fun WelcomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A2463), Color(0xFF1E6091))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_medical),
                //painterResource(id = R.res.drawable),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
            Text(
                "MediScan",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Understand your reports\nin simple language",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { navController.navigate(Screen.Upload.route) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48CAE4)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}