package com.example.kaskita.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaskita.data.model.AccountWithBalance
import com.example.kaskita.data.model.DuesSettings
import com.example.kaskita.data.model.Member
import com.example.kaskita.data.model.MemberArrears
import com.example.kaskita.data.model.MemberDuesStatus
import com.example.kaskita.data.model.MonthlyDuesSummary
import com.example.kaskita.data.repository.KasKitaRepository
import com.example.kaskita.ui.components.EmptyState
import com.example.kaskita.ui.components.PeriodNavigator
import com.example.kaskita.ui.components.StatBox
import com.example.kaskita.ui.components.StatusBadge
import com.example.kaskita.ui.components.formatPeriodeLabel
import com.example.kaskita.ui.components.formatRupiah
import com.example.kaskita.ui.theme.ExpenseRed
import com.example.kaskita.ui.theme.IncomeGreen
import com.example.kaskita.ui.theme.PrimaryGreen
import com.example.kaskita.ui.theme.PrimaryGreenLight
import com.example.kaskita.ui.theme.SecondaryGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuesScreen(
    duesSettings: DuesSettings?,
    duesSummary: MonthlyDuesSummary?,
    currentPeriod: String,
    members: List<Member>,
    accounts: List<AccountWithBalance>,
    arrearsList: List<MemberArrears>,
    onPeriodChanged: (String) -> Unit,
    onPayDues: (memberId: String, amount: Double, date: String?, notes: String?, accountId: String?, onSuccess: () -> Unit) -> Unit,
    onUpdateSettings: (name: String, nominal: Double, startDate: String, onSuccess: () -> Unit) -> Unit,
    onLoadArrears: () -> Unit
) {
    var showPayModal by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }
    var showArrearsModal by remember { mutableStateOf(false) }
    var preselectedMemberId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Dues Settings Banner
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreenLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = duesSettings?.namaIuran ?: "Iuran Bulanan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${formatRupiah(duesSettings?.nominal ?: 0.0)} / bln · Mulai ${duesSettings?.tanggalMulai ?: "-"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryGreen.copy(alpha = 0.85f)
                        )
                    }
                    IconButton(
                        onClick = { showSettingsModal = true },
                        modifier = Modifier.testTag("dues_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan Iuran",
                            tint = PrimaryGreen
                        )
                    }
                }
            }
        }

        // Pay Dues CTA Button
        item {
            Button(
                onClick = {
                    preselectedMemberId = null
                    showPayModal = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pay_dues_button")
            ) {
                Icon(Icons.Default.Payment, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Catat Pembayaran Iuran", fontWeight = FontWeight.Bold)
            }
        }

        // Period Navigator
        item {
            PeriodNavigator(
                currentPeriod = currentPeriod,
                onPeriodChanged = onPeriodChanged,
                modifier = Modifier.testTag("dues_period_navigator")
            )
        }

        // Period Stats Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ringkasan Iuran (${formatPeriodeLabel(currentPeriod)})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox(
                            label = "Lunas",
                            value = (duesSummary?.lunasCount ?: 0).toString(),
                            valueColor = IncomeGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "Sebagian",
                            value = (duesSummary?.sebagianCount ?: 0).toString(),
                            valueColor = SecondaryGold,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "Belum Bayar",
                            value = (duesSummary?.belumBayarCount ?: 0).toString(),
                            valueColor = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox(
                            label = "Terkumpul",
                            value = formatRupiah(duesSummary?.terkumpul ?: 0.0),
                            valueColor = IncomeGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "Tunggakan",
                            value = formatRupiah(duesSummary?.tunggakan ?: 0.0),
                            valueColor = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            onLoadArrears()
                            showArrearsModal = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("view_arrears_report_button")
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lihat Laporan Tunggakan Semua Anggota", color = ExpenseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Member Status List Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Status Anggota",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val list = duesSummary?.memberStatuses ?: emptyList()
                    if (list.isEmpty()) {
                        EmptyState(text = "Belum ada anggota aktif")
                    } else {
                        list.forEachIndexed { index, item ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    thickness = 0.8.dp
                                )
                            }
                            MemberDuesStatusRow(
                                item = item,
                                onQuickPay = {
                                    preselectedMemberId = item.member.id
                                    showPayModal = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Pay Dues Modal
    if (showPayModal) {
        PayDuesDialog(
            members = members.filter { it.aktif },
            accounts = accounts,
            duesSettings = duesSettings,
            preselectedMemberId = preselectedMemberId,
            onDismiss = { showPayModal = false },
            onSave = { memberId, amount, date, notes, accId ->
                onPayDues(memberId, amount, date, notes, accId) {
                    showPayModal = false
                }
            }
        )
    }

    // Dues Settings Modal
    if (showSettingsModal) {
        DuesSettingsDialog(
            settings = duesSettings,
            onDismiss = { showSettingsModal = false },
            onSave = { name, nominal, start ->
                onUpdateSettings(name, nominal, start) {
                    showSettingsModal = false
                }
            }
        )
    }

    // Arrears Report Dialog
    if (showArrearsModal) {
        ArrearsDialog(
            arrearsList = arrearsList,
            currentPeriod = currentPeriod,
            onDismiss = { showArrearsModal = false }
        )
    }
}

@Composable
fun MemberDuesStatusRow(
    item: MemberDuesStatus,
    onQuickPay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.member.nama,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))

            val paidText = "${formatRupiah(item.dibayar)} / ${formatRupiah(item.wajib)}"
            val lunasSampaiText = item.lunasSampai?.let { " · Lunas s/d ${formatPeriodeLabel(it)}" } ?: ""

            Text(
                text = "$paidText$lunasSampaiText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            StatusBadge(status = item.status)
            if (item.status != "lunas" && item.status != "tidak_dikenakan") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bayar",
                    color = PrimaryGreen,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onQuickPay() }
                        .padding(2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayDuesDialog(
    members: List<Member>,
    accounts: List<AccountWithBalance>,
    duesSettings: DuesSettings?,
    preselectedMemberId: String?,
    onDismiss: () -> Unit,
    onSave: (memberId: String, amount: Double, date: String, notes: String?, accountId: String?) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf(preselectedMemberId ?: members.firstOrNull()?.id ?: "") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: "") }
    var amountText by remember { mutableStateOf((duesSettings?.nominal?.toLong() ?: 0L).toString()) }
    var dateText by remember { mutableStateOf(KasKitaRepository.todayStr()) }
    var notesText by remember { mutableStateOf("") }

    var expandedMember by remember { mutableStateOf(false) }
    var expandedAccount by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Pembayaran Iuran", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Member Selector
                ExposedDropdownMenuBox(
                    expanded = expandedMember,
                    onExpandedChange = { expandedMember = it }
                ) {
                    val currentMemberName = members.find { it.id == selectedMemberId }?.nama ?: "Pilih Anggota"
                    OutlinedTextField(
                        value = currentMemberName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Anggota") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMember) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMember,
                        onDismissRequest = { expandedMember = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.nama) },
                                onClick = {
                                    selectedMemberId = m.id
                                    expandedMember = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Account Selector
                ExposedDropdownMenuBox(
                    expanded = expandedAccount,
                    onExpandedChange = { expandedAccount = it }
                ) {
                    val currentAcc = accounts.find { it.account.id == selectedAccountId }?.let {
                        "${it.account.nama} (${formatRupiah(it.saldo)})"
                    } ?: "Pilih Akun Kas"
                    OutlinedTextField(
                        value = currentAcc,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Masuk ke Akun") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccount) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccount,
                        onDismissRequest = { expandedAccount = false }
                    ) {
                        accounts.forEach { item ->
                            DropdownMenuItem(
                                text = { Text("${item.account.nama} (${formatRupiah(item.saldo)})") },
                                onClick = {
                                    selectedAccountId = item.account.id
                                    expandedAccount = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Jumlah Pembayaran (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pay_dues_amount_input")
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = PrimaryGreenLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Otomatis dialokasikan ke bulan tertua yang belum lunas (FIFO).",
                            fontSize = 11.5.sp,
                            color = PrimaryGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Tanggal Bayar (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Catatan (Opsional)") },
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val amountVal = amountText.toDoubleOrNull()
            val isValid = selectedMemberId.isNotBlank() && amountVal != null && amountVal > 0
            Button(
                onClick = {
                    if (isValid) {
                        onSave(selectedMemberId, amountVal!!, dateText, notesText.ifBlank { null }, selectedAccountId)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                modifier = Modifier.testTag("submit_pay_dues_button")
            ) {
                Text("Bayar Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun DuesSettingsDialog(
    settings: DuesSettings?,
    onDismiss: () -> Unit,
    onSave: (name: String, nominal: Double, startDate: String) -> Unit
) {
    var name by remember { mutableStateOf(settings?.namaIuran ?: "Iuran Bulanan") }
    var nominalText by remember { mutableStateOf((settings?.nominal?.toLong() ?: 0L).toString()) }
    var startDate by remember { mutableStateOf(settings?.tanggalMulai ?: KasKitaRepository.todayStr()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pengaturan Iuran", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Iuran") },
                    placeholder = { Text("Contoh: Iuran Kas Bulanan") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nominalText,
                    onValueChange = { nominalText = it },
                    label = { Text("Nominal per Bulan (Rp)") },
                    placeholder = { Text("Contoh: 25000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Mulai Diberlakukan (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val nominalVal = nominalText.toDoubleOrNull()
            val isValid = name.isNotBlank() && nominalVal != null && nominalVal >= 0 && startDate.isNotBlank()
            Button(
                onClick = {
                    if (isValid) {
                        onSave(name, nominalVal!!, startDate)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ArrearsDialog(
    arrearsList: List<MemberArrears>,
    currentPeriod: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Laporan Tunggakan s/d ${formatPeriodeLabel(currentPeriod)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (arrearsList.isEmpty()) {
                    EmptyState(text = "Hebat! Tidak ada anggota yang menunggak.")
                } else {
                    val totalSemua = arrearsList.sumOf { it.totalTunggakan }
                    Text(
                        text = "Total Tunggakan: ${formatRupiah(totalSemua)} (${arrearsList.size} orang)",
                        style = MaterialTheme.typography.titleMedium,
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        itemsIndexed(arrearsList) { index, item ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.member.nama,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = item.member.noHp ?: "-",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = formatRupiah(item.totalTunggakan),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Tutup")
            }
        }
    )
}
