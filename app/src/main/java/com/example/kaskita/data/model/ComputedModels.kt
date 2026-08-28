package com.example.kaskita.data.model

data class AccountWithBalance(
    val account: Account,
    val saldo: Double
)

data class TransactionDetail(
    val transaction: Transaction,
    val anggotaNama: String? = null,
    val akunNama: String? = null,
    val akunTujuanNama: String? = null,
    val periodeList: List<String> = emptyList()
)

data class MemberDuesStatus(
    val member: Member,
    val status: String, // "lunas", "sebagian", "belum_bayar", "tidak_dikenakan"
    val dibayar: Double,
    val wajib: Double,
    val lunasSampai: String? = null
)

data class MonthlyDuesSummary(
    val periode: String,
    val totalAnggota: Int,
    val lunasCount: Int,
    val sebagianCount: Int,
    val belumBayarCount: Int,
    val terkumpul: Double,
    val tunggakan: Double,
    val memberStatuses: List<MemberDuesStatus>
)

data class MemberArrears(
    val member: Member,
    val totalTunggakan: Double
)

data class CategoryBreakdown(
    val categoryName: String,
    val amount: Double,
    val percentage: Float
)

data class MonthlyReport(
    val bulan: String, // YYYY-MM
    val totalMasuk: Double,
    val totalKeluar: Double,
    val saldoBersih: Double,
    val jumlahTransaksi: Int,
    val categoryBreakdowns: List<CategoryBreakdown>,
    val duesSummary: MonthlyDuesSummary,
    val transactions: List<TransactionDetail>
)

data class MonthTrend(
    val monthNumber: Int,
    val monthLabel: String,
    val masuk: Double,
    val keluar: Double
)

data class AnnualReport(
    val tahun: String, // YYYY
    val totalMasuk: Double,
    val totalKeluar: Double,
    val saldoBersih: Double,
    val jumlahTransaksi: Int,
    val monthlyTrends: List<MonthTrend>
)

data class DashboardSummary(
    val totalSaldo: Double,
    val pemasukanBulanIni: Double,
    val pengeluaranBulanIni: Double,
    val duesSummary: MonthlyDuesSummary,
    val recentTransactions: List<TransactionDetail>
)

data class ReceiptData(
    val id: String,
    val organisasiNama: String,
    val tipe: String,
    val kategori: String?,
    val jumlah: Double,
    val tanggal: String,
    val anggotaNama: String?,
    val periodeList: List<String>,
    val catatan: String?,
    val akunNama: String?,
    val metode: String?
)
