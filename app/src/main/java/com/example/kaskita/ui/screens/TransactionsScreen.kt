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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaskita.data.model.AccountWithBalance
import com.example.kaskita.data.model.Category
import com.example.kaskita.data.model.TransactionDetail
import com.example.kaskita.data.repository.KasKitaRepository
import com.example.kaskita.ui.components.EmptyState
import com.example.kaskita.ui.components.StatusBadge
import com.example.kaskita.ui.components.formatRupiah
import com.example.kaskita.ui.theme.ExpenseRed
import com.example.kaskita.ui.theme.ExpenseRedLight
import com.example.kaskita.ui.theme.IncomeGreen
import com.example.kaskita.ui.theme.IncomeGreenLight
import com.example.kaskita.ui.theme.PrimaryGreen
import com.example.kaskita.ui.theme.PrimaryGreenLight
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<TransactionDetail>,
    accounts: List<AccountWithBalance>,
    categories: List<Category>,
    currentFilter: String,
    onFilterChanged: (String) -> Unit,
    onAddTransaction: (tipe: String, kategori: String?, jumlah: Double, catatan: String?, metode: String?, akunId: String?, akunTujuanId: String?, tanggal: String?, onSuccess: () -> Unit) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onOpenReceipt: (String) -> Unit
) {
    var activeModalType by remember { mutableStateOf<String?>(null) } // "masuk", "keluar", "transfer", "penyesuaian"
    var transactionToDelete by remember { mutableStateOf<TransactionDetail?>(null) }

    val filterOptions = listOf(
        Pair("", "Semua"),
        Pair("masuk", "Masuk"),
        Pair("keluar", "Keluar"),
        Pair("transfer", "Transfer"),
        Pair("penyesuaian", "Penyesuaian")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Quick Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { activeModalType = "masuk" },
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("add_income_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Masuk", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { activeModalType = "keluar" },
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("add_expense_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Keluar", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { activeModalType = "transfer" },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("transfer_button")
            ) {
                Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Transfer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { activeModalType = "penyesuaian" },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("adjustment_button")
            ) {
                Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Penyesuaian", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { (key, label) ->
                val selected = currentFilter == key
                FilterChip(
                    selected = selected,
                    onClick = { onFilterChanged(key) },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryGreen,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_chip_$label")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Transactions List Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (transactions.isEmpty()) {
                EmptyState(
                    text = "Belum ada transaksi di filter ini",
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    itemsIndexed(transactions) { index, item ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                thickness = 0.8.dp
                            )
                        }
                        TransactionListItem(
                            item = item,
                            onOpenReceipt = { onOpenReceipt(item.transaction.id) },
                            onDelete = { transactionToDelete = item }
                        )
                    }
                }
            }
        }
    }

    // Add Transaction Dialog
    if (activeModalType != null) {
        TransactionFormDialog(
            type = activeModalType!!,
            accounts = accounts,
            categories = categories,
            onDismiss = { activeModalType = null },
            onSave = { kat, amt, notes, method, accId, accTujuanId, date ->
                onAddTransaction(activeModalType!!, kat, amt, notes, method, accId, accTujuanId, date) {
                    activeModalType = null
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Hapus Transaksi?", fontWeight = FontWeight.Bold) },
            text = { Text("Transaksi sebesar ${formatRupiah(transactionToDelete!!.transaction.jumlah)} akan dihapus dari buku kas.") },
            confirmButton = {
                Button(
                    onClick = {
                        val t = transactionToDelete!!
                        transactionToDelete = null
                        onDeleteTransaction(t.transaction.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun TransactionListItem(
    item: TransactionDetail,
    onOpenReceipt: () -> Unit,
    onDelete: () -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                if (item.akunNama != null && !isTransfer) subtitle += " · ${item.akunNama}"
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
                    fontSize = 15.sp,
                    color = amountColor
                )
            }
        }

        // Action links
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onOpenReceipt() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Struk", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (t.sumber != "iuran") {
                Spacer(modifier = Modifier.width(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onDelete() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Hapus", color = ExpenseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormDialog(
    type: String, // "masuk", "keluar", "transfer", "penyesuaian"
    accounts: List<AccountWithBalance>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (kategori: String?, jumlah: Double, catatan: String?, metode: String?, akunId: String?, akunTujuanId: String?, tanggal: String) -> Unit
) {
    val relevantCategories = categories.filter { it.tipe == type }
    var selectedCategory by remember { mutableStateOf(relevantCategories.firstOrNull()?.nama ?: "") }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: "") }
    var selectedAccountDestination by remember { mutableStateOf(accounts.getOrNull(1)?.account?.id ?: accounts.firstOrNull()?.account?.id ?: "") }
    var amountText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(KasKitaRepository.todayStr()) }
    var notesText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Tunai") }

    var expandedAccount by remember { mutableStateOf(false) }
    var expandedAccountDest by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedMethod by remember { mutableStateOf(false) }

    val methods = listOf("Tunai", "Transfer Bank", "E-Wallet")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val title = when (type) {
                "masuk" -> "Catat Pemasukan"
                "keluar" -> "Catat Pengeluaran"
                "transfer" -> "Transfer Antar Akun"
                else -> "Penyesuaian Saldo"
            }
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Account selector
                ExposedDropdownMenuBox(
                    expanded = expandedAccount,
                    onExpandedChange = { expandedAccount = it }
                ) {
                    val currentAccountName = accounts.find { it.account.id == selectedAccount }?.let {
                        "${it.account.nama} (${formatRupiah(it.saldo)})"
                    } ?: "Pilih Akun"

                    OutlinedTextField(
                        value = currentAccountName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (type == "transfer") "Dari Akun" else "Akun") },
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
                                    selectedAccount = item.account.id
                                    expandedAccount = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (type == "transfer") {
                    ExposedDropdownMenuBox(
                        expanded = expandedAccountDest,
                        onExpandedChange = { expandedAccountDest = it }
                    ) {
                        val currentDestName = accounts.find { it.account.id == selectedAccountDestination }?.let {
                            "${it.account.nama} (${formatRupiah(it.saldo)})"
                        } ?: "Pilih Akun Tujuan"

                        OutlinedTextField(
                            value = currentDestName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ke Akun Tujuan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountDest) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAccountDest,
                            onDismissRequest = { expandedAccountDest = false }
                        ) {
                            accounts.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.account.nama} (${formatRupiah(item.saldo)})") },
                                    onClick = {
                                        selectedAccountDestination = item.account.id
                                        expandedAccountDest = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (type == "masuk" || type == "keluar") {
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = { selectedCategory = it },
                            label = { Text("Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            relevantCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.nama) },
                                    onClick = {
                                        selectedCategory = cat.nama
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (type == "penyesuaian") "Jumlah Penyesuaian (Rp, boleh minus)" else "Jumlah (Rp)") },
                    placeholder = { Text(if (type == "penyesuaian") "Contoh: -50000" else "Contoh: 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_transaction_amount_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (type == "masuk" || type == "keluar") {
                    ExposedDropdownMenuBox(
                        expanded = expandedMethod,
                        onExpandedChange = { expandedMethod = it }
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Metode Pembayaran") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMethod) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMethod,
                            onDismissRequest = { expandedMethod = false }
                        ) {
                            methods.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = {
                                        paymentMethod = m
                                        expandedMethod = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text(if (type == "penyesuaian") "Keterangan (Wajib)" else "Catatan / Keterangan (Opsional)") },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val amountVal = amountText.toDoubleOrNull()
            val isValid = amountVal != null &&
                    (type == "penyesuaian" || amountVal > 0) &&
                    (type != "penyesuaian" || notesText.isNotBlank())

            Button(
                onClick = {
                    if (isValid) {
                        onSave(
                            if (type == "masuk" || type == "keluar") selectedCategory else null,
                            amountVal!!,
                            notesText,
                            if (type == "masuk" || type == "keluar") paymentMethod else null,
                            selectedAccount,
                            if (type == "transfer") selectedAccountDestination else null,
                            dateText
                        )
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "keluar") ExpenseRed else PrimaryGreen
                ),
                modifier = Modifier.testTag("dialog_save_transaction_button")
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
