package com.example.data

import com.example.forecasting.ForecastingEngine
import com.example.model.DailySalesPoint
import com.example.model.IceCreamProduct
import com.example.model.MutationType
import com.example.model.ProductCategory
import com.example.model.StockMutation

object SampleData {

    val initialProducts = listOf(
        IceCreamProduct(
            id = 1L,
            sku = "IC-VAN-01",
            name = "Vanilla Bourbon Pod",
            category = ProductCategory.SCOOP,
            costPrice = 11000.0,
            sellingPrice = 22000.0,
            stock = 14,
            minimumStock = 12,
            unit = "Cup",
            description = "Ekstrak vanilla bourbon Madagaskar asli dengan tekstur lembut creamy.",
            flavorEmoji = "🍦",
            accentHex = 0xFFF5E6CC
        ),
        IceCreamProduct(
            id = 2L,
            sku = "IC-CHO-02",
            name = "Belgian Dark Choco",
            category = ProductCategory.SCOOP,
            costPrice = 13000.0,
            sellingPrice = 25000.0,
            stock = 28,
            minimumStock = 10,
            unit = "Cup",
            description = "Cokelat hitam Belgia 70% kaya antioksidan dengan taburan choco chips.",
            flavorEmoji = "🍫",
            accentHex = 0xFF795548
        ),
        IceCreamProduct(
            id = 3L,
            sku = "IC-MAT-03",
            name = "Uji Kyoto Matcha",
            category = ProductCategory.SOFT_SERVE,
            costPrice = 12500.0,
            sellingPrice = 24000.0,
            stock = 6, // Low stock alert!
            minimumStock = 10,
            unit = "Cup",
            description = "Bubuk teh hijau autentik langsung dari Uji Kyoto dengan rasa umami khas.",
            flavorEmoji = "🍵",
            accentHex = 0xFF4CAF50
        ),
        IceCreamProduct(
            id = 4L,
            sku = "IC-STR-04",
            name = "Wild Strawberry Sundae",
            category = ProductCategory.SUNDAE,
            costPrice = 14000.0,
            sellingPrice = 28000.0,
            stock = 18,
            minimumStock = 8,
            unit = "Porsi",
            description = "Sundae strawberry segar dengan siraman selai buah asli dan whipped cream.",
            flavorEmoji = "🍓",
            accentHex = 0xFFE91E63
        ),
        IceCreamProduct(
            id = 5L,
            sku = "IC-CAR-05",
            name = "Salted Caramel Waffle",
            category = ProductCategory.SCOOP,
            costPrice = 12000.0,
            sellingPrice = 24000.0,
            stock = 22,
            minimumStock = 10,
            unit = "Cup",
            description = "Saus karamel mentega gurih berpadu dengan garam laut Bali artisanal.",
            flavorEmoji = "🍯",
            accentHex = 0xFFFF9800
        ),
        IceCreamProduct(
            id = 6L,
            sku = "IC-MAN-06",
            name = "Mango Passionfruit Sorbet",
            category = ProductCategory.SCOOP,
            costPrice = 10000.0,
            sellingPrice = 20000.0,
            stock = 0, // Out of stock!
            minimumStock = 8,
            unit = "Cup",
            description = "Sorbet buah mangga harum manis 100% vegan tanpa susu, segar dingin.",
            flavorEmoji = "🥭",
            accentHex = 0xFFFFC107
        ),
        IceCreamProduct(
            id = 7L,
            sku = "IC-CKC-07",
            name = "Cookies & Cream Swirl",
            category = ProductCategory.SOFT_SERVE,
            costPrice = 11500.0,
            sellingPrice = 23000.0,
            stock = 32,
            minimumStock = 10,
            unit = "Cup",
            description = "Remahan biskuit cokelat renyah melimpah dalam soft ice cream susu murni.",
            flavorEmoji = "🍪",
            accentHex = 0xFF607D8B
        ),
        IceCreamProduct(
            id = 8L,
            sku = "IC-TOP-08",
            name = "Extra Roasted Almond & Fudge",
            category = ProductCategory.BEVERAGE,
            costPrice = 3000.0,
            sellingPrice = 6000.0,
            stock = 45,
            minimumStock = 15,
            unit = "Pcs",
            description = "Topping premium kacang almond panggang dan saus cokelat leleh hangat.",
            flavorEmoji = "🌰",
            accentHex = 0xFF8D6E63
        )
    )

    val initialMutations = listOf(
        StockMutation(
            id = 101L,
            productId = 1L,
            productName = "Vanilla Bourbon Pod",
            type = MutationType.IN,
            quantity = 30,
            stockBefore = 5,
            stockAfter = 35,
            referenceId = "PO-20260918-01",
            notes = "Restok kiriman distributor susu utama",
            timestamp = "2026-09-18 09:15"
        ),
        StockMutation(
            id = 102L,
            productId = 3L,
            productName = "Uji Kyoto Matcha",
            type = MutationType.SALE,
            quantity = 14,
            stockBefore = 20,
            stockAfter = 6,
            referenceId = "INV-20260920-04",
            notes = "Penjualan weekend ramai event",
            timestamp = "2026-09-20 20:30"
        ),
        StockMutation(
            id = 103L,
            productId = 6L,
            productName = "Mango Passionfruit Sorbet",
            type = MutationType.WASTE,
            quantity = 4,
            stockBefore = 4,
            stockAfter = 0,
            referenceId = "WST-20260921-01",
            notes = "Freezer display suhu naik, meleleh sebagian",
            timestamp = "2026-09-21 11:00"
        )
    )

    /**
     * 21-day sales history with characteristic weekend spikes for ice cream shop
     */
    fun generateProductHistory(productId: Long): List<DailySalesPoint> {
        val baseMultiplier = when (productId) {
            1L -> 1.2  // Vanilla: High steady demand
            2L -> 1.4  // Belgian Choco: Top seller
            3L -> 0.9  // Matcha: Popular on weekends
            4L -> 1.1  // Strawberry Sundae
            5L -> 1.0  // Salted Caramel
            6L -> 0.8  // Mango
            7L -> 1.3  // Cookies & Cream
            else -> 1.0
        }

        val history = mutableListOf<DailySalesPoint>()
        val dayNames = ForecastingEngine.DAY_NAMES_ORDER // Senin..Minggu

        // 3 full cycles of weeks (21 days)
        for (i in 0 until 21) {
            val dayName = dayNames[i % 7]
            val dayNumber = i + 1
            val dateStr = "Sep %02d".format(dayNumber)

            // Pattern: Senin-Kamis 18-28 cup, Jumat 35-45 cup, Sabtu-Minggu 55-75 cup
            val baseForDay = when (dayName) {
                "Senin" -> 19.0
                "Selasa" -> 22.0
                "Rabu" -> 20.0
                "Kamis" -> 25.0
                "Jumat" -> 38.0
                "Sabtu" -> 58.0
                "Minggu" -> 64.0
                else -> 25.0
            }

            // Upward gentle trend (+0.8 per day) and minor random variation
            val trendFactor = i * 0.7
            val randomNoise = ((i * 17) % 5) - 2.0
            val qty = (baseForDay * baseMultiplier + trendFactor + randomNoise).coerceAtLeast(8.0)

            history.add(
                DailySalesPoint(
                    date = dateStr,
                    dayName = dayName,
                    qty = qty
                )
            )
        }
        return history
    }
}
