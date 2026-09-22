package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.model.SyncStatus
import com.example.ui.theme.CoralRose
import com.example.ui.theme.MintMatcha
import com.example.ui.theme.WarmWaffle
import com.example.viewmodel.MainViewModel

@Composable
fun SecuritySyncScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val jwtState by viewModel.jwtTokenState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    val pendingTransactions = transactions.filter { it.syncStatus == SyncStatus.PENDING_OFFLINE }
    var syncSuccessMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Keamanan & Sinkronisasi",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Arsitektur JWT, Hardware Keystore AES-256, & Offline Sync",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Connection Mode Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOffline) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = if (isOffline) Color(0xFFB45309) else Color(0xFF047857),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isOffline) "Mode Jaringan: OFFLINE" else "Mode Jaringan: ONLINE (Connected)",
                            fontWeight = FontWeight.Bold,
                            color = if (isOffline) Color(0xFF92400E) else Color(0xFF065F46)
                        )
                        Text(
                            text = if (isOffline) "Transaksi disimpan lokal dengan enkripsi AES-256" else "Koneksi REST API Laravel aktif & stabil",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOffline) Color(0xFF78350F) else Color(0xFF047857),
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = { viewModel.toggleOfflineMode() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOffline) WarmWaffle else MintMatcha
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isOffline) "Go Online" else "Simulasi Putus")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pending Offline Queue Card
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = CoralRose)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Antrean Transaksi Offline", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Surface(
                        color = if (pendingTransactions.isNotEmpty()) WarmWaffle.copy(alpha = 0.2f) else MintMatcha.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${pendingTransactions.size} Tertunda",
                            color = if (pendingTransactions.isNotEmpty()) WarmWaffle else MintMatcha,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Saat offline, transaksi kasir diberi UUID lokal dan disimpan di database lokal terenkripsi. Begitu online kembali, tombol sinkronisasi akan memicu batch upload ke backend Laravel.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (pendingTransactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    pendingTransactions.forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(tx.invoiceNo, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("${tx.items.size} item • ${tx.paymentMethod.label}", fontSize = 11.sp)
                            }
                            Text(formatRupiah(tx.totalAmount), fontWeight = FontWeight.Bold, color = CoralRose, fontSize = 12.sp)
                        }
                    }
                }

                syncSuccessMsg?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(msg, color = MintMatcha, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val count = viewModel.syncPendingOfflineTransactions()
                        syncSuccessMsg = if (count > 0) "✓ Sukses sinkronisasi $count transaksi ke Laravel API!" else "Semua transaksi sudah tersinkronisasi."
                    },
                    enabled = !isOffline && pendingTransactions.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRose),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sinkronkan Transaksi Offline Sekarang", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // JWT Token & Keystore Security Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MintMatcha)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manajemen Kunci & Sesi JWT", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hardware Keystore Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MintMatcha)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Hardware Security Keystore", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Text(jwtState.keystoreAlgorithm, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("✓ Enkripsi data transaksi lokal offline aktif", style = MaterialTheme.typography.labelSmall, color = MintMatcha, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Access Token Preview
                Text("Access Token (Bearer)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Text(
                    text = jwtState.accessToken.take(38) + "...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Refresh Token Preview
                Text("Refresh Token (Tersimpan di Secure Storage)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Text(
                    text = jwtState.refreshToken.take(38) + "...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.refreshJwtToken() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simulasikan Token Rotation (Auto-Refresh 401)")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Technical Architecture Reference Card for TA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = WarmWaffle)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Alur Otentikasi & Keamanan (Untuk Laporan TA)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "1. Login Kasir:\n" +
                            "   Mobile mengirim kredensial -> Server merespon sepasang token (Access Token exp: 15m, Refresh Token exp: 30d).\n\n" +
                            "2. Penyimpanan Aman:\n" +
                            "   Token disimpan di Android Keystore / iOS Keychain via EncryptedSharedPreferences.\n\n" +
                            "3. Auto-Refresh Interceptor:\n" +
                            "   Saat Access Token kadaluarsa (HTTP 401), HTTP Interceptor memicu request refresh otomatis di latar belakang tanpa membuat kasir logout.\n\n" +
                            "4. Enkripsi Transaksi Lokal:\n" +
                            "   Data penjualan saat jaringan terputus dienkripsi secara lokal sebelum disimpan, mencegah data tampering.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
