package com.example.kaskita.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaskita.data.model.AnnualReport
import com.example.kaskita.data.model.CategoryBreakdown
import com.example.kaskita.data.model.MonthTrend
import com.example.kaskita.data.model.MonthlyReport
import com.example.kaskita.ui.components.EmptyState
import com.example.kaskita.ui.components.PeriodNavigator
import com.example.kaskita.ui.components.StatBox
import com.example.kaskita.ui.components.YearNavigator
import com.example.kaskita.ui.components.formatPeriodeLabel
import com.example.kaskita.ui.components.formatRupiah
import com.example.kaskita.ui.theme.ExpenseRed
import com.example.kaskita.ui.theme.IncomeGreen
import com.example.kaskita.ui.theme.PrimaryGreen
import com.example.kaskita.ui.theme.PrimaryGreenLight
import com.example.kaskita.ui.theme.SecondaryGold

private val CHART_COLORS = listOf(
    Color(0xFF0F4C3A),
    Color(0xFFC0392B),
    Color(0xFFC9932F),
    Color(0xFF2980B9),
    Color(0xFF8E44AD),
    Color(0xFF16A085),
    Color(0xFFD35400),
    Color(0xFF2C3E50),
    Color(0xFF7F8C8D),
    Color(0xFF27AE60)
)

