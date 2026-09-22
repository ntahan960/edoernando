package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CartItem
import com.example.model.ContainerChoice
import com.example.model.IceCreamProduct
import com.example.model.PaymentMethod
import com.example.model.ProductCategory
import com.example.model.TransactionRecord
import com.example.ui.theme.CoralRose
import com.example.ui.theme.MintMatcha
import com.example.ui.theme.WarmWaffle
import com.example.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOfflineMode.collectAsStateWithLifecycle()

    var showCartSheet by remember { mutableStateOf(false) }
    var showProductOptionDialog by remember { mutableStateOf<IceCreamProduct?>(null) }
    var completedTransaction by remember { mutableStateOf<TransactionRecord?>(null) }

    val filteredProducts = remember(products, selectedCategory, searchQuery) {
        products.filter { prod ->
            val matchCategory = selectedCategory == ProductCategory.ALL || prod.category == selectedCategory
            val matchSearch = prod.name.contains(searchQuery, ignoreCase = true) || prod.sku.contains(searchQuery, ignoreCase = true)
            matchCategory && matchSearch
        }
    }

    val totalCartItems = cart.sumOf { it.quantity }
    val totalCartAmount = cart.sumOf { it.itemPrice }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Status bar for Online / Offline sync indicator
            Surface(
                color = if (isOffline) Color(0xFFFEF3C7) else Color(0xFFD1FAE5),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                            contentDescription = "Sync Status",
                            tint = if (isOffline) Color(0xFFD97706) else Color(0xFF059669),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOffline) "Mode Offline Aktif (Transaksi Disimpan Enkripsi)" else "Online (Terhubung ke Server API)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isOffline) Color(0xFF92400E) else Color(0xFF065F46)
                        )
                    }

                    TextButton(
                        onClick = { viewModel.toggleOfflineMode() },
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = if (isOffline) "Go Online" else "Simulasi Offline",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Search Bar & Store Title
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Cari rasa es krim atau SKU...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pos_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(ProductCategory.values()) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category.displayName, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CoralRose,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Product Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        onClick = {
                            if (product.stock > 0) {
                                showProductOptionDialog = product
                            }
                        }
                    )
                }
            }
        }

        // Floating Bottom Cart Bar
        AnimatedVisibility(
            visible = totalCartItems > 0,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCartSheet = true }
                    .testTag("floating_cart_bar"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CoralRose),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$totalCartItems Item di Keranjang",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = formatRupiah(totalCartAmount),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Checkout >",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CoralRose,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal Option: Choose Container (Cup vs Waffle Cone)
    showProductOptionDialog?.let { prod ->
        AlertDialog(
            onDismissRequest = { showProductOptionDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(prod.flavorEmoji, fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(prod.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(prod.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Pilih Wadah Penyajian:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.addToCart(prod, ContainerChoice.CUP)
                            showProductOptionDialog = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🥣 Cup Kertas Klasik")
                            Text(formatRupiah(prod.sellingPrice), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.addToCart(prod, ContainerChoice.WAFFLE_CONE)
                            showProductOptionDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmWaffle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🧇 Waffle Cone (+Rp 3.000)")
                            Text(formatRupiah(prod.sellingPrice + 3000.0), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showProductOptionDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Cart & Checkout BottomSheet
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CheckoutSheetContent(
                cart = cart,
                totalAmount = totalCartAmount,
                onUpdateQty = { item, delta -> viewModel.updateCartQuantity(item, delta) },
                onClearCart = {
                    viewModel.clearCart()
                    showCartSheet = false
                },
                onCompletePayment = { method, paid ->
                    val tx = viewModel.processCheckout(method, paid)
                    showCartSheet = false
                    completedTransaction = tx
                }
            )
        }
    }

    // Receipt Dialog
    completedTransaction?.let { tx ->
        ReceiptDialog(
            transaction = tx,
            onDismiss = { completedTransaction = null }
        )
    }
}

@Composable
fun ProductCard(
    product: IceCreamProduct,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = product.stock > 0, onClick = onClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top tag & Stock status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.sku,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )

                // Stock Badge
                val badgeColor = when {
                    product.isOutOfStock -> Color(0xFFEF4444)
                    product.isLowStock -> Color(0xFFF59E0B)
                    else -> MintMatcha
                }
                val badgeText = when {
                    product.isOutOfStock -> "Habis"
                    product.isLowStock -> "Sisa ${product.stock}"
                    else -> "${product.stock} ${product.unit}"
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Emoji / Image Avatar Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(Color(product.accentHex).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = product.flavorEmoji, fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatRupiah(product.sellingPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = CoralRose
                )

                Surface(
                    color = if (product.stock > 0) CoralRose else Color.Gray,
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutSheetContent(
    cart: List<CartItem>,
    totalAmount: Double,
    onUpdateQty: (CartItem, Int) -> Unit,
    onClearCart: () -> Unit,
    onCompletePayment: (PaymentMethod, Double) -> Unit
) {
    var selectedPayment by remember { mutableStateOf(PaymentMethod.CASH) }
    var paidAmountInput by remember { mutableStateOf(totalAmount.toInt().toString()) }

    val numericPaid = paidAmountInput.toDoubleOrNull() ?: 0.0
    val change = (numericPaid - totalAmount).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pesanan Pelanggan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClearCart) {
                Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cart items list
        cart.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(item.product.flavorEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(item.product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${item.container.label} • ${formatRupiah(item.product.sellingPrice + item.container.extraPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Stepper (+ / -)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onUpdateQty(item, -1) }
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text("-", fontWeight = FontWeight.Bold) }
                    }
                    Text(
                        "${item.quantity}",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = CircleShape,
                        color = CoralRose,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onUpdateQty(item, 1) }
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text("+", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total Tagihan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                formatRupiah(totalAmount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = CoralRose
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metode Pembayaran
        Text("Metode Pembayaran", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMethod.values().forEach { method ->
                val isSelected = selectedPayment == method
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) CoralRose else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedPayment = method
                            if (method != PaymentMethod.CASH) {
                                paidAmountInput = totalAmount.toInt().toString()
                            }
                        }
                ) {
                    Text(
                        text = method.label,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedPayment == PaymentMethod.CASH) {
            Text("Pecahan Cepat Uang Tunai", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(totalAmount.toInt(), 50000, 100000).forEach { denom ->
                    OutlinedButton(
                        onClick = { paidAmountInput = denom.toString() },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text(
                            text = if (denom == totalAmount.toInt()) "Uang Pas" else "Rp ${denom / 1000}k",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = paidAmountInput,
                onValueChange = { paidAmountInput = it.filter { ch -> ch.isDigit() } },
                label = { Text("Jumlah Uang Diterima (Rp)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kembalian:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    formatRupiah(change),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (numericPaid >= totalAmount) MintMatcha else Color.Red
                )
            }
        } else if (selectedPayment == PaymentMethod.QRIS) {
            // QRIS Simulation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("SCAN QRIS SCOOPCAST", fontWeight = FontWeight.Bold, color = CoralRose)
                Spacer(modifier = Modifier.height(8.dp))
                // Simulated QR Box
                Canvas(modifier = Modifier.size(140.dp)) {
                    val w = size.width
                    drawRect(color = Color.White)
                    drawRect(color = Color.Black, topLeft = Offset(10f, 10f), size = Size(35f, 35f))
                    drawRect(color = Color.Black, topLeft = Offset(w - 45f, 10f), size = Size(35f, 35f))
                    drawRect(color = Color.Black, topLeft = Offset(10f, w - 45f), size = Size(35f, 35f))
                    // Central patterns
                    for (i in 2..8) {
                        for (j in 2..8) {
                            if ((i + j) % 2 == 0) {
                                drawRect(color = Color.Black, topLeft = Offset(i * 14f, j * 14f), size = Size(10f, 10f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("NMID: ID10293847291 • Standar Bank Indonesia", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bayar Button
        Button(
            onClick = {
                onCompletePayment(selectedPayment, if (selectedPayment == PaymentMethod.CASH) numericPaid else totalAmount)
            },
            enabled = selectedPayment != PaymentMethod.CASH || numericPaid >= totalAmount,
            colors = ButtonDefaults.buttonColors(containerColor = CoralRose),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_process_payment")
        ) {
            Text("Selesaikan Transaksi (${formatRupiah(totalAmount)})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ReceiptDialog(
    transaction: TransactionRecord,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MintMatcha,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Transaksi Berhasil!", fontWeight = FontWeight.Bold, color = MintMatcha)
                Text(
                    text = transaction.invoiceNo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        text = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "🍨 SCOOPCAST ICE CREAM POS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Waktu: ${transaction.timestamp}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Status: ${transaction.syncStatus.label}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = if (transaction.syncStatus.name.contains("PENDING")) WarmWaffle else MintMatcha,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("-".repeat(32), fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))

                    transaction.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${item.quantity}x ${item.product.name.take(16)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Text(
                                formatRupiah(item.itemPrice),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("-".repeat(32), fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Tagihan:", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(formatRupiah(transaction.totalAmount), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Metode (${transaction.paymentMethod.label}):", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        Text(formatRupiah(transaction.paidAmount), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kembalian:", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        Text(formatRupiah(transaction.changeAmount), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ Stok bahan otomatis terpotong",
                        style = MaterialTheme.typography.labelSmall,
                        color = MintMatcha,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CoralRose)
            ) {
                Text("Selesai")
            }
        }
    )
}
