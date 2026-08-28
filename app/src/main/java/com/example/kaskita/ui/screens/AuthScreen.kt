package com.example.kaskita.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaskita.data.model.Organization
import com.example.kaskita.data.model.User
import com.example.kaskita.ui.theme.PrimaryGreen
import com.example.kaskita.ui.theme.PrimaryGreenLight
import com.example.kaskita.ui.theme.SecondaryGold

@Composable
fun AuthScreen(
    currentUser: User?,
    userOrgs: List<Organization>,
    isLoading: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onCreateOrg: (String) -> Unit,
    onJoinOrg: (String) -> Unit,
    onSelectOrg: (Organization) -> Unit,
    onLogout: () -> Unit
) {
    if (currentUser == null) {
        LoginRegisterView(
            isLoading = isLoading,
            onLogin = onLogin,
            onRegister = onRegister
        )
    } else {
        OrganizationSelectView(
            currentUser = currentUser,
            userOrgs = userOrgs,
            isLoading = isLoading,
            onCreateOrg = onCreateOrg,
            onJoinOrg = onJoinOrg,
            onSelectOrg = onSelectOrg,
            onLogout = onLogout
        )
    }
}

@Composable
private fun LoginRegisterView(
    isLoading: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Hero
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💰", fontSize = 36.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Kas Kita",
                style = MaterialTheme.typography.headlineLarge,
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pencatatan kas organisasi, rapi & transparan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))

            // Main Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "Daftar Akun Baru" else "Masuk",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nama Lengkap") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { if (isRegisterMode) Text("Minimal 6 karakter") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                onRegister(name, email, password)
                            } else {
                                onLogin(email, password)
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (isRegisterMode) "Daftar" else "Masuk",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRegisterMode) "Sudah punya akun?" else "Belum punya akun?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = { isRegisterMode = !isRegisterMode },
                            modifier = Modifier.testTag("toggle_auth_mode_button")
                        ) {
                            Text(
                                text = if (isRegisterMode) "Masuk" else "Daftar",
                                style = MaterialTheme.typography.labelLarge,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrganizationSelectView(
    currentUser: User,
    userOrgs: List<Organization>,
    isLoading: Boolean,
    onCreateOrg: (String) -> Unit,
    onJoinOrg: (String) -> Unit,
    onSelectOrg: (Organization) -> Unit,
    onLogout: () -> Unit
) {
    var selectedOrgTab by remember { mutableIntStateOf(0) } // 0 = Buat Baru, 1 = Gabung
    var newOrgName by remember { mutableStateOf("") }
    var joinInviteCode by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pilih Organisasi",
                style = MaterialTheme.typography.headlineMedium,
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hai ${currentUser.nama}, buat organisasi baru atau gabung yang sudah ada",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Existing Orgs List
            if (userOrgs.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Organisasi Kamu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        userOrgs.forEach { org ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = org.nama,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Kode: ${org.inviteCode}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { onSelectOrg(org) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("select_org_${org.id}")
                                ) {
                                    Text("Buka", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Create or Join Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TabRow(
                        selectedTabIndex = selectedOrgTab,
                        containerColor = PrimaryGreenLight,
                        contentColor = PrimaryGreen,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedOrgTab == 0,
                            onClick = { selectedOrgTab = 0 },
                            text = { Text("Buat Baru", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedOrgTab == 1,
                            onClick = { selectedOrgTab = 1 },
                            text = { Text("Gabung", fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedOrgTab == 0) {
                        OutlinedTextField(
                            value = newOrgName,
                            onValueChange = { newOrgName = it },
                            label = { Text("Nama Organisasi / Lembaga") },
                            placeholder = { Text("Contoh: Kas RT 05, Karang Taruna") },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_org_name_input")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onCreateOrg(newOrgName) },
                            enabled = !isLoading && newOrgName.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("create_org_button")
                        ) {
                            Text("Buat Organisasi", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedTextField(
                            value = joinInviteCode,
                            onValueChange = { joinInviteCode = it.uppercase() },
                            label = { Text("Kode Undangan") },
                            placeholder = { Text("8 Karakter (Contoh: ABC123XY)") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("join_invite_code_input")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onJoinOrg(joinInviteCode) },
                            enabled = !isLoading && joinInviteCode.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("join_org_button")
                        ) {
                            Text("Gabung Organisasi", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onLogout,
                modifier = Modifier.testTag("org_screen_logout_button")
            ) {
                Text(
                    text = "Keluar Akun",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
