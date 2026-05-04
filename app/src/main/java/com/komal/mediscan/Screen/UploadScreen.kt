package com.komal.mediscan.Screen

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

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

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            imageUri = it
            vm.capturedImageUri = it
            vm.inputType = "image"
        }
    }

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

    //  UI
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
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))

            //  Title
            Text(
                "Scan Your Report",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.offset(y = (24).dp)
            )

            Spacer(Modifier.height(6.dp))



            Spacer(Modifier.height(28.dp))

            // Capture Box (CLICKABLE)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(24.dp)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(24.dp)
                    )
                    .clickable {
                        if (cameraPermission.status.isGranted) {
                            launchCamera()
                        } else {
                            cameraPermission.launchPermissionRequest()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {

                when {
                    imageUri != null -> {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    vm.inputType == "pdf" && vm.capturedImageUri != null -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text("PDF Selected", color = Color.White)
                        }
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(75.dp)
                                    .background(
                                        Color(0xFF5BAF7A),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(Modifier.height(18.dp))

                            Text(
                                "Capture Now",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                "Align medical report within frame",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(50.dp))

            //  Upload Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                UploadOption(
                    icon = Icons.Default.PhotoLibrary,
                    title = "Gallery",
                    subtitle = "Upload Photo",
                    modifier = Modifier.weight(1f)
                ) {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                UploadOption(
                    icon = Icons.Default.PictureAsPdf,
                    title = "Document",
                    subtitle = "Upload PDF",
                    modifier = Modifier.weight(1f)
                ) {
                    pdfLauncher.launch(arrayOf("application/pdf"))
                }
            }

            Spacer(Modifier.height(50.dp))

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
                        .height(58.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF22C55E)
                    )
                ) {
                    Text(
                        "Analyze Report",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun UploadOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(
                Color.White.copy(alpha = 0.06f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {

        Icon(icon, contentDescription = null, tint = Color(0xFF52FFA8))

        Spacer(Modifier.height(8.dp))

        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)

        Text(
            subtitle,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}