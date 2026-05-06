package com.komal.mediscan.Screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.komal.mediscan.MVVM.MediScanViewModel
import com.komal.mediscan.ML.MediScanTFLite
import com.komal.mediscan.Navigation.Screen

@Composable
fun ResultScreen(navController: NavController, vm: MediScanViewModel) {

    val context = LocalContext.current
    val predictions = vm.localPredictions

    if (predictions.isEmpty()) {
        Box(
            Modifier
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No test values found", color = Color.White)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val highCount = predictions.count { it.status == "High" }
    val lowCount = predictions.count { it.status == "Low" }
    val normalCount = predictions.count { it.status == "Normal" }

    val summaryText = when {
        highCount == 0 && lowCount == 0 ->
            "All ${predictions.size} values are normal."
        highCount > 0 && lowCount > 0 ->
            "$highCount high, $lowCount low values found."
        highCount > 0 ->
            "$highCount values are high."
        else ->
            "$lowCount values are low."
    }

    fun buildShareText(): String { /* SAME AS YOUR CODE */ return "" }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 🔹 Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your Report",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, buildShareText())
                    }
                    context.startActivity(Intent.createChooser(intent, "Share"))
                }) {
                    Icon(Icons.Default.Share, null, tint = Color(0xFF22C55E))
                }
            }

            // 🔹 Summary Card (Glass)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.06f)
                )
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        "Overall Summary",
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        summaryText,
                        color = Color.White.copy(0.85f)
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatChip("✓", normalCount, Color(0xFF22C55E))
                        StatChip("↑", highCount, Color(0xFFFF7043))
                        StatChip("↓", lowCount, Color(0xFF42A5F5))
                    }
                }
            }

            Text(
                "Test Details",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            predictions.forEach {
                TestResultCard(it)
            }

            // 🔹 CTA
            Button(
                onClick = {
                    vm.reset()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E)
                )
            ) {
                Text("Scan Another Report", color = Color.Black)
            }
        }
    }
}
@Composable
fun TestResultCard(pred: MediScanTFLite.TFLitePrediction) {

    val accentColor = when (pred.status) {
        "High" -> Color(0xFFFF7043)
        "Low" -> Color(0xFF42A5F5)
        else -> Color(0xFF22C55E)
    }

    val icon = when (pred.status) {
        "High" -> "↑"
        "Low" -> "↓"
        else -> "✓"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111827) // deep dark
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$icon ${pred.testName.replace("_", " ")
                        .replaceFirstChar { it.uppercase() }}",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        pred.status,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                InfoBlock("Value", pred.value.toString())
                InfoBlock("Normal", pred.normalRange)
                InfoBlock(
                    "Confidence",
                    "${(pred.confidence * 100).toInt()}%",
                    accentColor
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            Text(
                explanationFor(pred),
                color = Color.White.copy(0.8f),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun InfoBlock(label: String, value: String, color: Color = Color.White) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontWeight = FontWeight.SemiBold, color = color)
    }
}

