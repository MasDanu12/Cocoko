package com.example.kaskita.data.repository

import com.example.kaskita.data.local.KasKitaDao
import com.example.kaskita.data.model.Account
import com.example.kaskita.data.model.AccountWithBalance
import com.example.kaskita.data.model.AnnualReport
import com.example.kaskita.data.model.Category
import com.example.kaskita.data.model.CategoryBreakdown
import com.example.kaskita.data.model.DashboardSummary
import com.example.kaskita.data.model.DuesAllocation
import com.example.kaskita.data.model.DuesPayment
import com.example.kaskita.data.model.DuesSettings
import com.example.kaskita.data.model.Member
import com.example.kaskita.data.model.MemberArrears
import com.example.kaskita.data.model.MemberDuesStatus
import com.example.kaskita.data.model.MonthTrend
import com.example.kaskita.data.model.MonthlyDuesSummary
import com.example.kaskita.data.model.MonthlyReport
import com.example.kaskita.data.model.Organization
import com.example.kaskita.data.model.OrganizationMember
import com.example.kaskita.data.model.ReceiptData
import com.example.kaskita.data.model.Transaction
import com.example.kaskita.data.model.TransactionDetail
import com.example.kaskita.data.model.User
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class KasKitaRepository(private val dao: KasKitaDao) {

    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Pair("Donasi", "masuk"),
            Pair("Pendapatan Kegiatan", "masuk"),
            Pair("Lain-lain (Masuk)", "masuk"),
            Pair("Kegiatan", "keluar"),
            Pair("Konsumsi", "keluar"),
            Pair("Perlengkapan", "keluar"),
            Pair("Transportasi", "keluar"),
            Pair("Administrasi", "keluar"),
            Pair("Sumbangan/Bantuan", "keluar"),
            Pair("Perawatan", "keluar"),
            Pair("Lainnya", "keluar")
        )

        fun todayStr(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun currentPeriode(): String {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            return sdf.format(Date())
        }

        fun periodeFromDateStr(dateStr: String?): String {
            return (dateStr ?: todayStr()).take(7)
        }

        fun periodAdd(periode: String, n: Int): String {
            val parts = periode.split("-").mapNotNull { it.toIntOrNull() }
            if (parts.size != 2) return periode
            val y = parts[0]
            val m = parts[1]
            val total = y * 12 + (m - 1) + n
            val ny = total / 12
            val nm = (total % 12) + 1
            return String.format(Locale.US, "%04d-%02d", ny, nm)
        }

        fun periodeMax(a: String, b: String): String {
            return if (a > b) a else b
        }

        fun generateInviteCode(): String {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            val random = SecureRandom()
            val sb = StringBuilder(8)
            for (i in 0 until 8) {
                sb.append(chars[random.nextInt(chars.length)])
            }
            return sb.toString()
        }

        fun hashPassword(password: String, salt: String? = null): Pair<String, String> {
            val actualSalt = salt ?: UUID.randomUUID().toString().replace("-", "").take(16)
            val md = MessageDigest.getInstance("SHA-256")
            val combined = "$password:$actualSalt"
            val digest = md.digest(combined.toByteArray(Charsets.UTF_8))
            val hash = digest.joinToString("") { "%02x".format(it) }
            return Pair(hash, actualSalt)
        }

        fun verifyPassword(password: String, storedHash: String, salt: String): Boolean {
            val (hash, _) = hashPassword(password, salt)
            return hash == storedHash
        }
    }

    // --- Authentication & User ---
    suspend fun registerUser(email: String, password: String, nama: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty() || password.isEmpty() || nama.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Email, password, dan nama wajib diisi"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password minimal 6 karakter"))
        }
        val existing = dao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return Result.failure(IllegalStateException("Email sudah terdaftar"))
        }
        val (hash, salt) = hashPassword(password)
        val user = User(
            email = cleanEmail,
            passwordHash = hash,
            passwordSalt = salt,
            nama = nama.trim()
        )
        dao.insertUser(user)
        return Result.success(user)
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Email dan password wajib diisi"))
        }
        val user = dao.getUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("Email atau password salah"))
        if (!verifyPassword(password, user.passwordHash, user.passwordSalt)) {
            return Result.failure(IllegalArgumentException("Email atau password salah"))
        }
        return Result.success(user)
    }

    suspend fun updateUserProfile(userId: String, newName: String): Result<Unit> {
        val user = dao.getUserById(userId)
            ?: return Result.failure(IllegalStateException("User tidak ditemukan"))
        dao.updateUser(user.copy(nama = newName.trim()))
        return Result.success(Unit)
    }

    suspend fun changePassword(userId: String, oldPass: String, newPass: String): Result<Unit> {
        val user = dao.getUserById(userId)
            ?: return Result.failure(IllegalStateException("User tidak ditemukan"))
        if (!verifyPassword(oldPass, user.passwordHash, user.passwordSalt)) {
            return Result.failure(IllegalArgumentException("Password lama salah"))
        }
        if (newPass.length < 6) {
            return Result.failure(IllegalArgumentException("Password baru minimal 6 karakter"))
        }
        val (hash, salt) = hashPassword(newPass)
        dao.updateUser(user.copy(passwordHash = hash, passwordSalt = salt))
        return Result.success(Unit)
    }

    fun observeUser(userId: String): Flow<User?> = dao.observeUserById(userId)

    // --- Organizations ---
    fun observeUserOrganizations(userId: String): Flow<List<Organization>> =
        dao.observeUserOrganizations(userId)

    suspend fun getUserOrganizations(userId: String): List<Organization> =
        dao.getUserOrganizations(userId)

    suspend fun createOrganization(userId: String, orgName: String): Result<Organization> {
        val cleanName = orgName.trim()
        if (cleanName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Nama organisasi wajib diisi"))
        }
        var code = generateInviteCode()
        for (i in 0 until 5) {
            val exists = dao.getOrganizationByInviteCode(code)
            if (exists == null) break
            code = generateInviteCode()
        }
        val org = Organization(
            nama = cleanName,
            inviteCode = code,
            createdBy = userId
        )
        dao.insertOrganization(org)
        dao.insertOrganizationMember(
            OrganizationMember(
                userId = userId,
                organizationId = org.id,
                role = "owner"
            )
        )
        // Seed default dues settings
        dao.insertOrUpdateDuesSettings(
            DuesSettings(
                organizationId = org.id,
                namaIuran = "Iuran Bulanan",
                nominal = 0.0,
                tanggalMulai = todayStr()
            )
        )
        // Seed default Account "Kas Utama"
        dao.insertAccount(
            Account(
                organizationId = org.id,
                nama = "Kas Utama",
                saldoAwal = 0.0
            )
        )
        // Seed default categories
        val cats = DEFAULT_CATEGORIES.map { (nama, tipe) ->
            Category(
                organizationId = org.id,
                nama = nama,
                tipe = tipe
            )
        }
        dao.insertCategories(cats)

        return Result.success(org)
    }

    suspend fun joinOrganization(userId: String, inviteCode: String): Result<Organization> {
        val cleanCode = inviteCode.trim().uppercase()
        if (cleanCode.isEmpty()) {
            return Result.failure(IllegalArgumentException("Kode undangan wajib diisi"))
        }
        val org = dao.getOrganizationByInviteCode(cleanCode)
            ?: return Result.failure(IllegalArgumentException("Kode undangan tidak ditemukan"))

        val already = dao.getOrgMembership(userId, org.id)
        if (already != null) {
            return Result.failure(IllegalStateException("Anda sudah tergabung di organisasi ini"))
        }
        dao.insertOrganizationMember(
            OrganizationMember(
                userId = userId,
                organizationId = org.id,
                role = "member"
            )
        )
        return Result.success(org)
    }

    // --- Accounts & Balances ---
    fun observeAccounts(orgId: String): Flow<List<Account>> = dao.observeAccounts(orgId)

    suspend fun getAccountsWithBalances(orgId: String): List<AccountWithBalance> {
        val accounts = dao.getAccounts(orgId)
        val allTrx = dao.getTransactions(orgId)
        return accounts.map { account ->
            val balance = calculateAccountBalance(account, allTrx)
            AccountWithBalance(account, balance)
        }
    }

    private fun calculateAccountBalance(account: Account, transactions: List<Transaction>): Double {
        var balance = account.saldoAwal
        for (t in transactions) {
            when (t.tipe) {
                "masuk" -> {
                    if (t.akunId == account.id) balance += t.jumlah
                }
                "keluar" -> {
                    if (t.akunId == account.id) balance -= t.jumlah
                }
                "transfer" -> {
                    if (t.akunId == account.id) balance -= t.jumlah
                    if (t.akunTujuanId == account.id) balance += t.jumlah
                }
                "penyesuaian" -> {
                    if (t.akunId == account.id) balance += t.jumlah
                }
            }
        }
        return balance
    }

    suspend fun createAccount(orgId: String, name: String, initialBalance: Double): Result<Account> {
        if (name.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Nama akun wajib diisi"))
        }
        val account = Account(
            organizationId = orgId,
            nama = name.trim(),
            saldoAwal = initialBalance
        )
        dao.insertAccount(account)
        return Result.success(account)
    }

    // --- Categories ---
    fun observeCategories(orgId: String): Flow<List<Category>> = dao.observeCategories(orgId)
    suspend fun getCategories(orgId: String): List<Category> = dao.getCategories(orgId)

    suspend fun addCategory(orgId: String, name: String, type: String): Result<Category> {
        if (name.trim().isEmpty() || (type != "masuk" && type != "keluar")) {
            return Result.failure(IllegalArgumentException("Nama dan tipe kategori tidak valid"))
        }
        val category = Category(
            organizationId = orgId,
            nama = name.trim(),
            tipe = type
        )
        dao.insertCategory(category)
        return Result.success(category)
    }

    // --- Members ---
    fun observeMembers(orgId: String): Flow<List<Member>> = dao.observeMembers(orgId)
    suspend fun getAllMembers(orgId: String): List<Member> = dao.getAllMembers(orgId)

    suspend fun addMember(
        orgId: String,
        name: String,
        phone: String?,
        notes: String?,
        joinDate: String?
    ): Result<Member> {
        if (name.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Nama anggota wajib diisi"))
        }
        val member = Member(
            organizationId = orgId,
            nama = name.trim(),
            noHp = phone?.trim()?.ifEmpty { null },
            catatan = notes?.trim()?.ifEmpty { null },
            tanggalGabung = joinDate?.ifEmpty { todayStr() } ?: todayStr()
        )
        dao.insertMember(member)
        return Result.success(member)
    }

    suspend fun updateMember(
        orgId: String,
        memberId: String,
        name: String,
        phone: String?,
        notes: String?,
        joinDate: String?,
        active: Boolean
    ): Result<Unit> {
        val existing = dao.getMemberById(memberId)
            ?: return Result.failure(IllegalStateException("Anggota tidak ditemukan"))
        val updated = existing.copy(
            nama = name.trim(),
            noHp = phone?.trim()?.ifEmpty { null },
            catatan = notes?.trim()?.ifEmpty { null },
            tanggalGabung = joinDate?.ifEmpty { existing.tanggalGabung } ?: existing.tanggalGabung,
            aktif = active
        )
        dao.updateMember(updated)
        return Result.success(Unit)
    }

    suspend fun deleteMember(orgId: String, memberId: String): Result<Unit> {
        dao.deleteMember(memberId, orgId)
        return Result.success(Unit)
    }

    // --- Transactions (Kas) ---
    fun observeTransactions(orgId: String): Flow<List<Transaction>> =
        dao.observeTransactions(orgId)

    suspend fun getTransactionsWithDetails(orgId: String, filterTipe: String? = null): List<TransactionDetail> {
        val trxs = dao.getTransactions(orgId, filterTipe)
        val members = dao.getAllMembers(orgId).associateBy { it.id }
        val accounts = dao.getAccounts(orgId).associateBy { it.id }

        return trxs.map { t ->
            var periodes: List<String> = emptyList()
            if (t.sumber == "iuran") {
                val payment = dao.getDuesPaymentByTransactionId(t.id)
                if (payment != null) {
                    periodes = dao.getAllocationsByPaymentId(payment.id).map { it.periode }
                }
            }
            TransactionDetail(
                transaction = t,
                anggotaNama = t.anggotaId?.let { members[it]?.nama },
                akunNama = t.akunId?.let { accounts[it]?.nama },
                akunTujuanNama = t.akunTujuanId?.let { accounts[it]?.nama },
                periodeList = periodes
            )
        }
    }

    suspend fun addTransaction(
        orgId: String,
        tipe: String, // "masuk", "keluar", "transfer", "penyesuaian"
        kategori: String?,
        jumlah: Double,
        catatan: String?,
        metode: String?,
        akunId: String?,
        akunTujuanId: String?,
        tanggal: String?,
        userId: String?
    ): Result<Transaction> {
        if (tipe !in listOf("masuk", "keluar", "transfer", "penyesuaian")) {
            return Result.failure(IllegalArgumentException("Tipe transaksi tidak valid"))
        }
        if (tipe != "penyesuaian" && jumlah <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah harus lebih dari 0"))
        }
        if (tipe == "penyesuaian" && catatan.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Keterangan wajib diisi untuk penyesuaian saldo"))
        }
        if (tipe == "transfer") {
            if (akunId == null || akunTujuanId == null) {
                return Result.failure(IllegalArgumentException("Akun asal dan tujuan wajib dipilih"))
            }
            if (akunId == akunTujuanId) {
                return Result.failure(IllegalArgumentException("Akun asal dan tujuan tidak boleh sama"))
            }
        }
        val defaultAkun = akunId ?: dao.getAccounts(orgId).firstOrNull()?.id
        val transaction = Transaction(
            organizationId = orgId,
            tipe = tipe,
            sumber = "umum",
            kategori = kategori,
            jumlah = jumlah,
            catatan = catatan?.trim()?.ifEmpty { null },
            metode = metode,
            akunId = defaultAkun,
            akunTujuanId = akunTujuanId,
            tanggal = tanggal?.ifEmpty { todayStr() } ?: todayStr(),
            createdBy = userId
        )
        dao.insertTransaction(transaction)
        return Result.success(transaction)
    }

    suspend fun deleteTransaction(orgId: String, transactionId: String): Result<Unit> {
        val t = dao.getTransactionById(transactionId)
            ?: return Result.failure(IllegalStateException("Transaksi tidak ditemukan"))
        if (t.sumber == "iuran") {
            return Result.failure(IllegalStateException("Transaksi iuran tidak bisa dihapus langsung dari Kas."))
        }
        dao.deleteTransaction(transactionId, orgId)
        return Result.success(Unit)
    }

    // --- Dues & FIFO Allocation ---
    fun observeDuesSettings(orgId: String): Flow<DuesSettings?> = dao.observeDuesSettings(orgId)

    suspend fun getDuesSettings(orgId: String): DuesSettings {
        return dao.getDuesSettings(orgId) ?: DuesSettings(
            organizationId = orgId,
            namaIuran = "Iuran Bulanan",
            nominal = 0.0,
            tanggalMulai = todayStr()
        )
    }

    suspend fun updateDuesSettings(
        orgId: String,
        namaIuran: String,
        nominal: Double,
        tanggalMulai: String
    ): Result<Unit> {
        if (nominal < 0) return Result.failure(IllegalArgumentException("Nominal iuran tidak valid"))
        if (tanggalMulai.isBlank()) return Result.failure(IllegalArgumentException("Tanggal mulai iuran wajib diisi"))

        val settings = DuesSettings(
            organizationId = orgId,
            namaIuran = namaIuran.trim().ifEmpty { "Iuran Bulanan" },
            nominal = nominal,
            tanggalMulai = tanggalMulai,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdateDuesSettings(settings)
        return Result.success(Unit)
    }

    private suspend fun getAnggotaStartPeriode(orgId: String, member: Member, settings: DuesSettings): String {
        val orgStart = periodeFromDateStr(settings.tanggalMulai)
        val memberStart = periodeFromDateStr(member.tanggalGabung)
        return periodeMax(orgStart, memberStart)
    }

    private suspend fun getMemberAllocationMap(orgId: String, memberId: String): Map<String, Double> {
        val summaries = dao.getMemberAllocationMap(orgId, memberId)
        return summaries.associate { it.periode to it.total }
    }

    fun computeStatusUntuk(
        periode: String,
        startPeriode: String,
        nominal: Double,
        alokasiMap: Map<String, Double>
    ): Triple<String, Double, Double> {
        if (periode < startPeriode) {
            return Triple("tidak_dikenakan", 0.0, 0.0)
        }
        val dibayar = alokasiMap[periode] ?: 0.0
        val status = when {
            dibayar >= nominal - 0.01 -> "lunas"
            dibayar > 0 -> "sebagian"
            else -> "belum_bayar"
        }
        return Triple(status, dibayar, nominal)
    }

    fun computeLunasSampai(
        startPeriode: String,
        nominal: Double,
        alokasiMap: Map<String, Double>,
        batasPeriode: String
    ): String? {
        var p = startPeriode
        var lunasSampai: String? = null
        var guard = 0
        while (p <= batasPeriode && guard < 1200) {
            guard++
            val dibayar = alokasiMap[p] ?: 0.0
            if (dibayar >= nominal - 0.01) {
                lunasSampai = p
                p = periodAdd(p, 1)
            } else {
                break
            }
        }
        return lunasSampai
    }

    fun hitungAlokasi(
        startPeriode: String,
        nominal: Double,
        alokasiMapAwal: Map<String, Double>,
        jumlahBayar: Double
    ): List<Pair<String, Double>> {
        val map = alokasiMapAwal.toMutableMap()
        var periode = startPeriode
        var remaining = jumlahBayar
        val hasil = mutableListOf<Pair<String, Double>>()
        var guard = 0
        while (remaining > 0.009 && guard < 1200) {
            guard++
            val sudah = map[periode] ?: 0.0
            val butuh = nominal - sudah
            if (butuh <= 0.009) {
                periode = periodAdd(periode, 1)
                continue
            }
            val alokasi = minOf(butuh, remaining)
            hasil.add(Pair(periode, alokasi))
            map[periode] = sudah + alokasi
            remaining -= alokasi
            if (alokasi < butuh - 0.009) break
            periode = periodAdd(periode, 1)
        }
        return hasil
    }

    suspend fun getMonthlyDuesSummary(orgId: String, periode: String = currentPeriode()): MonthlyDuesSummary {
        val settings = getDuesSettings(orgId)
        val members = dao.getActiveMembers(orgId)
        val memberStatuses = mutableListOf<MemberDuesStatus>()

        val currPer = currentPeriode()
        val limitPeriode = if (periode > currPer) periode else currPer

        for (m in members) {
            val startPeriode = getAnggotaStartPeriode(orgId, m, settings)
            val alokasiMap = getMemberAllocationMap(orgId, m.id)
            val (status, dibayar, wajib) = computeStatusUntuk(periode, startPeriode, settings.nominal, alokasiMap)
            val lunasSampai = computeLunasSampai(startPeriode, settings.nominal, alokasiMap, limitPeriode)
            memberStatuses.add(
                MemberDuesStatus(
                    member = m,
                    status = status,
                    dibayar = dibayar,
                    wajib = wajib,
                    lunasSampai = lunasSampai
                )
            )
        }

        val lunasCount = memberStatuses.count { it.status == "lunas" }
        val sebagianCount = memberStatuses.count { it.status == "sebagian" }
        val belumCount = memberStatuses.count { it.status == "belum_bayar" }
        val terkumpul = memberStatuses.filter { it.status != "tidak_dikenakan" }.sumOf { it.dibayar }
        val tunggakan = memberStatuses.filter { it.status != "tidak_dikenakan" }.sumOf { maxOf(0.0, it.wajib - it.dibayar) }

        return MonthlyDuesSummary(
            periode = periode,
            totalAnggota = memberStatuses.size,
            lunasCount = lunasCount,
            sebagianCount = sebagianCount,
            belumBayarCount = belumCount,
            terkumpul = terkumpul,
            tunggakan = tunggakan,
            memberStatuses = memberStatuses
        )
    }

    suspend fun payDues(
        orgId: String,
        memberId: String,
        jumlah: Double,
        tanggal: String?,
        catatan: String?,
        akunId: String?,
        userId: String?
    ): Result<Pair<Transaction, List<String>>> {
        if (jumlah <= 0) return Result.failure(IllegalArgumentException("Jumlah pembayaran harus lebih dari 0"))
        val member = dao.getMemberById(memberId)
            ?: return Result.failure(IllegalStateException("Anggota tidak ditemukan"))
        val settings = getDuesSettings(orgId)
        if (settings.nominal <= 0) {
            return Result.failure(IllegalStateException("Nominal iuran belum diatur. Atur di menu Pengaturan Iuran."))
        }

        val startPeriode = getAnggotaStartPeriode(orgId, member, settings)
        val alokasiMap = getMemberAllocationMap(orgId, memberId)
        val alokasiHasil = hitungAlokasi(startPeriode, settings.nominal, alokasiMap, jumlah)

        if (alokasiHasil.isEmpty()) {
            return Result.failure(IllegalStateException("Tidak ada alokasi yang bisa dibuat (periode sudah lunas semua)"))
        }

        val tgl = tanggal?.ifEmpty { todayStr() } ?: todayStr()
        val defaultAkun = akunId ?: dao.getAccounts(orgId).firstOrNull()?.id

        val transaction = Transaction(
            organizationId = orgId,
            tipe = "masuk",
            sumber = "iuran",
            kategori = settings.namaIuran,
            jumlah = jumlah,
            catatan = catatan?.trim()?.ifEmpty { null } ?: "Iuran (${member.nama})",
            akunId = defaultAkun,
            anggotaId = memberId,
            tanggal = tgl,
            createdBy = userId
        )
        dao.insertTransaction(transaction)

        val duesPayment = DuesPayment(
            organizationId = orgId,
            anggotaId = memberId,
            jumlahTotal = jumlah,
            tanggalBayar = tgl,
            transaksiId = transaction.id,
            catatan = catatan?.trim()?.ifEmpty { null }
        )
        dao.insertDuesPayment(duesPayment)

        val allocations = alokasiHasil.map { (per, amt) ->
            DuesAllocation(
                organizationId = orgId,
                anggotaId = memberId,
                pembayaranId = duesPayment.id,
                periode = per,
                jumlah = amt
            )
        }
        dao.insertDuesAllocations(allocations)

        return Result.success(Pair(transaction, alokasiHasil.map { it.first }))
    }

    suspend fun getArrearsReport(orgId: String, periode: String = currentPeriode()): List<MemberArrears> {
        val settings = getDuesSettings(orgId)
        val members = dao.getActiveMembers(orgId)
        val arrearsList = mutableListOf<MemberArrears>()

        for (m in members) {
            val startPeriode = getAnggotaStartPeriode(orgId, m, settings)
            if (periode < startPeriode) continue
            val alokasiMap = getMemberAllocationMap(orgId, m.id)
            var totalTunggakan = 0.0
            var p = startPeriode
            var guard = 0
            while (p <= periode && guard < 1200) {
                guard++
                val dibayar = alokasiMap[p] ?: 0.0
                totalTunggakan += maxOf(0.0, settings.nominal - dibayar)
                p = periodAdd(p, 1)
            }
            if (totalTunggakan > 0.01) {
                arrearsList.add(MemberArrears(member = m, totalTunggakan = totalTunggakan))
            }
        }
        return arrearsList
    }

    // --- Reports ---
    suspend fun getMonthlyReport(orgId: String, bulan: String = currentPeriode()): MonthlyReport {
        val trxs = dao.getTransactionsByMonth(orgId, bulan)
        val members = dao.getAllMembers(orgId).associateBy { it.id }
        val accounts = dao.getAccounts(orgId).associateBy { it.id }

        val totalMasuk = trxs.filter { it.tipe == "masuk" }.sumOf { it.jumlah }
        val totalKeluar = trxs.filter { it.tipe == "keluar" }.sumOf { it.jumlah }

        val categoryMap = mutableMapOf<String, Double>()
        for (t in trxs) {
            if (t.tipe == "keluar") {
                val cat = t.kategori ?: "Lainnya"
                categoryMap[cat] = (categoryMap[cat] ?: 0.0) + t.jumlah
            }
        }

        val categoryBreakdowns = categoryMap.map { (catName, amt) ->
            val pct = if (totalKeluar > 0) (amt / totalKeluar).toFloat() else 0f
            CategoryBreakdown(catName, amt, pct)
        }.sortedByDescending { it.amount }

        val duesSummary = getMonthlyDuesSummary(orgId, bulan)

        val trxDetails = trxs.map { t ->
            var periodes: List<String> = emptyList()
            if (t.sumber == "iuran") {
                val payment = dao.getDuesPaymentByTransactionId(t.id)
                if (payment != null) {
                    periodes = dao.getAllocationsByPaymentId(payment.id).map { it.periode }
                }
            }
            TransactionDetail(
                transaction = t,
                anggotaNama = t.anggotaId?.let { members[it]?.nama },
                akunNama = t.akunId?.let { accounts[it]?.nama },
                akunTujuanNama = t.akunTujuanId?.let { accounts[it]?.nama },
                periodeList = periodes
            )
        }

        return MonthlyReport(
            bulan = bulan,
            totalMasuk = totalMasuk,
            totalKeluar = totalKeluar,
            saldoBersih = totalMasuk - totalKeluar,
            jumlahTransaksi = trxs.size,
            categoryBreakdowns = categoryBreakdowns,
            duesSummary = duesSummary,
            transactions = trxDetails
        )
    }

    suspend fun getAnnualReport(orgId: String, year: String): AnnualReport {
        val trxs = dao.getTransactionsByYear(orgId, year)
        val totalMasuk = trxs.filter { it.tipe == "masuk" }.sumOf { it.jumlah }
        val totalKeluar = trxs.filter { it.tipe == "keluar" }.sumOf { it.jumlah }

        val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
        val monthlyMap = (1..12).associateWith { Pair(0.0, 0.0) }.toMutableMap()

        for (t in trxs) {
            if (t.tipe != "masuk" && t.tipe != "keluar") continue
            val parts = t.tanggal.split("-")
            if (parts.size >= 2) {
                val m = parts[1].toIntOrNull() ?: continue
                val current = monthlyMap[m] ?: Pair(0.0, 0.0)
                if (t.tipe == "masuk") {
                    monthlyMap[m] = Pair(current.first + t.jumlah, current.second)
                } else if (t.tipe == "keluar") {
                    monthlyMap[m] = Pair(current.first, current.second + t.jumlah)
                }
            }
        }

        val trends = (1..12).map { m ->
            val (masuk, keluar) = monthlyMap[m] ?: Pair(0.0, 0.0)
            MonthTrend(
                monthNumber = m,
                monthLabel = monthLabels[m - 1],
                masuk = masuk,
                keluar = keluar
            )
        }

        return AnnualReport(
            tahun = year,
            totalMasuk = totalMasuk,
            totalKeluar = totalKeluar,
            saldoBersih = totalMasuk - totalKeluar,
            jumlahTransaksi = trxs.size,
            monthlyTrends = trends
        )
    }

    suspend fun getDashboardSummary(orgId: String): DashboardSummary {
        val currMonth = currentPeriode()
        val accounts = getAccountsWithBalances(orgId)
        val totalSaldo = accounts.sumOf { it.saldo }

        val monthTrxs = dao.getTransactionsByMonth(orgId, currMonth)
        val totalMasuk = monthTrxs.filter { it.tipe == "masuk" }.sumOf { it.jumlah }
        val totalKeluar = monthTrxs.filter { it.tipe == "keluar" }.sumOf { it.jumlah }

        val duesSummary = getMonthlyDuesSummary(orgId, currMonth)
        val recentTrxs = dao.getRecentTransactions(orgId, 5)
        val members = dao.getAllMembers(orgId).associateBy { it.id }
        val accountsMap = accounts.associateBy { it.account.id }

        val recentDetails = recentTrxs.map { t ->
            var periodes: List<String> = emptyList()
            if (t.sumber == "iuran") {
                val payment = dao.getDuesPaymentByTransactionId(t.id)
                if (payment != null) {
                    periodes = dao.getAllocationsByPaymentId(payment.id).map { it.periode }
                }
            }
            TransactionDetail(
                transaction = t,
                anggotaNama = t.anggotaId?.let { members[it]?.nama },
                akunNama = t.akunId?.let { accountsMap[it]?.account?.nama },
                akunTujuanNama = t.akunTujuanId?.let { accountsMap[it]?.account?.nama },
                periodeList = periodes
            )
        }

        return DashboardSummary(
            totalSaldo = totalSaldo,
            pemasukanBulanIni = totalMasuk,
            pengeluaranBulanIni = totalKeluar,
            duesSummary = duesSummary,
            recentTransactions = recentDetails
        )
    }

    suspend fun getReceiptData(orgId: String, transactionId: String): ReceiptData? {
        val t = dao.getTransactionById(transactionId) ?: return null
        val org = dao.getOrganizationById(orgId)
        val member = t.anggotaId?.let { dao.getMemberById(it) }
        val account = t.akunId?.let { dao.getAccountById(it) }

        var periodes: List<String> = emptyList()
        if (t.sumber == "iuran") {
            val payment = dao.getDuesPaymentByTransactionId(t.id)
            if (payment != null) {
                periodes = dao.getAllocationsByPaymentId(payment.id).map { it.periode }
            }
        }

        return ReceiptData(
            id = t.id,
            organisasiNama = org?.nama ?: "Kas Kita",
            tipe = t.tipe,
            kategori = t.kategori,
            jumlah = t.jumlah,
            tanggal = t.tanggal,
            anggotaNama = member?.nama,
            periodeList = periodes,
            catatan = t.catatan,
            akunNama = account?.nama,
            metode = t.metode
        )
    }
}