@Composable
fun ReportsScreen(
    orgName: String,
    reportTab: String, // "bulanan" or "tahunan"
    selectedMonth: String,
    selectedYear: String,
    monthlyReport: MonthlyReport?,
    annualReport: AnnualReport?,
    onTabSelected: (String) -> Unit,
    onMonthChanged: (String) -> Unit,
    onYearChanged: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Selector
        TabRow(
            selectedTabIndex = if (reportTab == "bulanan") 0 else 1,
            containerColor = PrimaryGreenLight,
            contentColor = PrimaryGreen,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = reportTab == "bulanan",
                onClick = { onTabSelected("bulanan") },
                text = { Text("Laporan Bulanan", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = reportTab == "tahunan",
                onClick = { onTabSelected("tahunan") },
                text = { Text("Laporan Tahunan", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (reportTab == "bulanan") {
            MonthlyReportView(
                orgName = orgName,
                selectedMonth = selectedMonth,
                report = monthlyReport,
                onMonthChanged = onMonthChanged,
                onShare = { text -> shareReportText(context, text) }
            )
        } else {
            AnnualReportView(
                orgName = orgName,
                selectedYear = selectedYear,
                report = annualReport,
                onYearChanged = onYearChanged,
                onShare = { text -> shareReportText(context, text) }
            )
        }
    }
}

@Composable
fun MonthlyReportView(
    orgName: String,
    selectedMonth: String,
    report: MonthlyReport?,
    onMonthChanged: (String) -> Unit,
    onShare: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            PeriodNavigator(
                currentPeriod = selectedMonth,
                onPeriodChanged = onMonthChanged,
                modifier = Modifier.testTag("report_period_navigator")
            )
        }

        // Summary Metric Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ringkasan Keuangan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox(
                            label = "Pemasukan",
                            value = formatRupiah(report?.totalMasuk ?: 0.0),
                            valueColor = IncomeGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "Pengeluaran",
                            value = formatRupiah(report?.totalKeluar ?: 0.0),
                            valueColor = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val netColor = if ((report?.saldoBersih ?: 0.0) >= 0) IncomeGreen else ExpenseRed
                        StatBox(
                            label = "Saldo Bersih",
                            value = formatRupiah(report?.saldoBersih ?: 0.0),
                            valueColor = netColor,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "Transaksi",
                            value = "${report?.jumlahTransaksi ?: 0}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Expense Category Chart Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Komposisi Pengeluaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val breakdowns = report?.categoryBreakdowns ?: emptyList()
                    if (breakdowns.isEmpty()) {
                        EmptyState(text = "Tidak ada pengeluaran pada bulan ini")
                    } else {
                        // Donut Chart Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChart(breakdowns = breakdowns)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category Legend Table
                        breakdowns.forEachIndexed { idx, cat ->
                            val color = CHART_COLORS[idx % CHART_COLORS.size]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cat.categoryName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "${formatRupiah(cat.amount)} (${(cat.percentage * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Share Button
        item {
            Button(
                onClick = {
                    val shareText = buildMonthlyReportText(orgName, selectedMonth, report)
                    onShare(shareText)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("share_monthly_report_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bagikan Laporan Bulanan (Teks)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DonutChart(breakdowns: List<CategoryBreakdown>) {
    Canvas(modifier = Modifier.size(140.dp)) {
        val strokeWidth = 28.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        val arcSize = Size(diameter, diameter)

        var startAngle = -90f
        breakdowns.forEachIndexed { index, item ->
            val sweep = item.percentage * 360f
            val color = CHART_COLORS[index % CHART_COLORS.size]
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweep
        }
    }
}

@Composable
fun AnnualReportView(
    orgName: String,
    selectedYear: String,
    report: AnnualReport?,
    onYearChanged: (String) -> Unit,
    onShare: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            YearNavigator(
                currentYear = selectedYear,
                onYearChanged = onYearChanged,
                modifier = Modifier.testTag("report_year_navigator")
            )
        }

        // Annual Summary Metric Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ringkasan Tahunan $selectedYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox(
                            label = "Total Masuk",
                            value = formatRupiah(report?.totalMasuk ?: 0.0),
                            valueColor = IncomeGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "Total Keluar",
                            value = formatRupiah(report?.totalKeluar ?: 0.0),
                            valueColor = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val netColor = if ((report?.saldoBersih ?: 0.0) >= 0) IncomeGreen else ExpenseRed
                        StatBox(
                            label = "Saldo Bersih",
                            value = formatRupiah(report?.saldoBersih ?: 0.0),
                            valueColor = netColor,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "Total Transaksi",
                            value = "${report?.jumlahTransaksi ?: 0}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 12 Months Bar Chart Card
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
                            text = "Grafik Tren Bulanan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Masuk", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Keluar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val trends = report?.monthlyTrends ?: emptyList()
                    val maxVal = trends.maxOfOrNull { maxOf(it.masuk, it.keluar) } ?: 1.0
                    val safeMax = if (maxVal > 0) maxVal else 1.0

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        MonthlyBarChart(trends = trends, maxVal = safeMax)
                    }
                }
            }
        }

        // Monthly Breakdown Table Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rincian Tiap Bulan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val trends = report?.monthlyTrends ?: emptyList()
                    trends.forEachIndexed { index, m ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                thickness = 0.8.dp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = m.monthLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(50.dp)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Row {
                                    Text(
                                        text = "+${formatRupiah(m.masuk)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = IncomeGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "-${formatRupiah(m.keluar)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ExpenseRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                val net = m.masuk - m.keluar
                                val netCol = if (net >= 0) IncomeGreen else ExpenseRed
                                Text(
                                    text = "Bersih: ${formatRupiah(net)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = netCol
                                )
                            }
                        }
                    }
                }
            }
        }

        // Share Button
        item {
            Button(
                onClick = {
                    val shareText = buildAnnualReportText(orgName, selectedYear, report)
                    onShare(shareText)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("share_annual_report_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bagikan Laporan Tahunan (Teks)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MonthlyBarChart(trends: List<MonthTrend>, maxVal: Double) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val count = trends.size
        if (count == 0) return@Canvas
        val paddingHorizontal = 12.dp.toPx()
        val availableWidth = size.width - (paddingHorizontal * 2)
        val barGroupWidth = availableWidth / count
        val singleBarWidth = (barGroupWidth * 0.35f).coerceAtMost(10.dp.toPx())
        val chartHeight = size.height - 30.dp.toPx()

        // Draw baseline
        drawLine(
            color = Color(0xFFD0D7D4),
            start = Offset(paddingHorizontal, chartHeight),
            end = Offset(size.width - paddingHorizontal, chartHeight),
            strokeWidth = 1.5f
        )

        trends.forEachIndexed { i, item ->
            val centerX = paddingHorizontal + (i * barGroupWidth) + (barGroupWidth / 2)

            // Masuk bar (green)
            val masukHeight = ((item.masuk / maxVal) * (chartHeight - 10f)).toFloat()
            val masukTop = chartHeight - masukHeight
            drawRoundRect(
                color = IncomeGreen,
                topLeft = Offset(centerX - singleBarWidth - 2f, masukTop),
                size = Size(singleBarWidth, masukHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // Keluar bar (red)
            val keluarHeight = ((item.keluar / maxVal) * (chartHeight - 10f)).toFloat()
            val keluarTop = chartHeight - keluarHeight
            drawRoundRect(
                color = ExpenseRed,
                topLeft = Offset(centerX + 2f, keluarTop),
                size = Size(singleBarWidth, keluarHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }
}

private fun shareReportText(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan Laporan Kas")
    context.startActivity(shareIntent)
}

private fun buildMonthlyReportText(orgName: String, month: String, report: MonthlyReport?): String {
    val sb = StringBuilder()
    sb.append("📊 LAPORAN KAS BULANAN\n")
    sb.append("Organisasi: $orgName\n")
    sb.append("Periode: ${formatPeriodeLabel(month)}\n")
    sb.append("========================\n\n")

    sb.append("💰 RINGKASAN:\n")
    sb.append("• Pemasukan: ${formatRupiah(report?.totalMasuk ?: 0.0)}\n")
    sb.append("• Pengeluaran: ${formatRupiah(report?.totalKeluar ?: 0.0)}\n")
    sb.append("• Saldo Bersih: ${formatRupiah(report?.saldoBersih ?: 0.0)}\n")
    sb.append("• Total Transaksi: ${report?.jumlahTransaksi ?: 0}\n\n")

    val breakdowns = report?.categoryBreakdowns ?: emptyList()
    if (breakdowns.isNotEmpty()) {
        sb.append("📉 PENGELUARAN PER KATEGORI:\n")
        breakdowns.forEach { cat ->
            sb.append("• ${cat.categoryName}: ${formatRupiah(cat.amount)} (${(cat.percentage * 100).toInt()}%)\n")
        }
        sb.append("\n")
    }

    val dues = report?.duesSummary
    if (dues != null && dues.totalAnggota > 0) {
        sb.append("📋 STATUS IURAN:\n")
        sb.append("• Lunas: ${dues.lunasCount} orang\n")
        sb.append("• Sebagian: ${dues.sebagianCount} orang\n")
        sb.append("• Belum Bayar: ${dues.belumBayarCount} orang\n")
        sb.append("• Terkumpul: ${formatRupiah(dues.terkumpul)}\n")
        sb.append("• Tunggakan: ${formatRupiah(dues.tunggakan)}\n\n")
    }

    sb.append("Dibuat otomatis oleh Aplikasi Kas Kita.")
    return sb.toString()
}

private fun buildAnnualReportText(orgName: String, year: String, report: AnnualReport?): String {
    val sb = StringBuilder()
    sb.append("📊 LAPORAN KAS TAHUNAN\n")
    sb.append("Organisasi: $orgName\n")
    sb.append("Tahun: $year\n")
    sb.append("========================\n\n")

    sb.append("💰 RINGKASAN TAHUNAN:\n")
    sb.append("• Total Pemasukan: ${formatRupiah(report?.totalMasuk ?: 0.0)}\n")
    sb.append("• Total Pengeluaran: ${formatRupiah(report?.totalKeluar ?: 0.0)}\n")
    sb.append("• Saldo Bersih: ${formatRupiah(report?.saldoBersih ?: 0.0)}\n")
    sb.append("• Total Transaksi: ${report?.jumlahTransaksi ?: 0}\n\n")

    val trends = report?.monthlyTrends ?: emptyList()
    if (trends.isNotEmpty()) {
        sb.append("📅 RINCIAN BULANAN:\n")
        trends.forEach { m ->
            sb.append("• ${m.monthLabel}: Masuk ${formatRupiah(m.masuk)} | Keluar ${formatRupiah(m.keluar)} | Bersih ${formatRupiah(m.masuk - m.keluar)}\n")
        }
        sb.append("\n")
    }

    sb.append("Dibuat otomatis oleh Aplikasi Kas Kita.")
    return sb.toString()
}
