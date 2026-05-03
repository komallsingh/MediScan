package com.komal.mediscan.Screen

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
    val context       = LocalContext.current
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(vm.errorMessage) {
        vm.errorMessage?.let { snackbarState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarState) }) { padding ->
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
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Check if the text is correct. Edit if needed before analyzing.",
                color    = Color.Gray,
                fontSize = 14.sp
            )

            // Editable OCR text
            OutlinedTextField(
                value         = vm.editableText,
                onValueChange = { vm.editableText = it },
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                label = { Text("Extracted Report Text") },
                shape = RoundedCornerShape(12.dp)
            )

            // Warning card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3CD)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier          = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint               = Color(0xFFFF9800)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Make sure test names (e.g. Hemoglobin, Glucose) and " +
                                "their numbers are visible in the text above.",
                        fontSize = 13.sp,
                        color    = Color(0xFF5D4037)
                    )
                }
            }

            // ── Analyze button — TFLite only, fully offline ───────────────────
            Button(
                onClick = {
                    // Navigate to processing screen first (shows spinner)
                    navController.navigate(Screen.Processing.route)

                    // Run TFLite analysis — callback navigates to results
                    vm.runLocalML(context) {
                        navController.navigate(Screen.Result.route) {
                            popUpTo(Screen.Processing.route) { inclusive = true }
                        }
                    }
                },
                enabled  = !vm.isProcessing && vm.editableText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E6091)
                )
            ) {
                if (vm.isProcessing) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    "Analyze Report",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── Retake button ─────────────────────────────────────────────────
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
                Text("Retake Photo / Choose Again")
            }
        }
    }
}