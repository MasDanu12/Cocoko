package com.example.kaskita.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kaskita.data.model.Account
import com.example.kaskita.data.model.Category
import com.example.kaskita.data.model.DuesAllocation
import com.example.kaskita.data.model.DuesPayment
import com.example.kaskita.data.model.DuesSettings
import com.example.kaskita.data.model.Member
import com.example.kaskita.data.model.Organization
import com.example.kaskita.data.model.OrganizationMember
import com.example.kaskita.data.model.Transaction
import com.example.kaskita.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface KasKitaDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    // --- Organizations ---
    @Query("SELECT o.* FROM organizations o INNER JOIN organization_members m ON m.organizationId = o.id WHERE m.userId = :userId ORDER BY o.createdAt DESC")
    fun observeUserOrganizations(userId: String): Flow<List<Organization>>

    @Query("SELECT o.* FROM organizations o INNER JOIN organization_members m ON m.organizationId = o.id WHERE m.userId = :userId ORDER BY o.createdAt DESC")
    suspend fun getUserOrganizations(userId: String): List<Organization>

    @Query("SELECT * FROM organizations WHERE id = :id LIMIT 1")
    suspend fun getOrganizationById(id: String): Organization?

    @Query("SELECT * FROM organizations WHERE UPPER(inviteCode) = UPPER(:inviteCode) LIMIT 1")
    suspend fun getOrganizationByInviteCode(inviteCode: String): Organization?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(organization: Organization)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganizationMember(member: OrganizationMember)

    @Query("SELECT * FROM organization_members WHERE userId = :userId AND organizationId = :orgId LIMIT 1")
    suspend fun getOrgMembership(userId: String, orgId: String): OrganizationMember?

    // --- Accounts (Akun) ---
    @Query("SELECT * FROM accounts WHERE organizationId = :orgId AND aktif = 1 ORDER BY createdAt ASC")
    fun observeAccounts(orgId: String): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE organizationId = :orgId AND aktif = 1 ORDER BY createdAt ASC")
    suspend fun getAccounts(orgId: String): List<Account>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): Account?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    // --- Categories (Kategori) ---
    @Query("SELECT * FROM categories WHERE organizationId = :orgId ORDER BY tipe ASC, nama ASC")
    fun observeCategories(orgId: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE organizationId = :orgId ORDER BY tipe ASC, nama ASC")
    suspend fun getCategories(orgId: String): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("DELETE FROM categories WHERE id = :id AND organizationId = :orgId")
    suspend fun deleteCategory(id: String, orgId: String)

    // --- Members (Anggota) ---
    @Query("SELECT * FROM members WHERE organizationId = :orgId ORDER BY nama ASC")
    fun observeMembers(orgId: String): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE organizationId = :orgId AND aktif = 1 ORDER BY nama ASC")
    suspend fun getActiveMembers(orgId: String): List<Member>

    @Query("SELECT * FROM members WHERE organizationId = :orgId ORDER BY nama ASC")
    suspend fun getAllMembers(orgId: String): List<Member>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: String): Member?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member)

    @Update
    suspend fun updateMember(member: Member)

    @Query("DELETE FROM members WHERE id = :id AND organizationId = :orgId")
    suspend fun deleteMember(id: String, orgId: String)

    // --- Transactions (Kas) ---
    @Query("SELECT * FROM transactions WHERE organizationId = :orgId ORDER BY tanggal DESC, createdAt DESC")
    fun observeTransactions(orgId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE organizationId = :orgId AND (:filterTipe IS NULL OR tipe = :filterTipe) ORDER BY tanggal DESC, createdAt DESC LIMIT 500")
    suspend fun getTransactions(orgId: String, filterTipe: String? = null): List<Transaction>

    @Query("SELECT * FROM transactions WHERE organizationId = :orgId AND SUBSTR(tanggal, 1, 7) = :month ORDER BY tanggal DESC, createdAt DESC")
    suspend fun getTransactionsByMonth(orgId: String, month: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE organizationId = :orgId AND SUBSTR(tanggal, 1, 4) = :year ORDER BY tanggal ASC")
    suspend fun getTransactionsByYear(orgId: String, year: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE organizationId = :orgId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentTransactions(orgId: String, limit: Int = 5): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id AND organizationId = :orgId")
    suspend fun deleteTransaction(id: String, orgId: String)

    // --- Dues Settings ---
    @Query("SELECT * FROM dues_settings WHERE organizationId = :orgId LIMIT 1")
    fun observeDuesSettings(orgId: String): Flow<DuesSettings?>

    @Query("SELECT * FROM dues_settings WHERE organizationId = :orgId LIMIT 1")
    suspend fun getDuesSettings(orgId: String): DuesSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDuesSettings(settings: DuesSettings)

    // --- Dues Payments & Allocations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuesPayment(payment: DuesPayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuesAllocations(allocations: List<DuesAllocation>)

    @Query("SELECT * FROM dues_payments WHERE transaksiId = :transaksiId LIMIT 1")
    suspend fun getDuesPaymentByTransactionId(transaksiId: String): DuesPayment?

    @Query("SELECT * FROM dues_allocations WHERE pembayaranId = :pembayaranId ORDER BY periode ASC")
    suspend fun getAllocationsByPaymentId(pembayaranId: String): List<DuesAllocation>

    @Query("SELECT * FROM dues_allocations WHERE organizationId = :orgId AND anggotaId = :anggotaId")
    suspend fun getAllocationsForMember(orgId: String, anggotaId: String): List<DuesAllocation>

    @Query("SELECT periode, SUM(jumlah) as total FROM dues_allocations WHERE organizationId = :orgId AND anggotaId = :anggotaId GROUP BY periode")
    suspend fun getMemberAllocationMap(orgId: String, anggotaId: String): List<AllocationSummary>
}

data class AllocationSummary(
    val periode: String,
    val total: Double
)
