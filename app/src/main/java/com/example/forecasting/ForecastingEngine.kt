package com.example.forecasting

import com.example.model.DailySalesPoint
import com.example.model.DecompositionResult
import com.example.model.ForecastResult
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

object ForecastingEngine {

    val DAY_NAMES_ORDER = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    /**
     * Classical Additive Time-Series Decomposition
     * Y_t = Trend_t + Seasonal_t + Residual_t
     * m = 7 (musiman mingguan)
     */
    fun decomposeAdditive(
        series: List<DailySalesPoint>,
        m: Int = 7
    ): DecompositionResult {
        val n = series.size
        val y = series.map { it.qty }
        val dates = series.map { it.date }
        val dayNames = series.map { it.dayName }

        val trend = MutableList<Double?>(n) { null }
        val detrended = MutableList<Double?>(n) { null }
        val k = m / 2

        // 1. Centered Moving Average (CMA) untuk Trend
        for (i in k until (n - k)) {
            var sum = 0.0
            for (j in -k..k) {
                sum += y[i + j]
            }
            val tVal = sum / m
            trend[i] = (tVal * 100.0).roundToInt() / 100.0
            detrended[i] = y[i] - tVal
        }

        // 2. Average detrended values per day of week (0 to m-1)
        val daySums = mutableMapOf<String, Double>()
        val dayCounts = mutableMapOf<String, Int>()

        for (name in DAY_NAMES_ORDER) {
            daySums[name] = 0.0
            dayCounts[name] = 0
        }

        for (i in 0 until n) {
            val dVal = detrended[i]
            if (dVal != null) {
                val day = dayNames[i]
                daySums[day] = (daySums[day] ?: 0.0) + dVal
                dayCounts[day] = (dayCounts[day] ?: 0) + 1
            }
        }

        val rawIndices = mutableMapOf<String, Double>()
        var sumRaw = 0.0
        for (day in DAY_NAMES_ORDER) {
            val count = dayCounts[day] ?: 0
            val avg = if (count > 0) (daySums[day] ?: 0.0) / count else 0.0
            rawIndices[day] = avg
            sumRaw += avg
        }

        // Normalisasi agar total efek musiman sum = 0 (additive property)
        val meanOffset = sumRaw / m
        val normalizedIndices = mutableMapOf<String, Double>()
        for (day in DAY_NAMES_ORDER) {
            val norm = ((rawIndices[day] ?: 0.0) - meanOffset)
            normalizedIndices[day] = (norm * 100.0).roundToInt() / 100.0
        }

        // 3. Bangun array Seasonal & Residual
        val seasonal = mutableListOf<Double>()
        val residual = MutableList<Double?>(n) { null }

        for (i in 0 until n) {
            val day = dayNames[i]
            val sVal = normalizedIndices[day] ?: 0.0
            seasonal.add(sVal)

            val tVal = trend[i]
            if (tVal != null) {
                val res = y[i] - tVal - sVal
                residual[i] = (res * 100.0).roundToInt() / 100.0
            }
        }

        return DecompositionResult(
            dates = dates,
            dayNames = dayNames,
            observed = y,
            trend = trend,
            seasonal = seasonal,
            residual = residual,
            dayOfWeekIndices = normalizedIndices
        )
    }

    /**
     * Holt-Winters Additive Method (ETS - Error, Trend, Seasonal)
     */
    fun forecastHoltWinters(
        series: List<DailySalesPoint>,
        horizon: Int = 7,
        m: Int = 7,
        alpha: Double = 0.35,
        beta: Double = 0.15,
        gamma: Double = 0.25,
        currentStock: Int = 12
    ): ForecastResult {
        val n = series.size
        val y = series.map { it.qty }

        // Initial Level (L0) and Trend (b0)
        val l0 = y.take(m).average()
        val secondPeriod = y.drop(m).take(m).average()
        val b0 = (secondPeriod - l0) / m

        // Initial seasonal components for period 0..m-1
        val s = DoubleArray(m) { i -> y[i] - l0 }

        var level = l0
        var trend = b0

        val inSampleForecast = MutableList<Double?>(n) { null }

        // Run recursions
        for (t in 0 until n) {
            val prevLevel = level
            val prevTrend = trend
            val seasonIdx = t % m
            val prevSeasonal = s[seasonIdx]

            if (t >= m) {
                inSampleForecast[t] = max(0.0, prevLevel + prevTrend + prevSeasonal)
            }

            // Holt-Winters Additive updating formulas
            level = alpha * (y[t] - prevSeasonal) + (1.0 - alpha) * (prevLevel + prevTrend)
            trend = beta * (level - prevLevel) + (1.0 - beta) * prevTrend
            s[seasonIdx] = gamma * (y[t] - level) + (1.0 - gamma) * prevSeasonal
        }

        // Out-of-sample Forecast h = 1..horizon
        val forecastValues = mutableListOf<Int>()
        val futureDates = mutableListOf<String>()

        for (h in 1..horizon) {
            val targetSeason = (n + h - 1) % m
            val pointEstimate = level + (h * trend) + s[targetSeason]
            val safeForecast = max(0, pointEstimate.roundToInt())
            forecastValues.add(safeForecast)
            futureDates.add("H+$h")
        }

        // Calculate MAPE for accuracy
        var apeSum = 0.0
        var count = 0
        for (t in m until n) {
            val actual = y[t]
            val fitted = inSampleForecast[t]
            if (actual > 0 && fitted != null) {
                apeSum += abs((actual - fitted) / actual)
                count++
            }
        }

        val rawMape = if (count > 0) (apeSum / count) * 100.0 else 12.5
        val mape = (rawMape * 10.0).roundToInt() / 10.0
        val accuracy = max(0.0, ((100.0 - mape) * 10.0).roundToInt() / 10.0)

        val totalDemand = forecastValues.sum()
        // Safety stock buffer (15% dari demand)
        val safetyBuffer = (totalDemand * 0.15).roundToInt()
        val needed = totalDemand + safetyBuffer
        val recommendedRestock = max(0, needed - currentStock)

        return ForecastResult(
            dates = futureDates,
            forecastValues = forecastValues,
            mape = mape,
            accuracyPercentage = accuracy,
            alpha = alpha,
            beta = beta,
            gamma = gamma,
            currentStock = currentStock,
            totalForecastDemand = totalDemand,
            recommendedRestock = recommendedRestock
        )
    }
}
