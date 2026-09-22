package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SampleData
import com.example.ui.components.DayOfWeekSeasonalBarChart
import com.example.ui.components.DecompositionSection
import com.example.ui.components.ForecastLineChart
import com.example.ui.theme.ChartForecast
import com.example.ui.theme.CoralRose
import com.example.ui.theme.MintMatcha
import com.example.ui.theme.WarmWaffle
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedProductId.collectAsStateWithLifecycle()
    val forecastHorizon by viewModel.forecastHorizon.collectAsStateWithLifecycle()
    val alpha by viewModel.alpha.collectAsStateWithLifecycle()
    val beta by viewModel.beta.collectAsStateWithLifecycle()
    val gamma by viewModel.gamma.collectAsStateWithLifecycle()

    val currentProduct by viewModel.currentForecastProduct.collectAsStateWithLifecycle()
    val decomposition by viewModel.decompositionResult.collectAsStateWithLifecycle()
    val forecastResult by viewModel.forecastResult.collectAsStateWithLifecycle()

    val history = remember(selectedId) {
        SampleData.generateProductHistory(selectedId)
    }

    var showParamTuning by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title & Method header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Prediksi Penjualan (ETS)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Model Holt-Winters & Time-Series Decomposition",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Surface(
                color = CoralRose.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable { showParamTuning = !showParamTuning }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = CoralRose, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showParamTuning) "Tutup Tuning" else "Tuning Model",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CoralRose
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Product Selector Chips
        Text(
            text = "Pilih Varian Es Krim:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(products) { prod ->
                val isSelected = prod.id == selectedId
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) CoralRose else MaterialTheme.colorScheme.surface,
                    shadowElevation = if (isSelected) 4.dp else 1.dp,
                    modifier = Modifier.clickable { viewModel.selectProductForForecast(prod.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(prod.flavorEmoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = prod.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizon selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Horizon Prediksi:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(7, 14, 21).forEach { days ->
                    FilterChip(
                        selected = forecastHorizon == days,
                        onClick = { viewModel.setForecastHorizon(days) },
                        label = { Text("$days Hari", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Expandable Parameter Tuning
        if (showParamTuning) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Parameter Smoothing Holt-Winters:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)

                    // Alpha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Alpha (Level) : %.2f".format(alpha), style = MaterialTheme.typography.labelSmall)
                    }
                    Slider(
                        value = alpha.toFloat(),
                        onValueChange = { viewModel.setParameters(it.toDouble(), beta, gamma) },
                        valueRange = 0.05f..0.95f,
                        colors = SliderDefaults.colors(thumbColor = CoralRose, activeTrackColor = CoralRose)
                    )

                    // Beta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Beta (Trend) : %.2f".format(beta), style = MaterialTheme.typography.labelSmall)
                    }
                    Slider(
                        value = beta.toFloat(),
                        onValueChange = { viewModel.setParameters(alpha, it.toDouble(), gamma) },
                        valueRange = 0.01f..0.50f,
                        colors = SliderDefaults.colors(thumbColor = WarmWaffle, activeTrackColor = WarmWaffle)
                    )

                    // Gamma
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gamma (Seasonal) : %.2f".format(gamma), style = MaterialTheme.typography.labelSmall)
                    }
                    Slider(
                        value = gamma.toFloat(),
                        onValueChange = { viewModel.setParameters(alpha, beta, it.toDouble()) },
                        valueRange = 0.05f..0.80f,
                        colors = SliderDefaults.colors(thumbColor = MintMatcha, activeTrackColor = MintMatcha)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Evaluation Summary KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // MAPE & Accuracy Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Akurasi Model", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(
                        text = "${forecastResult.accuracyPercentage}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MintMatcha
                    )
                    Text("MAPE: ${forecastResult.mape}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Total Projected Demand Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Perkiraan Permintaan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(
                        text = "${forecastResult.totalForecastDemand} Cup",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = ChartForecast
                    )
                    Text("Horizon: $forecastHorizon Hari", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Smart Restock Recommendation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (forecastResult.recommendedRestock > 0) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (forecastResult.recommendedRestock > 0) WarmWaffle else MintMatcha,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (forecastResult.recommendedRestock > 0) Icons.Default.Lightbulb else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (forecastResult.recommendedRestock > 0) "Rekomendasi Restok Bahan Baku" else "Stok Saat Ini Masih Mencukupi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (forecastResult.recommendedRestock > 0) Color(0xFF92400E) else Color(0xFF065F46)
                    )
                    Text(
                        text = "Stok saat ini ${currentProduct?.stock ?: 0} ${currentProduct?.unit ?: "Cup"}. Diproyeksikan butuh ${forecastResult.totalForecastDemand} ${currentProduct?.unit ?: "Cup"} (+ buffer 15%)." +
                                if (forecastResult.recommendedRestock > 0) " Disarankan order tambahan: +${forecastResult.recommendedRestock} ${currentProduct?.unit ?: "Cup"}." else " Tidak perlu restok darurat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (forecastResult.recommendedRestock > 0) Color(0xFF78350F) else Color(0xFF047857)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 1: Historical vs Forecast
        ForecastLineChart(history = history, forecast = forecastResult)

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 2: Time Series Decomposition (4 Components)
        DecompositionSection(decomp = decomposition)

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 3: Weekly Day of Week Bar Chart (Seasonality)
        DayOfWeekSeasonalBarChart(indices = decomposition.dayOfWeekIndices)

        Spacer(modifier = Modifier.height(16.dp))

        // Academic Methodology Explanation for Thesis Defense
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = CoralRose)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Landasan Teori Tugas Akhir (ETS & Dekomposisi)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "1. Pengecekan Dekomposisi Aditif:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Persamaan deret waktu: Y(t) = T(t) + S(t) + I(t)\n" +
                            "• T(t): Komponen tren diisolasi dengan Centered Moving Average panjang siklus m=7.\n" +
                            "• S(t): Pola musiman mingguan dihitung dari detrending rata-rata harian dengan normalisasi sum = 0.\n" +
                            "• I(t): Residual/error acak yang tersisa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "2. Pemodelan Holt-Winters Additive:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "• Level: L(t) = α(Y(t) - S(t-m)) + (1-α)(L(t-1) + b(t-1))\n" +
                            "• Trend: b(t) = β(L(t) - L(t-1)) + (1-β)b(t-1)\n" +
                            "• Seasonal: S(t) = γ(Y(t) - L(t)) + (1-γ)S(t-m)\n" +
                            "• Forecast h langkah: Ŷ(t+h) = L(t) + h·b(t) + S(t+h-m)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
