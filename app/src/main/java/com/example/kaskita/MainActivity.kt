package com.example.kaskita

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaskita.ui.components.AppHeader
import com.example.kaskita.ui.screens.AuthScreen
import com.example.kaskita.ui.screens.DuesScreen
import com.example.kaskita.ui.screens.HomeScreen
import com.example.kaskita.ui.screens.MembersScreen
import com.example.kaskita.ui.screens.ProfileScreen
import com.example.kaskita.ui.screens.ReceiptDialog
import com.example.kaskita.ui.screens.ReportsScreen
import com.example.kaskita.ui.screens.TransactionsScreen
import com.example.kaskita.ui.theme.KasKitaTheme
import com.example.kaskita.ui.theme.PrimaryGreen
import com.example.kaskita.ui.theme.PrimaryGreenLight
import com.example.kaskita.ui.viewmodel.KasKitaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: KasKitaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                viewModel.toastEvent.collect { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }

            KasKitaTheme(darkTheme = uiState.isDarkTheme) {
                if (uiState.currentUser == null || uiState.currentOrg == null) {
                    AuthScreen(
                        currentUser = uiState.currentUser,
                        userOrgs = uiState.userOrgs,
                        isLoading = uiState.isLoading,
                        onLogin = { email, pass -> viewModel.login(email, pass) },
                        onRegister = { name, email, pass -> viewModel.register(name, email, pass) },
                        onCreateOrg = { name -> viewModel.createOrganization(name) },
                        onJoinOrg = { code -> viewModel.joinOrganization(code) },
                        onSelectOrg = { org -> viewModel.selectOrganization(org) },
                        onLogout = { viewModel.logout() }
                    )
                } else {
                    MainAppScaffold(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }

                // Global Active Receipt Dialog
                if (uiState.activeReceipt != null) {
                    ReceiptDialog(
                        data = uiState.activeReceipt,
                        onDismiss = { viewModel.closeReceipt() }
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    viewModel: KasKitaViewModel,
    uiState: com.example.kaskita.ui.viewmodel.KasKitaUiState
) {
    val navItems = listOf(
        Pair("beranda", "Beranda") to Icons.Default.Home,
        Pair("anggota", "Anggota") to Icons.Default.Group,
        Pair("kas", "Kas") to Icons.Default.AccountBalanceWallet,
        Pair("iuran", "Iuran") to Icons.Default.Payment,
        Pair("laporan", "Laporan") to Icons.Default.BarChart,
        Pair("profil", "Profil") to Icons.Default.Person
    )

    Scaffold(
        topBar = {
            val title = when (uiState.currentTab) {
                "beranda" -> "Beranda"
                "anggota" -> "Daftar Anggota"
                "kas" -> "Buku Kas"
                "iuran" -> "Iuran Anggota"
                "laporan" -> "Laporan Keuangan"
                "profil" -> "Profil & Pengaturan"
                else -> "Kas Kita"
            }
            AppHeader(
                orgName = uiState.currentOrg?.nama ?: "Kas Kita",
                title = title
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { (item, icon) ->
                    val (key, label) = item
                    val isSelected = uiState.currentTab == key
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(key) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = PrimaryGreenLight
                        ),
                        modifier = Modifier.testTag("nav_item_$key")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                "beranda" -> {
                    HomeScreen(
                        summary = uiState.dashboardSummary,
                        onNavigateToTab = { viewModel.setTab(it) },
                        onOpenReceipt = { viewModel.loadReceipt(it) }
                    )
                }
                "anggota" -> {
                    MembersScreen(
                        members = uiState.members,
                        onAddMember = { name, hp, notes, joinDate, onSuccess ->
                            viewModel.addMember(name, hp, notes, joinDate, onSuccess)
                        },
                        onUpdateMember = { id, name, hp, notes, joinDate, active, onSuccess ->
                            viewModel.updateMember(id, name, hp, notes, joinDate, active, onSuccess)
                        },
                        onDeleteMember = { id, onSuccess ->
                            viewModel.deleteMember(id, onSuccess)
                        }
                    )
                }
                "kas" -> {
                    TransactionsScreen(
                        transactions = uiState.transactions,
                        accounts = uiState.accounts,
                        categories = uiState.categories,
                        currentFilter = uiState.kasFilter,
                        onFilterChanged = { viewModel.setKasFilter(it) },
                        onAddTransaction = { tipe, kat, amt, notes, method, accId, accTujuanId, date, onSuccess ->
                            viewModel.addTransaction(tipe, kat, amt, notes, method, accId, accTujuanId, date, onSuccess)
                        },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) },
                        onOpenReceipt = { viewModel.loadReceipt(it) }
                    )
                }
                "iuran" -> {
                    DuesScreen(
                        duesSettings = uiState.duesSettings,
                        duesSummary = uiState.duesSummary,
                        currentPeriod = uiState.duesSelectedPeriod,
                        members = uiState.members,
                        accounts = uiState.accounts,
                        arrearsList = uiState.arrearsList,
                        onPeriodChanged = { viewModel.setDuesPeriod(it) },
                        onPayDues = { memberId, amount, date, notes, accId, onSuccess ->
                            viewModel.payDues(memberId, amount, date, notes, accId, onSuccess)
                        },
                        onUpdateSettings = { name, nominal, start, onSuccess ->
                            viewModel.updateDuesSettings(name, nominal, start, onSuccess)
                        },
                        onLoadArrears = { viewModel.loadArrearsReport() }
                    )
                }
                "laporan" -> {
                    ReportsScreen(
                        orgName = uiState.currentOrg?.nama ?: "Kas Kita",
                        reportTab = uiState.reportTab,
                        selectedMonth = uiState.reportSelectedMonth,
                        selectedYear = uiState.reportSelectedYear,
                        monthlyReport = uiState.monthlyReport,
                        annualReport = uiState.annualReport,
                        onTabSelected = { viewModel.setReportTab(it) },
                        onMonthChanged = { viewModel.setReportMonth(it) },
                        onYearChanged = { viewModel.setReportYear(it) }
                    )
                }
                "profil" -> {
                    ProfileScreen(
                        user = uiState.currentUser,
                        org = uiState.currentOrg,
                        isDarkTheme = uiState.isDarkTheme,
                        onToggleDarkTheme = { viewModel.toggleDarkTheme(it) },
                        onUpdateName = { viewModel.updateProfileName(it) },
                        onChangePassword = { oldPass, newPass, onSuccess ->
                            viewModel.changePassword(oldPass, newPass, onSuccess)
                        },
                        onSwitchOrg = { viewModel.openSwitchOrganization() },
                        onLogout = { viewModel.logout() }
                    )
                }
            }
        }
    }
}
