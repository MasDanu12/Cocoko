package com.example.kaskita.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaskita.data.local.AppDatabase
import com.example.kaskita.data.model.AccountWithBalance
import com.example.kaskita.data.model.AnnualReport
import com.example.kaskita.data.model.Category
import com.example.kaskita.data.model.DashboardSummary
import com.example.kaskita.data.model.DuesSettings
import com.example.kaskita.data.model.Member
import com.example.kaskita.data.model.MemberArrears
import com.example.kaskita.data.model.MonthlyDuesSummary
import com.example.kaskita.data.model.MonthlyReport
import com.example.kaskita.data.model.Organization
import com.example.kaskita.data.model.ReceiptData
import com.example.kaskita.data.model.TransactionDetail
import com.example.kaskita.data.model.User
import com.example.kaskita.data.repository.KasKitaRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class KasKitaUiState(
    val currentUser: User? = null,
    val currentOrg: Organization? = null,
    val userOrgs: List<Organization> = emptyList(),
    val currentTab: String = "beranda", // beranda, anggota, kas, iuran, laporan, profil
    val isDarkTheme: Boolean = false,
    val isLoading: Boolean = false,

    // Data for active organization
    val dashboardSummary: DashboardSummary? = null,
    val accounts: List<AccountWithBalance> = emptyList(),
    val categories: List<Category> = emptyList(),
    val members: List<Member> = emptyList(),
    val transactions: List<TransactionDetail> = emptyList(),
    val kasFilter: String = "", // "", "masuk", "keluar", "transfer", "penyesuaian"

    // Iuran
    val duesSettings: DuesSettings? = null,
    val duesSelectedPeriod: String = KasKitaRepository.currentPeriode(),
    val duesSummary: MonthlyDuesSummary? = null,
    val arrearsList: List<MemberArrears> = emptyList(),

    // Reports
    val reportTab: String = "bulanan", // bulanan, tahunan
    val reportSelectedMonth: String = KasKitaRepository.currentPeriode(),
    val reportSelectedYear: String = Calendar.getInstance().get(Calendar.YEAR).toString(),
    val monthlyReport: MonthlyReport? = null,
    val annualReport: AnnualReport? = null,

    // Modals & Dialogs
    val activeReceipt: ReceiptData? = null
)

class KasKitaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KasKitaRepository
    private val prefs = application.getSharedPreferences("kaskita_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(KasKitaUiState())
    val uiState: StateFlow<KasKitaUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = KasKitaRepository(db.kasKitaDao())

        val savedDark = prefs.getBoolean("dark_theme", false)
        val savedUserId = prefs.getString("user_id", null)
        val savedOrgId = prefs.getString("org_id", null)

        _uiState.update { it.copy(isDarkTheme = savedDark) }

        if (savedUserId != null) {
            viewModelScope.launch {
                val dbUser = db.kasKitaDao().getUserById(savedUserId)
                if (dbUser != null) {
                    _uiState.update { it.copy(currentUser = dbUser) }
                    loadUserOrganizations(dbUser.id, savedOrgId)
                }
            }
        }
    }

    private fun showToast(msg: String) {
        viewModelScope.launch {
            _toastEvent.emit(msg)
        }
    }

    fun toggleDarkTheme(enable: Boolean) {
        prefs.edit().putBoolean("dark_theme", enable).apply()
        _uiState.update { it.copy(isDarkTheme = enable) }
    }

    fun setTab(tab: String) {
        _uiState.update { it.copy(currentTab = tab) }
        refreshActiveTabData(tab)
    }

    // --- Authentication ---
    fun register(nama: String, email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.registerUser(email, pass, nama)
            result.onSuccess { user ->
                prefs.edit().putString("user_id", user.id).apply()
                _uiState.update { it.copy(currentUser = user, isLoading = false) }
                loadUserOrganizations(user.id, null)
                showToast("Selamat datang, ${user.nama}!")
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false) }
                showToast(err.message ?: "Registrasi gagal")
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.loginUser(email, pass)
            result.onSuccess { user ->
                prefs.edit().putString("user_id", user.id).apply()
                _uiState.update { it.copy(currentUser = user, isLoading = false) }
                loadUserOrganizations(user.id, null)
                showToast("Berhasil masuk")
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false) }
                showToast(err.message ?: "Login gagal")
            }
        }
    }

    fun logout() {
        prefs.edit().remove("user_id").remove("org_id").apply()
        _uiState.update {
            KasKitaUiState(isDarkTheme = it.isDarkTheme)
        }
        showToast("Sampai jumpa!")
    }

    fun updateProfileName(newName: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.updateUserProfile(user.id, newName).onSuccess {
                _uiState.update { it.copy(currentUser = user.copy(nama = newName)) }
                showToast("Nama berhasil diperbarui")
            }.onFailure {
                showToast(it.message ?: "Gagal memperbarui nama")
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String, onSuccess: () -> Unit) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.changePassword(user.id, oldPass, newPass).onSuccess {
                showToast("Password berhasil diubah")
                onSuccess()
            }.onFailure {
                showToast(it.message ?: "Gagal mengubah password")
            }
        }
    }

    // --- Organizations ---
    private fun loadUserOrganizations(userId: String, preferredOrgId: String?) {
        viewModelScope.launch {
            val orgs = repository.getUserOrganizations(userId)
            val selectedOrg = if (preferredOrgId != null) {
                orgs.find { it.id == preferredOrgId } ?: orgs.firstOrNull()
            } else {
                orgs.firstOrNull()
            }
            _uiState.update { it.copy(userOrgs = orgs, currentOrg = selectedOrg) }
            if (selectedOrg != null) {
                prefs.edit().putString("org_id", selectedOrg.id).apply()
                refreshOrgData(selectedOrg.id)
            }
        }
    }

    fun createOrganization(name: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.createOrganization(user.id, name).onSuccess { org ->
                prefs.edit().putString("org_id", org.id).apply()
                val updatedOrgs = repository.getUserOrganizations(user.id)
                _uiState.update {
                    it.copy(
                        userOrgs = updatedOrgs,
                        currentOrg = org,
                        isLoading = false,
                        currentTab = "beranda"
                    )
                }
                refreshOrgData(org.id)
                showToast("Organisasi '${org.nama}' berhasil dibuat!")
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
                showToast(it.message ?: "Gagal membuat organisasi")
            }
        }
    }

    fun joinOrganization(inviteCode: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.joinOrganization(user.id, inviteCode).onSuccess { org ->
                prefs.edit().putString("org_id", org.id).apply()
                val updatedOrgs = repository.getUserOrganizations(user.id)
                _uiState.update {
                    it.copy(
                        userOrgs = updatedOrgs,
                        currentOrg = org,
                        isLoading = false,
                        currentTab = "beranda"
                    )
                }
                refreshOrgData(org.id)
                showToast("Berhasil bergabung ke '${org.nama}'!")
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
                showToast(it.message ?: "Gagal bergabung")
            }
        }
    }

    fun selectOrganization(org: Organization) {
        prefs.edit().putString("org_id", org.id).apply()
        _uiState.update { it.copy(currentOrg = org, currentTab = "beranda") }
        refreshOrgData(org.id)
    }

    fun openSwitchOrganization() {
        _uiState.update { it.copy(currentOrg = null) }
        val user = _uiState.value.currentUser
        if (user != null) {
            viewModelScope.launch {
                val orgs = repository.getUserOrganizations(user.id)
                _uiState.update { it.copy(userOrgs = orgs) }
            }
        }
    }

    // --- Data Refresh ---
    fun refreshOrgData(orgId: String? = null) {
        val targetOrgId = orgId ?: _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            val accounts = repository.getAccountsWithBalances(targetOrgId)
            val categories = repository.getCategories(targetOrgId)
            val duesSettings = repository.getDuesSettings(targetOrgId)

            _uiState.update {
                it.copy(
                    accounts = accounts,
                    categories = categories,
                    duesSettings = duesSettings
                )
            }
            refreshActiveTabData(_uiState.value.currentTab)
        }
    }

    private fun refreshActiveTabData(tab: String) {
        val orgId = _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            when (tab) {
                "beranda" -> {
                    val summary = repository.getDashboardSummary(orgId)
                    _uiState.update { it.copy(dashboardSummary = summary) }
                }
                "anggota" -> {
                    val members = repository.getAllMembers(orgId)
                    _uiState.update { it.copy(members = members) }
                }
                "kas" -> {
                    val accounts = repository.getAccountsWithBalances(orgId)
                    val trxs = repository.getTransactionsWithDetails(orgId, _uiState.value.kasFilter.ifEmpty { null })
                    _uiState.update { it.copy(transactions = trxs, accounts = accounts) }
                }
                "iuran" -> {
                    val summary = repository.getMonthlyDuesSummary(orgId, _uiState.value.duesSelectedPeriod)
                    val settings = repository.getDuesSettings(orgId)
                    val members = repository.getAllMembers(orgId)
                    _uiState.update { it.copy(duesSummary = summary, duesSettings = settings, members = members) }
                }
                "laporan" -> {
                    loadReports()
                }
            }
        }
    }

    // --- Members CRUD ---
    fun addMember(name: String, phone: String?, notes: String?, joinDate: String?, onSuccess: () -> Unit) {
        val orgId = _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            repository.addMember(orgId, name, phone, notes, joinDate).onSuccess {
                showToast("Anggota berhasil ditambahkan")
                refreshOrgData(orgId)
                onSuccess()
            }.onFailure {
                showToast(it.message ?: "Gagal menambah anggota")
            }
        }
    }

    fun updateMember(
        memberId: String,
        name: String,
        phone: String?,
        notes: String?,
        joinDate: String?,
        active: Boolean,
        onSuccess: () -> Unit
    ) {
        val orgId = _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            repository.updateMember(orgId, memberId, name, phone, notes, joinDate, active).onSuccess {
                showToast("Data anggota diperbarui")
                refreshOrgData(orgId)
                onSuccess()
            }.onFailure {
                showToast(it.message ?: "Gagal memperbarui anggota")
            }
        }
    }

    fun deleteMember(memberId: String, onSuccess: () -> Unit) {
        val orgId = _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            repository.deleteMember(orgId, memberId).onSuccess {
                showToast("Anggota berhasil dihapus")
                refreshOrgData(orgId)
                onSuccess()
            }.onFailure {
                showToast(it.message ?: "Gagal menghapus anggota")
            }
        }
    }

    // --- Transactions CRUD ---
    fun setKasFilter(filter: String) {
        _uiState.update { it.copy(kasFilter = filter) }
        val orgId = _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            val trxs = repository.getTransactionsWithDetails(orgId, filter.ifEmpty { null })
            _uiState.update { it.copy(transactions = trxs) }
        }
    }

    fun addTransaction(
        tipe: String,
        kategori: String?,
        jumlah: Double,
        catatan: String?,
        metode: String?,
        akunId: String?,
        akunTujuanId: String?,
        tanggal: String?,
        onSuccess: () -> Unit
    ) {
        val orgId = _uiState.value.currentOrg?.id ?: return
        val user = _uiState.value.currentUser
        viewModelScope.launch {
            repository.addTransaction(
                orgId = orgId,
                tipe = tipe,
                kategori = kategori,
                jumlah = jumlah,
                catatan = catatan,
                metode = metode,
                akunId = akunId,
                akunTujuanId = akunTujuanId,
                tanggal = tanggal,
                userId = user?.id
            ).onSuccess { trx ->
                showToast("Transaksi tersimpan")
                refreshOrgData(orgId)
                onSuccess()
                if (tipe == "masuk" || tipe == "keluar") {
                    loadReceipt(trx.id)
                }
            }.onFailure {
                showToast(it.message ?: "Gagal menyimpan transaksi")
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        val orgId = _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            repository.deleteTransaction(orgId, transactionId).onSuccess {
                showToast("Transaksi dihapus")
                refreshOrgData(orgId)
            }.onFailure {
                showToast(it.message ?: "Gagal menghapus transaksi")
            }
        }
    }

    // --- Dues ---
    fun setDuesPeriod(period: String) {
        _uiState.update { it.copy(duesSelectedPeriod = period) }
        val orgId = _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            val summary = repository.getMonthlyDuesSummary(orgId, period)
            _uiState.update { it.copy(duesSummary = summary) }
        }
    }

    fun updateDuesSettings(namaIuran: String, nominal: Double, tanggalMulai: String, onSuccess: () -> Unit) {
        val orgId = _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            repository.updateDuesSettings(orgId, namaIuran, nominal, tanggalMulai).onSuccess {
                showToast("Pengaturan iuran tersimpan")
                refreshOrgData(orgId)
                onSuccess()
            }.onFailure {
                showToast(it.message ?: "Gagal memperbarui pengaturan iuran")
            }
        }
    }

    fun payDues(
        memberId: String,
        jumlah: Double,
        tanggal: String?,
        catatan: String?,
        akunId: String?,
        onSuccess: () -> Unit
    ) {
        val orgId = _uiState.value.currentOrg?.id ?: return
        val user = _uiState.value.currentUser
        viewModelScope.launch {
            repository.payDues(orgId, memberId, jumlah, tanggal, catatan, akunId, user?.id).onSuccess { (trx, periodes) ->
                val lunasInfo = if (periodes.isNotEmpty()) " · Periode ${periodes.joinToString(", ")}" else ""
                showToast("Pembayaran iuran berhasil dicatat$lunasInfo")
                refreshOrgData(orgId)
                onSuccess()
                loadReceipt(trx.id)
            }.onFailure {
                showToast(it.message ?: "Gagal mencatat iuran")
            }
        }
    }

    fun loadArrearsReport() {
        val orgId = _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            val list = repository.getArrearsReport(orgId, _uiState.value.duesSelectedPeriod)
            _uiState.update { it.copy(arrearsList = list) }
        }
    }

    // --- Reports ---
    fun setReportTab(tab: String) {
        _uiState.update { it.copy(reportTab = tab) }
        loadReports()
    }

    fun setReportMonth(month: String) {
        _uiState.update { it.copy(reportSelectedMonth = month) }
        loadReports()
    }

    fun setReportYear(year: String) {
        _uiState.update { it.copy(reportSelectedYear = year) }
        loadReports()
    }

    fun loadReports() {
        val orgId = _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            if (_uiState.value.reportTab == "bulanan") {
                val monthly = repository.getMonthlyReport(orgId, _uiState.value.reportSelectedMonth)
                _uiState.update { it.copy(monthlyReport = monthly) }
            } else {
                val annual = repository.getAnnualReport(orgId, _uiState.value.reportSelectedYear)
                _uiState.update { it.copy(annualReport = annual) }
            }
        }
    }

    // --- Receipt ---
    fun loadReceipt(transactionId: String) {
        val orgId = _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            val data = repository.getReceiptData(orgId, transactionId)
            _uiState.update { it.copy(activeReceipt = data) }
        }
    }

    fun closeReceipt() {
        _uiState.update { it.copy(activeReceipt = null) }
    }
}
