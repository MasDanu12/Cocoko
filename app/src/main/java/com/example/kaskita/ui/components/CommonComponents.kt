package com.example.kaskita.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaskita.data.repository.KasKitaRepository
import com.example.kaskita.ui.theme.ExpenseRed
import com.example.kaskita.ui.theme.ExpenseRedLight
import com.example.kaskita.ui.theme.IncomeGreen
import com.example.kaskita.ui.theme.IncomeGreenLight
import com.example.kaskita.ui.theme.PrimaryGreen
import com.example.kaskita.ui.theme.PrimaryGreenLight
import com.example.kaskita.ui.theme.SecondaryGold
import com.example.kaskita.ui.theme.SecondaryGoldLight
import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount).replace("Rp", "Rp").trim()
}

fun formatPeriodeLabel(periode: String): String {
    val parts = periode.split("-")
    if (parts.size != 2) return periode
    val months = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    val m = parts[1].toIntOrNull() ?: return periode
    if (m in 1..12) {
        return "${months[m - 1]} ${parts[0]}"
    }
    return periode
}

@Composable
fun AppHeader(
    orgName: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = orgName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "lunas" -> Triple(IncomeGreenLight, IncomeGreen, "Lunas")
        "sebagian" -> Triple(SecondaryGoldLight, SecondaryGold, "Sebagian")
        "belum_bayar", "menunggak" -> Triple(ExpenseRedLight, ExpenseRed, "Belum Bayar")
        "tidak_dikenakan" -> Triple(Color(0xFFE8ECEB), Color(0xFF6B7A75), "Tidak Dikenakan")
        "nonaktif" -> Triple(Color(0xFFE8ECEB), Color(0xFF6B7A75), "Nonaktif")
        "iuran" -> Triple(SecondaryGoldLight, SecondaryGold, "Iuran")
        else -> Triple(PrimaryGreenLight, PrimaryGreen, status)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun StatBox(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PeriodNavigator(
    currentPeriod: String,
    onPeriodChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { onPeriodChanged(KasKitaRepository.periodAdd(currentPeriod, -1)) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Bulan Sebelumnya",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatPeriodeLabel(currentPeriod),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { onPeriodChanged(KasKitaRepository.periodAdd(currentPeriod, 1)) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Bulan Berikutnya",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun YearNavigator(
    currentYear: String,
    onYearChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val yearInt = currentYear.toIntOrNull() ?: 2026
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { onYearChanged((yearInt - 1).toString()) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Tahun Sebelumnya",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Tahun $currentYear",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { onYearChanged((yearInt + 1).toString()) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Tahun Berikutnya",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    text: String,
    icon: ImageVector = Icons.Default.Info,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
