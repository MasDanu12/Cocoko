package com.example.kaskita.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaskita.data.model.Organization
import com.example.kaskita.data.model.User
import com.example.kaskita.ui.theme.ExpenseRed
import com.example.kaskita.ui.theme.PrimaryGreen
import com.example.kaskita.ui.theme.PrimaryGreenLight
import com.example.kaskita.ui.theme.SecondaryGold

@Composable
fun ProfileScreen(
    user: User?,
    org: Organization?,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onUpdateName: (String) -> Unit,
    onChangePassword: (String, String, () -> Unit) -> Unit,
    onSwitchOrg: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var nameInput by remember(user?.nama) { mutableStateOf(user?.nama ?: "") }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var showPasswordForm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User Identity Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Profil Pengguna",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nama Lengkap") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Email: ${user?.email ?: "-"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onUpdateName(nameInput) },
                        enabled = nameInput.isNotBlank() && nameInput != user?.nama,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_profile_name_button")
                    ) {
                        Text("Simpan Perubahan Nama", fontSize = 13.sp)
                    }
                }
            }
        }

        // Active Organization Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Organisasi Aktif",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = org?.nama ?: "-",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Kode Undangan Organisasi",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = PrimaryGreenLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = org?.inviteCode ?: "--------",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                            IconButton(
                                onClick = {
                                    org?.inviteCode?.let { code ->
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Kode Undangan", code))
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Salin Kode",
                                    tint = PrimaryGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bagikan kode ini kepada bendahara atau pengurus lain agar dapat bergabung.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = onSwitchOrg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("switch_org_button")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ganti / Tambah Organisasi", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Security Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Keamanan Akun",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (!showPasswordForm) {
                        OutlinedButton(
                            onClick = { showPasswordForm = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ubah Password", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        OutlinedTextField(
                            value = oldPass,
                            onValueChange = { oldPass = it },
                            label = { Text("Password Lama") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newPass,
                            onValueChange = { newPass = it },
                            label = { Text("Password Baru (Min. 6 Karakter)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onChangePassword(oldPass, newPass) {
                                        oldPass = ""
                                        newPass = ""
                                        showPasswordForm = false
                                    }
                                },
                                enabled = oldPass.isNotBlank() && newPass.length >= 6,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Simpan Password")
                            }
                            TextButton(onClick = { showPasswordForm = false }) {
                                Text("Batal")
                            }
                        }
                    }
                }
            }
        }

        // Appearance Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = PrimaryGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Mode Gelap (Dark Mode)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Ganti tema tampilan aplikasi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onToggleDarkTheme,
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen),
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        // About & Help
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tentang Aplikasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kas Kita v1.0.0 · Sistem Manajemen Kas & Iuran Organisasi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dilengkapi alokasi iuran FIFO otomatis, pembukuan kas multi-akun, laporan bulanan/tahunan, dan pencetakan struk digital.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Logout CTA
        item {
            Button(
                onClick = onLogout,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_logout_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Keluar dari Akun", color = ExpenseRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}
