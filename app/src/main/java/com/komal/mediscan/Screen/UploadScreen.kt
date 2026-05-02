package com.komal.mediscan.Screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.komal.mediscan.MVVM.MediScanViewModel
import com.komal.mediscan.Navigation.Screen
import java.io.File

@Composable
fun UploadScreen(navController: NavController, vm: MediScanViewModel) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Photo picker
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            imageUri = it
            vm.capturedImageUri = it
        }
    }

    // Camera launcher
    val cameraFile = remember { mutableStateOf<File?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraFile.value?.let {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", it)
                imageUri = uri
                vm.capturedImageUri = uri
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Scan Your Report", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Upload or photograph your medical report", color = Color.Gray)

        // Preview box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF0F4F8)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(model = imageUri, contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddCircle, null,
                        Modifier.size(64.dp), tint = Color(0xFF90A4AE))
                    Text("No image selected", color = Color.Gray)
                }
            }
        }

        // Buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.MailOutline, null)
                Spacer(Modifier.width(8.dp))
                Text("Gallery")
            }
            Button(
                onClick = {
                    val file = File(context.cacheDir, "mediscan_${System.currentTimeMillis()}.jpg")
                    cameraFile.value = file
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    cameraLauncher.launch(uri)
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E6091))
            ) {
                Icon(Icons.Default.Face, null)
                Spacer(Modifier.width(8.dp))
                Text("Camera")
            }
        }

        if (imageUri != null) {
            Button(
                onClick = {
                    navController.navigate(Screen.Processing.route)
                    vm.runOCR(context, imageUri!!) {
                        navController.navigate(Screen.Confirm.route) {
                            popUpTo(Screen.Processing.route) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
            ) {
                Text("Analyze Report →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}