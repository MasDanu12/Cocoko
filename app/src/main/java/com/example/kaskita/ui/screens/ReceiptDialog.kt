package com.example.kaskita.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaskita.data.model.ReceiptData
import com.example.kaskita.ui.components.formatPeriodeLabel
import com.example.kaskita.ui.components.formatRupiah
import com.example.kaskita.ui.theme.ExpenseRed
import com.example.kaskita.ui.theme.IncomeGreen
import com.example.kaskita.ui.theme.PrimaryGreen
import com.example.kaskita.ui.theme.PrimaryGreenLight
import kotlin.math.abs

@Composable
fun ReceiptDialog(
    data: ReceiptData?,
    onDismiss: () -> Unit
) {
    if (data == null) return
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(PrimaryGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "BUKTI TRANSAKSI KAS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Text(
                        text = data.organisasiNama,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = data.tanggal,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Key-Values
                    ReceiptRow(label = "Tipe", value = data.tipe.replaceFirstChar { it.uppercase() })
                    if (data.kategori != null) {
                        ReceiptRow(label = "Kategori", value = data.kategori)
                    }
                    if (data.anggotaNama != null) {
                        ReceiptRow(label = "Anggota", value = data.anggotaNama)
                    }
                    if (data.periodeList.isNotEmpty()) {
                        val periodLabels = data.periodeList.joinToString(", ") { formatPeriodeLabel(it) }
                        ReceiptRow(label = "Periode Iuran", value = periodLabels)
                    }
                    if (data.akunNama != null) {
                        ReceiptRow(label = "Akun Kas", value = data.akunNama)
                    }
                    if (data.metode != null) {
                        ReceiptRow(label = "Metode", value = data.metode)
                    }
                    if (!data.catatan.isNullOrBlank()) {
                        ReceiptRow(label = "Catatan", value = data.catatan)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount Total
                    val amountColor = if (data.tipe == "keluar") ExpenseRed else IncomeGreen
                    Text(
                        text = "TOTAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatRupiah(abs(data.jumlah)),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ID: ${data.id.take(8).uppercase()}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    shareReceiptText(context, data)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                modifier = Modifier.testTag("share_receipt_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bagikan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

private fun shareReceiptText(context: Context, data: ReceiptData) {
    val sb = StringBuilder()
    sb.append("🧾 BUKTI TRANSAKSI KAS\n")
    sb.append("========================\n")
    sb.append("Organisasi : ${data.organisasiNama}\n")
    sb.append("Tanggal    : ${data.tanggal}\n")
    sb.append("Tipe       : ${data.tipe.replaceFirstChar { it.uppercase() }}\n")
    if (data.kategori != null) sb.append("Kategori   : ${data.kategori}\n")
    if (data.anggotaNama != null) sb.append("Anggota    : ${data.anggotaNama}\n")
    if (data.periodeList.isNotEmpty()) {
        sb.append("Periode    : ${data.periodeList.joinToString(", ") { formatPeriodeLabel(it) }}\n")
    }
    if (data.akunNama != null) sb.append("Akun Kas   : ${data.akunNama}\n")
    if (data.metode != null) sb.append("Metode     : ${data.metode}\n")
    if (!data.catatan.isNullOrBlank()) sb.append("Catatan    : ${data.catatan}\n")
    sb.append("------------------------\n")
    sb.append("TOTAL      : ${formatRupiah(abs(data.jumlah))}\n")
    sb.append("ID Trx     : ${data.id.take(8).uppercase()}\n")
    sb.append("========================\n")
    sb.append("Terima kasih atas partisipasi Anda.")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan Bukti Transaksi")
    context.startActivity(shareIntent)
}
