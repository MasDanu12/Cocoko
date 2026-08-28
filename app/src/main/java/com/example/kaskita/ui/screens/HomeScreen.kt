package com.example.kaskita.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaskita.data.model.DashboardSummary
import com.example.kaskita.data.model.TransactionDetail
import com.example.kaskita.ui.components.EmptyState
import com.example.kaskita.ui.components.StatBox
import com.example.kaskita.ui.components.StatusBadge
import com.example.kaskita.ui.components.formatRupiah
import com.example.kaskita.ui.theme.ExpenseRed
import com.example.kaskita.ui.theme.IncomeGreen
import com.example.kaskita.ui.theme.PrimaryGreen
import com.example.kaskita.ui.theme.PrimaryGreenLight
import com.example.kaskita.ui.theme.SecondaryGold
import kotlin.math.abs

@Composable
fun HomeScreen(
    summary: DashboardSummary?,
    onNavigateToTab: (String) -> Unit,
    onOpenReceipt: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Hero Gradient Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_balance_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryGreen, Color(0xFF17694F))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Saldo Kas Saat Ini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatRupiah(summary?.totalSaldo ?: 0.0),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("home_total_balance_text")
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Pemasukan",
                                        fontSize = 11.5.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formatRupiah(summary?.pemasukanBulanIni ?: 0.0),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Pengeluaran",
                                        fontSize = 11.5.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formatRupiah(summary?.pengeluaranBulanIni ?: 0.0),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Monthly Dues Overview Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_dues_summary_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Iuran Bulan Ini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${summary?.duesSummary?.totalAnggota ?: 0} anggota aktif",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox(
                            label = "Lunas",
                            value = (summary?.duesSummary?.lunasCount ?: 0).toString(),
                            valueColor = IncomeGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "Sebagian",
                            value = (summary?.duesSummary?.sebagianCount ?: 0).toString(),
                            valueColor = SecondaryGold,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "Belum Bayar",
                            value = (summary?.duesSummary?.belumBayarCount ?: 0).toString(),
                            valueColor = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    StatBox(
                        label = "Total Tunggakan",
                        value = formatRupiah(summary?.duesSummary?.tunggakan ?: 0.0),
                        valueColor = ExpenseRed,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { onNavigateToTab("iuran") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_view_dues_button")
                    ) {
                        Text("Lihat Iuran", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Recent Activities Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aktivitas Terbaru",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { onNavigateToTab("kas") },
                            modifier = Modifier.testTag("home_view_all_transactions_button")
                        ) {
                            Text("Lihat Semua", color = PrimaryGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    val recent = summary?.recentTransactions ?: emptyList()
                    if (recent.isEmpty()) {
                        EmptyState(text = "Belum ada transaksi")
                    } else {
                        recent.forEachIndexed { index, item ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    thickness = 0.8.dp
                                )
                            }
                            HomeTransactionRow(
                                item = item,
                                onOpenReceipt = { onOpenReceipt(item.transaction.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTransactionRow(
    item: TransactionDetail,
    onOpenReceipt: () -> Unit
) {
    val t = item.transaction
    val isMasuk = t.tipe == "masuk"
    val isKeluar = t.tipe == "keluar"
    val isTransfer = t.tipe == "transfer"
    val isPenyesuaian = t.tipe == "penyesuaian"

    val title = t.kategori ?: when {
        isMasuk -> "Pemasukan"
        isKeluar -> "Pengeluaran"
        isTransfer -> "Transfer"
        else -> "Penyesuaian"
    }

    val amountPrefix = when {
        isMasuk -> "+"
        isKeluar -> "-"
        isPenyesuaian && t.jumlah < 0 -> "-"
        isPenyesuaian -> "+"
        else -> ""
    }

    val amountColor = when {
        isMasuk -> IncomeGreen
        isKeluar -> ExpenseRed
        isPenyesuaian && t.jumlah < 0 -> ExpenseRed
        isPenyesuaian -> IncomeGreen
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (t.sumber == "iuran") {
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge(status = "iuran")
                }
            }
            Spacer(modifier = Modifier.height(2.dp))

            var subtitle = t.tanggal
            if (item.anggotaNama != null) subtitle += " · ${item.anggotaNama}"
            if (isTransfer) subtitle += " · ${item.akunNama ?: "-"} → ${item.akunTujuanNama ?: "-"}"
            if (!t.catatan.isNullOrBlank()) subtitle += " · ${t.catatan}"

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$amountPrefix${formatRupiah(abs(t.jumlah))}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = amountColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onOpenReceipt() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "Struk",
                    color = PrimaryGreen,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
