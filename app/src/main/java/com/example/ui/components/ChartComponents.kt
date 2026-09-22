package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailySalesPoint
import com.example.model.DecompositionResult
import com.example.model.ForecastResult
import com.example.ui.theme.ChartForecast
import com.example.ui.theme.ChartObserved
import com.example.ui.theme.ChartResidual
import com.example.ui.theme.ChartSeasonal
import com.example.ui.theme.ChartTrend
import kotlin.math.max

@Composable
fun ForecastLineChart(
    history: List<DailySalesPoint>,
    forecast: ForecastResult,
    modifier: Modifier = Modifier
) {
    val historyValues = history.map { it.qty }
    val forecastValues = forecast.forecastValues.map { it.toDouble() }
    val allValues = historyValues + forecastValues

    val maxVal = (allValues.maxOrNull() ?: 100.0) * 1.15
    val minVal = 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Grafik Deret Waktu & Proyeksi ETS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${history.size} Hari Historis + ${forecast.forecastValues.size} Hari Prediksi Masa Depan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(ChartObserved, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Historis", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(ChartForecast, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prediksi", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val width = size.width
                val height = size.height
                val totalPoints = allValues.size
                if (totalPoints < 2) return@Canvas

                val stepX = width / (totalPoints - 1)

                // Draw background grid lines (horizontal)
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = height * (i.toFloat() / gridLines)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 1. Draw Historical line
                val histPath = Path()
                for (i in historyValues.indices) {
                    val normalizedY = ((historyValues[i] - minVal) / (maxVal - minVal)).toFloat()
                    val x = i * stepX
                    val y = height - (normalizedY * height)
                    if (i == 0) histPath.moveTo(x, y) else histPath.lineTo(x, y)
                }

                drawPath(
                    path = histPath,
                    color = ChartObserved,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Dots on historical points
                for (i in historyValues.indices step 2) {
                    val normalizedY = ((historyValues[i] - minVal) / (maxVal - minVal)).toFloat()
                    val x = i * stepX
                    val y = height - (normalizedY * height)
                    drawCircle(color = ChartObserved, radius = 3.5.dp.toPx(), center = Offset(x, y))
                }

                // 2. Draw Forecast projection line (dashed connection from last historical)
                val lastHistIdx = historyValues.lastIndex
                val forecastPath = Path()
                val lastNormY = ((historyValues.last() - minVal) / (maxVal - minVal)).toFloat()
                forecastPath.moveTo(lastHistIdx * stepX, height - (lastNormY * height))

                for (j in forecastValues.indices) {
                    val globalIdx = lastHistIdx + 1 + j
                    val normY = ((forecastValues[j] - minVal) / (maxVal - minVal)).toFloat()
                    val x = globalIdx * stepX
                    val y = height - (normY * height)
                    forecastPath.lineTo(x, y)
                }

                drawPath(
                    path = forecastPath,
                    color = ChartForecast,
                    style = Stroke(
                        width = 3.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                    )
                )

                // Dots on forecast points
                for (j in forecastValues.indices) {
                    val globalIdx = lastHistIdx + 1 + j
                    val normY = ((forecastValues[j] - minVal) / (maxVal - minVal)).toFloat()
                    val x = globalIdx * stepX
                    val y = height - (normY * height)
                    drawCircle(color = ChartForecast, radius = 4.5.dp.toPx(), center = Offset(x, y))
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(history.firstOrNull()?.date ?: "", style = MaterialTheme.typography.labelSmall)
                Text(history.lastOrNull()?.date ?: "", style = MaterialTheme.typography.labelSmall)
                Text("Prediksi +${forecast.forecastValues.size} Hari", style = MaterialTheme.typography.labelSmall, color = ChartForecast, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DecompositionSection(
    decomp: DecompositionResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tahapan Dekomposisi Deret Waktu (Additive)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Rumus: Y_t (Observed) = Trend_t + Seasonal_t + Residual_t",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            SingleDecompRow(
                title = "1. Observed (Y_t) - Data Aktual Penjualan",
                values = decomp.observed,
                color = ChartObserved,
                subtitle = "Fluktuasi harian asli gabungan tren dan pola mingguan"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SingleDecompRow(
                title = "2. Trend (T_t) - Centered Moving Average",
                values = decomp.trend.filterNotNull(),
                color = ChartTrend,
                subtitle = "Arah pergerakan umum jangka menengah (CMA m=7)"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SingleDecompRow(
                title = "3. Seasonal (S_t) - Pola Musiman Mingguan",
                values = decomp.seasonal,
                color = ChartSeasonal,
                subtitle = "Pola berulang tiap 7 hari (lonjakan hari Sabtu-Minggu)"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SingleDecompRow(
                title = "4. Residual (I_t) - Komponen Error / Acak",
                values = decomp.residual.filterNotNull(),
                color = ChartResidual,
                subtitle = "Variasi tidak teratur setelah detrending & deseasonalizing"
            )
        }
    }
}

@Composable
private fun SingleDecompRow(
    title: String,
    values: List<Double>,
    color: Color,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "n = ${values.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (values.isNotEmpty()) {
            val maxVal = max(values.maxOrNull() ?: 1.0, 1.0)
            val minVal = values.minOrNull() ?: 0.0

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                val w = size.width
                val h = size.height
                val stepX = w / (values.size - 1).coerceAtLeast(1)
                val path = Path()

                val range = (maxVal - minVal).coerceAtLeast(0.001)

                for (i in values.indices) {
                    val normY = ((values[i] - minVal) / range).toFloat()
                    val x = i * stepX
                    val y = h - (normY * h)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
fun DayOfWeekSeasonalBarChart(
    indices: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Indeks Pengaruh Hari (Weekend Surge Effect)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Selisih rata-rata penjualan per hari terhadap baseline tren (Unit: Cup)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { day ->
                    val value = indices[day] ?: 0.0
                    val isPositive = value >= 0
                    val barColor = if (isPositive) ChartSeasonal else Color(0xFFEF4444)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = (if (isPositive) "+" else "") + "%.1f".format(value),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val barHeight = (kotlin.math.abs(value) * 3.5).coerceIn(8.0, 70.0).dp

                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(barHeight)
                                .background(barColor, RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = day.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (day in listOf("Sabtu", "Minggu")) FontWeight.Bold else FontWeight.Normal,
                            color = if (day in listOf("Sabtu", "Minggu")) ChartObserved else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
