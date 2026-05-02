package com.komal.mediscan.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.komal.mediscan.MVVM.MediScanViewModel
import com.komal.mediscan.Navigation.Screen

@Composable
fun ConfirmScreen(navController: NavController, vm: MediScanViewModel) {
    val context = LocalContext.current

    // Show error as a Snackbar/dialog
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.errorMessage) {
        vm.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Review Extracted Text",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Check if the text below looks correct. Edit if needed.",
                color = Color.Gray,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = vm.editableText,
                onValueChange = { vm.editableText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                label = { Text("Extracted Report Text") },
                shape = RoundedCornerShape(12.dp)
            )

            // Warning card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "If text looks wrong, edit it above or retake the photo.",
                        fontSize = 13.sp,
                        color = Color(0xFF5D4037)
                    )
                }
            }

            // ✅ CORRECT: navigate first, then start async work
            Button(
                onClick = {
                    // 1. Navigate to processing screen immediately (shows spinner)
                    navController.navigate(Screen.Processing.route)

                    // 2. Start API call — callback fires on Main thread when done
                    vm.analyzeWithOpenAI {
                        // 3. Navigate to results, remove processing from back stack
                        navController.navigate(Screen.Result.route) {
                            popUpTo(Screen.Processing.route) { inclusive = true }
                        }
                    }
                },
                enabled = !vm.isProcessing && vm.editableText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E6091)
                )
            ) {
                if (vm.isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    "Looks Good — Analyze",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = {
                    vm.reset()
                    navController.navigate(Screen.Upload.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Retake Photo")
            }
        }
    }
}