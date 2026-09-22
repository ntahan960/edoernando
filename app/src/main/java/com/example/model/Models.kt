package com.example.model

import java.util.UUID

enum class ProductCategory(val displayName: String) {
    ALL("Semua"),
    SCOOP("Scoop Ice Cream"),
    SOFT_SERVE("Soft Serve"),
    SUNDAE("Sundae Special"),
    BEVERAGE("Beverage & Topping")
}

data class IceCreamProduct(
    val id: Long,
    val sku: String,
    val name: String,
    val category: ProductCategory,
    val costPrice: Double,
    val sellingPrice: Double,
    var stock: Int,
    val minimumStock: Int = 10,
    val unit: String = "Cup",
    val description: String = "",
    val flavorEmoji: String = "🍦",
    val accentHex: Long = 0xFFFF8DA1
) {
    val isLowStock: Boolean get() = stock <= minimumStock
    val isOutOfStock: Boolean get() = stock <= 0
}

enum class MutationType(val label: String, val isPositive: Boolean) {
    IN("Barang Masuk", true),
    OUT("Barang Keluar", false),
    SALE("Penjualan Kasir", false),
    ADJUSTMENT("Penyesuaian", true),
    WASTE("Kerusakan/Melted", false)
}

data class StockMutation(
    val id: Long,
    val productId: Long,
    val productName: String,
    val type: MutationType,
    val quantity: Int,
    val stockBefore: Int,
    val stockAfter: Int,
    val referenceId: String,
    val notes: String,
    val timestamp: String
)

enum class ContainerChoice(val label: String, val extraPrice: Double) {
    CUP("Cup Kertas", 0.0),
    WAFFLE_CONE("Waffle Cone Renyah", 3000.0)
}

data class CartItem(
    val product: IceCreamProduct,
    var quantity: Int,
    val container: ContainerChoice = ContainerChoice.CUP
) {
    val itemPrice: Double get() = (product.sellingPrice + container.extraPrice) * quantity
}

enum class PaymentMethod(val label: String) {
    CASH("Tunai"),
    QRIS("QRIS"),
    DEBIT("Kartu Debit")
}

enum class SyncStatus(val label: String) {
    SYNCED("Tersinkronisasi"),
    PENDING_OFFLINE("Offline (Tersimpan Lokal)")
}

data class TransactionRecord(
    val id: String = UUID.randomUUID().toString(),
    val invoiceNo: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val paidAmount: Double,
    val changeAmount: Double,
    val paymentMethod: PaymentMethod,
    val timestamp: String,
    var syncStatus: SyncStatus = SyncStatus.SYNCED
)

data class DailySalesPoint(
    val date: String,
    val dayName: String,
    val qty: Double
)

data class DecompositionResult(
    val dates: List<String>,
    val dayNames: List<String>,
    val observed: List<Double>,
    val trend: List<Double?>,
    val seasonal: List<Double>,
    val residual: List<Double?>,
    val dayOfWeekIndices: Map<String, Double> // e.g. "Senin": -4.2, "Sabtu": +8.5
)

data class ForecastResult(
    val dates: List<String>,
    val forecastValues: List<Int>,
    val mape: Double,
    val accuracyPercentage: Double,
    val alpha: Double,
    val beta: Double,
    val gamma: Double,
    val currentStock: Int,
    val totalForecastDemand: Int,
    val recommendedRestock: Int
)
