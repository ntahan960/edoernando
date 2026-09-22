package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.forecasting.ForecastingEngine
import com.example.model.CartItem
import com.example.model.ContainerChoice
import com.example.model.DecompositionResult
import com.example.model.ForecastResult
import com.example.model.IceCreamProduct
import com.example.model.MutationType
import com.example.model.PaymentMethod
import com.example.model.ProductCategory
import com.example.model.StockMutation
import com.example.model.SyncStatus
import com.example.model.TransactionRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainViewModel : ViewModel() {

    private val _products = MutableStateFlow(SampleData.initialProducts)
    val products: StateFlow<List<IceCreamProduct>> = _products.asStateFlow()

    private val _mutations = MutableStateFlow(SampleData.initialMutations)
    val mutations: StateFlow<List<StockMutation>> = _mutations.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionRecord>>(emptyList())
    val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ProductCategory.ALL)
    val selectedCategory: StateFlow<ProductCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Offline Mode & Security Simulation
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _jwtTokenState = MutableStateFlow(
        JwtState(
            accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwibmFtZSI6Ikthc2lyIEVzIEtyaW0iLCJyb2xlIjoiY2FzaGllciIsImV4cCI6MTgwMDAwMDAwMH0",
            refreshToken = "d7a8f9c1b2e3456789abcdef0123456789abcdef0123456789abcdef01234567",
            isSecureStorageEncrypted = true,
            keystoreAlgorithm = "AES-256-GCM (Hardware Backed)"
        )
    )
    val jwtTokenState: StateFlow<JwtState> = _jwtTokenState.asStateFlow()

    // Forecasting State
    private val _selectedProductId = MutableStateFlow(1L)
    val selectedProductId: StateFlow<Long> = _selectedProductId.asStateFlow()

    private val _forecastHorizon = MutableStateFlow(7)
    val forecastHorizon: StateFlow<Int> = _forecastHorizon.asStateFlow()

    private val _alpha = MutableStateFlow(0.35)
    val alpha: StateFlow<Double> = _alpha.asStateFlow()

    private val _beta = MutableStateFlow(0.15)
    val beta: StateFlow<Double> = _beta.asStateFlow()

    private val _gamma = MutableStateFlow(0.25)
    val gamma: StateFlow<Double> = _gamma.asStateFlow()

    val currentForecastProduct: StateFlow<IceCreamProduct?> = combine(
        _products, _selectedProductId
    ) { prods, id ->
        prods.find { it.id == id } ?: prods.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SampleData.initialProducts.first())

    val decompositionResult: StateFlow<DecompositionResult> = combine(
        _selectedProductId
    ) { id ->
        val history = SampleData.generateProductHistory(id[0])
        ForecastingEngine.decomposeAdditive(history, m = 7)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ForecastingEngine.decomposeAdditive(SampleData.generateProductHistory(1L))
    )

    val forecastResult: StateFlow<ForecastResult> = combine(
        _selectedProductId, _forecastHorizon, _alpha, _beta, _gamma, _products
    ) { values ->
        val id = values[0] as Long
        val horizon = values[1] as Int
        val a = values[2] as Double
        val b = values[3] as Double
        val g = values[4] as Double
        val prods = values[5] as List<IceCreamProduct>

        val product = prods.find { it.id == id }
        val currentStock = product?.stock ?: 10
        val history = SampleData.generateProductHistory(id)

        ForecastingEngine.forecastHoltWinters(
            series = history,
            horizon = horizon,
            m = 7,
            alpha = a,
            beta = b,
            gamma = g,
            currentStock = currentStock
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ForecastingEngine.forecastHoltWinters(SampleData.generateProductHistory(1L))
    )

    // --- POS Actions ---
    fun selectCategory(category: ProductCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: IceCreamProduct, container: ContainerChoice = ContainerChoice.CUP) {
        if (product.stock <= 0) return
        val currentList = _cart.value.toMutableList()
        val existingIndex = currentList.indexOfFirst {
            it.product.id == product.id && it.container == container
        }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            if (existing.quantity < product.stock) {
                currentList[existingIndex] = existing.copy(quantity = existing.quantity + 1)
            }
        } else {
            currentList.add(CartItem(product = product, quantity = 1, container = container))
        }
        _cart.value = currentList
    }

    fun updateCartQuantity(item: CartItem, delta: Int) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst {
            it.product.id == item.product.id && it.container == item.container
        }
        if (index >= 0) {
            val newQty = currentList[index].quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else if (newQty <= item.product.stock) {
                currentList[index] = currentList[index].copy(quantity = newQty)
            }
        }
        _cart.value = currentList
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun processCheckout(paymentMethod: PaymentMethod, paidAmount: Double): TransactionRecord? {
        val currentCart = _cart.value
        if (currentCart.isEmpty()) return null

        val total = currentCart.sumOf { it.itemPrice }
        val change = (paidAmount - total).coerceAtLeast(0.0)
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val invoice = "INV-" + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())

        val isOffline = _isOfflineMode.value
        val syncStatus = if (isOffline) SyncStatus.PENDING_OFFLINE else SyncStatus.SYNCED

        val transaction = TransactionRecord(
            id = UUID.randomUUID().toString(),
            invoiceNo = invoice,
            items = currentCart,
            totalAmount = total,
            paidAmount = paidAmount,
            changeAmount = change,
            paymentMethod = paymentMethod,
            timestamp = now,
            syncStatus = syncStatus
        )

        // Deduct stocks & log mutations
        val updatedProducts = _products.value.map { prod ->
            val boughtItem = currentCart.find { it.product.id == prod.id }
            if (boughtItem != null) {
                val newStock = (prod.stock - boughtItem.quantity).coerceAtLeast(0)
                prod.copy(stock = newStock)
            } else {
                prod
            }
        }
        _products.value = updatedProducts

        // Log mutation
        val newMutations = _mutations.value.toMutableList()
        currentCart.forEach { item ->
            val before = item.product.stock
            val after = (before - item.quantity).coerceAtLeast(0)
            newMutations.add(
                0,
                StockMutation(
                    id = System.currentTimeMillis() + item.product.id,
                    productId = item.product.id,
                    productName = item.product.name,
                    type = MutationType.SALE,
                    quantity = item.quantity,
                    stockBefore = before,
                    stockAfter = after,
                    referenceId = invoice,
                    notes = "Penjualan via POS (${item.container.label})",
                    timestamp = now
                )
            )
        }
        _mutations.value = newMutations

        // Save transaction
        _transactions.value = listOf(transaction) + _transactions.value
        clearCart()
        return transaction
    }

    // --- Inventory Actions ---
    fun addStockAdjustment(
        productId: Long,
        type: MutationType,
        quantity: Int,
        notes: String
    ) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val ref = "ADJ-" + System.currentTimeMillis().toString().takeLast(6)

        val updatedProducts = _products.value.map { prod ->
            if (prod.id == productId) {
                val newStock = if (type.isPositive) {
                    prod.stock + quantity
                } else {
                    (prod.stock - quantity).coerceAtLeast(0)
                }

                // Log mutation
                val newMut = StockMutation(
                    id = System.currentTimeMillis(),
                    productId = prod.id,
                    productName = prod.name,
                    type = type,
                    quantity = quantity,
                    stockBefore = prod.stock,
                    stockAfter = newStock,
                    referenceId = ref,
                    notes = notes.ifBlank { "Penyesuaian manual dari Admin" },
                    timestamp = now
                )
                _mutations.value = listOf(newMut) + _mutations.value

                prod.copy(stock = newStock)
            } else {
                prod
            }
        }
        _products.value = updatedProducts
    }

    fun updateProductPrices(productId: Long, newCost: Double, newSelling: Double, newMinStock: Int) {
        _products.value = _products.value.map {
            if (it.id == productId) {
                it.copy(costPrice = newCost, sellingPrice = newSelling, minimumStock = newMinStock)
            } else it
        }
    }

    // --- Forecasting Actions ---
    fun selectProductForForecast(productId: Long) {
        _selectedProductId.value = productId
    }

    fun setForecastHorizon(days: Int) {
        _forecastHorizon.value = days
    }

    fun setParameters(a: Double, b: Double, g: Double) {
        _alpha.value = a
        _beta.value = b
        _gamma.value = g
    }

    // --- Offline & Sync ---
    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
    }

    fun syncPendingOfflineTransactions(): Int {
        val current = _transactions.value
        val pendingCount = current.count { it.syncStatus == SyncStatus.PENDING_OFFLINE }
        if (pendingCount > 0) {
            _transactions.value = current.map {
                if (it.syncStatus == SyncStatus.PENDING_OFFLINE) {
                    it.copy(syncStatus = SyncStatus.SYNCED)
                } else it
            }
        }
        return pendingCount
    }

    fun refreshJwtToken() {
        val newAccess = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXBkYXRlZCI6" + System.currentTimeMillis()
        val newRefresh = "rtk_" + UUID.randomUUID().toString().replace("-", "")
        _jwtTokenState.value = _jwtTokenState.value.copy(
            accessToken = newAccess,
            refreshToken = newRefresh
        )
    }
}

data class JwtState(
    val accessToken: String,
    val refreshToken: String,
    val isSecureStorageEncrypted: Boolean,
    val keystoreAlgorithm: String
)
