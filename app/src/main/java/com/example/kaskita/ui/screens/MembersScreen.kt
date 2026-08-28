package com.example.kaskita.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaskita.data.model.Member
import com.example.kaskita.data.repository.KasKitaRepository
import com.example.kaskita.ui.components.EmptyState
import com.example.kaskita.ui.components.StatusBadge
import com.example.kaskita.ui.theme.ExpenseRed
import com.example.kaskita.ui.theme.PrimaryGreen

@Composable
fun MembersScreen(
    members: List<Member>,
    onAddMember: (String, String?, String?, String?, () -> Unit) -> Unit,
    onUpdateMember: (String, String, String?, String?, String?, Boolean, () -> Unit) -> Unit,
    onDeleteMember: (String, () -> Unit) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showMemberDialog by remember { mutableStateOf(false) }
    var selectedMemberForEdit by remember { mutableStateOf<Member?>(null) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }

    val filteredMembers = members.filter {
        it.nama.contains(searchQuery, ignoreCase = true) ||
                (it.noHp?.contains(searchQuery) == true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Add Button
        Button(
            onClick = {
                selectedMemberForEdit = null
                showMemberDialog = true
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("add_member_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("+ Tambah Anggota", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari anggota...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("member_search_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Members List Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (filteredMembers.isEmpty()) {
                EmptyState(
                    text = if (searchQuery.isNotEmpty()) "Tidak ada anggota yang sesuai pencarian" else "Belum ada anggota. Tambahkan dulu.",
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    itemsIndexed(filteredMembers) { index, member ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                thickness = 0.8.dp
                            )
                        }
                        MemberListItem(
                            member = member,
                            onEdit = {
                                selectedMemberForEdit = member
                                showMemberDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showMemberDialog) {
        MemberFormDialog(
            member = selectedMemberForEdit,
            onDismiss = { showMemberDialog = false },
            onSave = { name, hp, notes, joinDate, active ->
                if (selectedMemberForEdit == null) {
                    onAddMember(name, hp, notes, joinDate) {
                        showMemberDialog = false
                    }
                } else {
                    onUpdateMember(selectedMemberForEdit!!.id, name, hp, notes, joinDate, active) {
                        showMemberDialog = false
                    }
                }
            },
            onDelete = {
                memberToDelete = selectedMemberForEdit
                showMemberDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("Hapus Anggota?", fontWeight = FontWeight.Bold) },
            text = { Text("Hapus ${memberToDelete!!.nama}? Riwayat transaksinya tetap tersimpan di Kas.") },
            confirmButton = {
                Button(
                    onClick = {
                        val m = memberToDelete!!
                        memberToDelete = null
                        onDeleteMember(m.id) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun MemberListItem(
    member: Member,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = member.nama,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!member.aktif) {
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge(status = "nonaktif")
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${member.noHp ?: "-"} · Gabung ${member.tanggalGabung}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!member.catatan.isNullOrBlank()) {
                Text(
                    text = "Catatan: ${member.catatan}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        OutlinedButton(
            onClick = onEdit,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("edit_member_${member.id}")
        ) {
            Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MemberFormDialog(
    member: Member?,
    onDismiss: () -> Unit,
    onSave: (name: String, hp: String?, notes: String?, joinDate: String, active: Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val isEditing = member != null
    var name by remember { mutableStateOf(member?.nama ?: "") }
    var phone by remember { mutableStateOf(member?.noHp ?: "") }
    var joinDate by remember { mutableStateOf(member?.tanggalGabung ?: KasKitaRepository.todayStr()) }
    var notes by remember { mutableStateOf(member?.catatan ?: "") }
    var isActive by remember { mutableStateOf(member?.aktif ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Anggota" else "Tambah Anggota",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_member_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. HP / WhatsApp") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = joinDate,
                    onValueChange = { joinDate = it },
                    label = { Text("Tanggal Bergabung (YYYY-MM-DD)") },
                    placeholder = { Text("2026-01-01") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isEditing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Anggota Aktif",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, phone, notes, joinDate, isActive)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                modifier = Modifier.testTag("dialog_save_member_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            Row {
                if (isEditing) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                    ) {
                        Text("Hapus")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Batal")
                }
            }
        }
    )
}
