package com.komal.mediscan.Screen

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.komal.mediscan.MVVM.MediScanViewModel
import com.komal.mediscan.Navigation.Screen
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UploadScreen(navController: NavController, vm: MediScanViewModel) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraFileRef by remember { mutableStateOf<File?>(null) }

    // ── Camera permission ────────────────────────────────────────────────────
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    // ── Camera launcher ──────────────────────────────────────────────────────
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraFileRef?.let { file ->
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                imageUri = uri
                vm.capturedImageUri = uri
                vm.inputType = "image"
            }
        }
    }

    // ── Gallery launcher ─────────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            imageUri = it
            vm.capturedImageUri = it
            vm.inputType = "image"
        }
    }

    // ── PDF launcher ─────────────────────────────────────────────────────────
    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            imageUri = null
            vm.capturedImageUri = it
            vm.inputType = "pdf"
        }
    }

    // ── Helper: create temp file and launch camera ───────────────────────────
    fun launchCamera() {
        val file = File(context.cacheDir, "mediscan_${System.currentTimeMillis()}.jpg")
        cameraFileRef = file
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        cameraLauncher.launch(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            "Scan Your Report",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Upload a photo, take a picture, or select a PDF",
            color = Color.Gray,
            fontSize = 14.sp
        )

        // ── Image preview box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF0F4F8))
                .border(1.dp, Color(0xFFDDE3EA), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                imageUri != null -> {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                vm.inputType == "pdf" && vm.capturedImageUri != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFE53935)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("PDF selected", color = Color.Gray)
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF90A4AE)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No file selected", color = Color.Gray)
                    }
                }
            }
        }

        // 3 buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Camera button — requests permission first
            OutlinedButton(
                onClick = {
                    if (cameraPermission.status.isGranted) {
                        launchCamera()
                    } else {
                        cameraPermission.launchPermissionRequest()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Camera", fontSize = 13.sp)
            }

            // Gallery button
            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Gallery", fontSize = 13.sp)
            }

            // PDF button
            OutlinedButton(
                onClick = {
                    pdfLauncher.launch(arrayOf("application/pdf"))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("PDF", fontSize = 13.sp)
            }
        }

        // Show permission rationale if denied
        if (!cameraPermission.status.isGranted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Camera permission is needed to take photos. Tap Camera to grant it.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = Color(0xFF5D4037)
                )
            }
        }

        // Analyze button (only when file selected)
        val hasFile = imageUri != null ||
                (vm.inputType == "pdf" && vm.capturedImageUri != null)

        if (hasFile) {
            Button(
                onClick = {
                    navController.navigate(Screen.Processing.route)
                    val uri = vm.capturedImageUri ?: return@Button

                    if (vm.inputType == "pdf") {
                        vm.extractPdfText(context, uri) {
                            navController.navigate(Screen.Confirm.route) {
                                popUpTo(Screen.Processing.route) { inclusive = true }
                            }
                        }
                    } else {
                        vm.runOCR(context, uri) {
                            navController.navigate(Screen.Confirm.route) {
                                popUpTo(Screen.Processing.route) { inclusive = true }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2ECC71)
                )
            ) {
                Text(
                    "Analyze Report →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Auto-launch camera after permission granted
    LaunchedEffect(cameraPermission.status.isGranted) {
        // nothing auto — user taps the button again after granting
    }
}