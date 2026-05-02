package com.komal.mediscan.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.komal.mediscan.MVVM.MediScanViewModel
import com.komal.mediscan.MVVM.TestResult
import com.komal.mediscan.Navigation.Screen

@Composable
fun ResultScreen(navController: NavController, vm: MediScanViewModel) {
    val result = vm.analysisResult ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Your Report Summary", fontSize = 26.sp, fontWeight = FontWeight.Bold)

        // Summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Overall", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                Text(result.patientSummary, fontSize = 15.sp, color = Color(0xFF1B5E20))
            }
        }

        Text("Test Results", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        result.testResults.forEach { test ->
            TestResultCard(test)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                vm.reset()
                navController.navigate(Screen.Welcome.route) { popUpTo(0) }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text("Scan Another Report")
        }
    }
}

@Composable
fun TestResultCard(test: TestResult) {
    val (bgColor, statusColor, icon) = when (test.status) {
        "High" -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "↑")
        "Low"  -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "↓")
        else   -> Triple(Color(0xFFF1F8E9), Color(0xFF33691E), "✓")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$icon ${test.testName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = statusColor,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        test.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column {
                    Text("Your Value", fontSize = 12.sp, color = Color.Gray)
                    Text(test.value, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Normal Range", fontSize = 12.sp, color = Color.Gray)
                    Text(test.normalRange, fontWeight = FontWeight.SemiBold)
                }
            }
            HorizontalDivider(color = statusColor.copy(alpha = 0.2f))
            Text(test.explanation, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}