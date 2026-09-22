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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.IceCreamProduct
import com.example.model.MutationType
import com.example.model.StockMutation
import com.example.ui.theme.CoralRose
import com.example.ui.theme.MintMatcha
import com.example.ui.theme.WarmWaffle
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val products by viewModel.products.collectAsStateWithLifecycle()
    val mutations by viewModel.mutations.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var stockFilter by remember { mutableStateOf("ALL") } // ALL, LOW, OUT

    var editingProduct by remember { mutableStateOf<IceCreamProduct?>(null) }
    var adjustingStockProduct by remember { mutableStateOf<IceCreamProduct?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Tab: Stok Produk vs Riwayat Mutasi
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Katalog & Stok", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Inventory, contentDescription = null) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Riwayat Mutasi", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.History, contentDescription = null) }
            )
        }

        if (selectedTabIndex == 0) {
            // Tab 1: Manajemen Stok & Harga
            val filteredProducts = remember(products, searchQuery, stockFilter) {
                products.filter { prod ->
                    val matchesSearch = prod.name.contains(searchQuery, ignoreCase = true) || prod.sku.contains(searchQuery, ignoreCase = true)
                    val matchesStock = when (stockFilter) {
                        "LOW" -> prod.isLowStock && !prod.isOutOfStock
                        "OUT" -> prod.isOutOfStock
                        else -> true
                    }
                    matchesSearch && matchesStock
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari produk atau SKU...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stock Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = stockFilter == "ALL",
                        onClick = { stockFilter = "ALL" },
                        label = { Text("Semua (${products.size})") }
                    )
                    FilterChip(
                        selected = stockFilter == "LOW",
                        onClick = { stockFilter = "LOW" },
                        label = { Text("Menipis (${products.count { it.isLowStock && !it.isOutOfStock }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarmWaffle,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = stockFilter == "OUT",
                        onClick = { stockFilter = "OUT" },
                        label = { Text("Habis (${products.count { it.isOutOfStock }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444),
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { prod ->
                        InventoryProductCard(
                            product = prod,
                            onAdjustStock = { adjustingStockProduct = prod },
                            onEditPrice = { editingProduct = prod }
                        )
                    }
                }
            }
        } else {
            // Tab 2: Riwayat Mutasi Barang Masuk/Keluar
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(mutations) { mut ->
                    MutationHistoryCard(mutation = mut)
                }
            }
        }
    }

    // Dialog Edit Harga & Stok Minimum
    editingProduct?.let { prod ->
        EditProductDialog(
            product = prod,
            onDismiss = { editingProduct = null },
            onSave = { cost, selling, minStock ->
                viewModel.updateProductPrices(prod.id, cost, selling, minStock)
                editingProduct = null
            }
        )
    }

    // Dialog Tambah/Koreksi Stok (Mutasi)
    adjustingStockProduct?.let { prod ->
        StockAdjustmentDialog(
            product = prod,
            onDismiss = { adjustingStockProduct = null },
            onConfirm = { type, qty, notes ->
                viewModel.addStockAdjustment(prod.id, type, qty, notes)
                adjustingStockProduct = null
            }
        )
    }
}

@Composable
fun InventoryProductCard(
    product: IceCreamProduct,
    onAdjustStock: () -> Unit,
    onEditPrice: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.flavorEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${product.sku} • ${product.category.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(onClick = onEditPrice) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Harga", tint = CoralRose)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Harga Modal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(formatRupiah(product.costPrice), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Harga Jual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(formatRupiah(product.sellingPrice), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = CoralRose)
                }
                Column {
                    Text("Margin Laba", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    val margin = product.sellingPrice - product.costPrice
                    Text(formatRupiah(margin), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MintMatcha)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock Bar & Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Stok: ${product.stock} ${product.unit}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (product.isLowStock) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(Min. ${product.minimumStock})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val ratio = (product.stock.toFloat() / (product.minimumStock * 2).coerceAtLeast(1)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(6.dp),
                        color = when {
                            product.isOutOfStock -> Color(0xFFEF4444)
                            product.isLowStock -> WarmWaffle
                            else -> MintMatcha
                        },
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                }

                Button(
                    onClick = onAdjustStock,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRose),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mutasi", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MutationHistoryCard(mutation: StockMutation) {
    val isPositive = mutation.type.isPositive
    val accentColor = when (mutation.type) {
        MutationType.IN -> MintMatcha
        MutationType.SALE -> CoralRose
        MutationType.ADJUSTMENT -> WarmWaffle
        MutationType.OUT, MutationType.WASTE -> Color(0xFFEF4444)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(mutation.productName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${mutation.type.label} • ${mutation.referenceId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        mutation.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Text(
                        mutation.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isPositive) "+" else "-") + "${mutation.quantity}",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor
                )
                Text(
                    text = "Sisa: ${mutation.stockAfter}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EditProductDialog(
    product: IceCreamProduct,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Int) -> Unit
) {
    var costPriceText by remember { mutableStateOf(product.costPrice.toInt().toString()) }
    var sellingPriceText by remember { mutableStateOf(product.sellingPrice.toInt().toString()) }
    var minStockText by remember { mutableStateOf(product.minimumStock.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Harga & Minimum Stok", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(product.name, style = MaterialTheme.typography.bodyMedium, color = CoralRose, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = costPriceText,
                    onValueChange = { costPriceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Modal / Beli (Rp)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = sellingPriceText,
                    onValueChange = { sellingPriceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Jual Kasir (Rp)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = minStockText,
                    onValueChange = { minStockText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Batas Minimum Stok (Alert)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = costPriceText.toDoubleOrNull() ?: product.costPrice
                    val sell = sellingPriceText.toDoubleOrNull() ?: product.sellingPrice
                    val min = minStockText.toIntOrNull() ?: product.minimumStock
                    onSave(cost, sell, min)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralRose)
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun StockAdjustmentDialog(
    product: IceCreamProduct,
    onDismiss: () -> Unit,
    onConfirm: (MutationType, Int, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(MutationType.IN) }
    var quantityText by remember { mutableStateOf("10") }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mutasi Stok Produk", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(product.name, fontWeight = FontWeight.Bold, color = CoralRose)
                Text("Stok saat ini: ${product.stock} ${product.unit}", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Tipe Mutasi:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(MutationType.IN, MutationType.OUT, MutationType.ADJUSTMENT, MutationType.WASTE).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah (${product.unit})") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Catatan / No. Dokumen") },
                    placeholder = { Text("Misal: Kiriman pabrik, kadaluarsa, dll") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toIntOrNull() ?: 1
                    onConfirm(selectedType, qty, notesText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralRose)
            ) {
                Text("Konfirmasi Mutasi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