fun explanationFor(pred: MediScanTFLite.TFLitePrediction): String {
    val name = pred.testName
    return when (pred.status) {
        "Normal" -> when {
            name.contains("hemoglobin") ->
                "Your hemoglobin is normal. Your blood is carrying oxygen well."
            name.contains("glucose") ->
                "Your blood sugar is normal. Your body is managing sugar well."
            name.contains("creatinine") ->
                "Your creatinine is normal. Your kidneys are filtering well."
            name.contains("tsh") ->
                "Your thyroid hormone level is normal. Thyroid is working fine."
            name.contains("cholesterol") ->
                "Your cholesterol is normal. Good for heart health."
            name.contains("hba1c") ->
                "Your HbA1c is normal. Your blood sugar control is good."
            name.contains("wbc") ->
                "Your white blood cell count is normal. Immune system looks fine."
            name.contains("platelets") ->
                "Your platelet count is normal. Blood clotting should be fine."
            name.contains("sodium") ->
                "Your sodium level is normal. Body fluids are well balanced."
            name.contains("potassium") ->
                "Your potassium is normal. Heart and muscle function looks good."
            name.contains("vitamin_d") ->
                "Your Vitamin D level is normal. Bones and immunity are supported."
            name.contains("vitamin_b12") ->
                "Your Vitamin B12 is normal. Nerve health looks good."
            name.contains("iron") ->
                "Your iron level is normal. Red blood cell production is fine."
            name.contains("calcium") ->
                "Your calcium is normal. Bones and muscles are well supported."
            name.contains("uric") ->
                "Your uric acid is normal. Low risk of gout or kidney stones."
            name.contains("triglycerides") ->
                "Your triglycerides are normal. Fat levels in blood look healthy."
            name.contains("hdl") ->
                "Your HDL (good cholesterol) is normal. Heart health looks fine."
            name.contains("ldl") ->
                "Your LDL (bad cholesterol) is normal. Good for heart health."
            else ->
                "This value is within the normal range. No concern needed."
        }

        "High" -> when {
            name.contains("hemoglobin") ->
                "Your hemoglobin is higher than normal. This can sometimes mean dehydration. Drink more water and consult your doctor."
            name.contains("glucose") ->
                "Your blood sugar is high. This could be a sign of diabetes or pre-diabetes. Reduce sugary foods and see a doctor soon."
            name.contains("creatinine") ->
                "Your creatinine is high. This may indicate your kidneys are under stress. Drink water and consult a doctor."
            name.contains("tsh") ->
                "Your TSH is high. This may mean your thyroid is underactive (hypothyroidism). A doctor can advise on treatment."
            name.contains("cholesterol") ->
                "Your cholesterol is high. This increases heart disease risk. Reduce oily foods and exercise regularly."
            name.contains("hba1c") ->
                "Your HbA1c is high. This suggests poor blood sugar control over the past 3 months. See a doctor for diabetes management."
            name.contains("wbc") ->
                "Your white blood cells are high. This often means your body is fighting an infection. Consult a doctor."
            name.contains("uric") ->
                "Your uric acid is high. This can cause gout or kidney stones. Drink plenty of water and reduce red meat."
            name.contains("triglycerides") ->
                "Your triglycerides are high. Reduce sugar, alcohol, and fatty foods. Regular exercise helps a lot."
            name.contains("ldl") ->
                "Your LDL (bad cholesterol) is high. This raises heart disease risk. Eat less saturated fat and exercise more."
            name.contains("sgpt") || name.contains("alt") ->
                "Your liver enzyme (SGPT/ALT) is high. This may indicate liver stress. Avoid alcohol and consult a doctor."
            name.contains("sgot") || name.contains("ast") ->
                "Your liver enzyme (SGOT/AST) is high. The liver may be inflamed. Avoid alcohol and see a doctor."
            name.contains("bilirubin") ->
                "Your bilirubin is high. This can cause jaundice. See a doctor promptly."
            name.contains("calcium") ->
                "Your calcium is high. This can affect kidneys and heart. Consult a doctor."
            name.contains("potassium") ->
                "Your potassium is high. This can affect heart rhythm. Consult a doctor promptly."
            else ->
                "This value is above the normal range. Please consult your doctor for advice."
        }

        else -> when { // "Low"
            name.contains("hemoglobin") ->
                "Your hemoglobin is low — this is called anemia. You may feel tired or dizzy. Eat iron-rich foods like spinach and lentils. See a doctor."
            name.contains("glucose") ->
                "Your blood sugar is low (hypoglycemia). Eat something sweet immediately and consult a doctor if this happens often."
            name.contains("creatinine") ->
                "Your creatinine is slightly low. This is usually not a concern but mention it to your doctor."
            name.contains("tsh") ->
                "Your TSH is low. This may mean your thyroid is overactive (hyperthyroidism). Consult a doctor."
            name.contains("wbc") ->
                "Your white blood cells are low. Your immunity may be weaker than normal. Avoid crowded places and see a doctor."
            name.contains("platelets") ->
                "Your platelets are low. This can affect blood clotting. Avoid injuries and consult a doctor promptly."
            name.contains("sodium") ->
                "Your sodium is low. This can cause fatigue or confusion. Drink electrolyte fluids and consult a doctor."
            name.contains("potassium") ->
                "Your potassium is low. This can cause muscle weakness. Eat bananas, potatoes, and consult your doctor."
            name.contains("vitamin_d") ->
                "Your Vitamin D is low. This is very common. Take Vitamin D supplements and get some sunlight daily."
            name.contains("vitamin_b12") ->
                "Your Vitamin B12 is low. This can cause fatigue and nerve problems. Take B12 supplements or eat more dairy and eggs."
            name.contains("iron") ->
                "Your iron is low. This leads to anemia and tiredness. Eat spinach, beans, and meat. A doctor may prescribe iron tablets."
            name.contains("hdl") ->
                "Your HDL (good cholesterol) is low. Exercise regularly and eat healthy fats like nuts and avocado to raise it."
            name.contains("calcium") ->
                "Your calcium is low. This can weaken bones. Drink milk, eat dairy, and consider supplements after consulting a doctor."
            else ->
                "This value is below the normal range. Please consult your doctor for advice."
        }
    }
}

@Composable
fun StatChip(label: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = "$label $count",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}