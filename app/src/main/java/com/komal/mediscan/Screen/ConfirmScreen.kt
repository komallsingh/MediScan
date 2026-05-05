package com.komal.mediscan.Screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.komal.mediscan.MVVM.MediScanViewModel
import com.komal.mediscan.Navigation.Screen

@Composable
fun ConfirmScreen(navController: NavController, vm: MediScanViewModel) {

    val context = LocalContext.current
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(vm.errorMessage) {
        vm.errorMessage?.let { snackbarState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        containerColor = Color.Transparent
    ) { padding ->

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
                )
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Title
                Text(
                    "Structured Clinical Output",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    "Review and verify extracted report before analysis",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )

                // 🔹 Glass Card (Main Content)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(20.dp)
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {

                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "TEXT/STRUCT_DATA",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )

                        Text(
                            "READ-WRITE",
                            color = Color(0xFF51AD74),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Editable Text Box (Styled)
                    OutlinedTextField(
                        value = vm.editableText,
                        onValueChange = { vm.editableText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 13.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color(0xFF459F67)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    // Footer Row
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "VALIDATED",
                            color = Color(0xFF3DA463),
                            fontSize = 11.sp
                        )

                        Text(
                            "AUTO-SAVED",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Warning Card (Soft dark style)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2F3A)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFFB020)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Ensure values like Hemoglobin, Glucose, etc. are clearly visible.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // 🔹 CTA Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Start Analysis
                    Button(
                        onClick = {
                            navController.navigate(Screen.Processing.route)

                            vm.runLocalML(context) {
                                navController.navigate(Screen.Result.route) {
                                    popUpTo(Screen.Processing.route) { inclusive = true }
                                }
                            }
                        },
                        enabled = !vm.isProcessing && vm.editableText.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF41AB68)
                        )
                    ) {
                        if (vm.isProcessing) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            "Start Analysis",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Discard
                    OutlinedButton(
                        onClick = {
                            vm.reset()
                            navController.navigate(Screen.Upload.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = false }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color.White.copy(0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Discard")
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}