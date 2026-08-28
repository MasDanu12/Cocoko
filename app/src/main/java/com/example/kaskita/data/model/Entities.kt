package com.example.kaskita.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val email: String,
    val passwordHash: String,
    val passwordSalt: String,
    val nama: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "organizations")
data class Organization(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nama: String,
    val inviteCode: String,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "organization_members",
    foreignKeys = [
        ForeignKey(
            entity = Organization::class,
            parentColumns = ["id"],
            childColumns = ["organizationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("organizationId"), Index("userId")]
)
data class OrganizationMember(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val organizationId: String,
    val role: String = "member",
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = Organization::class,
            parentColumns = ["id"],
            childColumns = ["organizationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("organizationId")]
)
data class Account(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val nama: String,
    val saldoAwal: Double = 0.0,
    val aktif: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = Organization::class,
            parentColumns = ["id"],
            childColumns = ["organizationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("organizationId")]
)
data class Category(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val nama: String,
    val tipe: String // "masuk" or "keluar"
)

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = Organization::class,
            parentColumns = ["id"],
            childColumns = ["organizationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("organizationId")]
)
data class Member(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val nama: String,
    val noHp: String? = null,
    val catatan: String? = null,
    val tanggalGabung: String, // YYYY-MM-DD
    val aktif: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Organization::class,
            parentColumns = ["id"],
            childColumns = ["organizationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("organizationId"), Index("tanggal"), Index("akunId"), Index("anggotaId")]
)
data class Transaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val tipe: String, // "masuk", "keluar", "transfer", "penyesuaian"
    val sumber: String = "umum", // "umum", "iuran"
    val kategori: String? = null,
    val jumlah: Double,
    val catatan: String? = null,
    val metode: String? = null, // "Tunai", "Transfer Bank", "E-Wallet"
    val akunId: String? = null,
    val akunTujuanId: String? = null,
    val anggotaId: String? = null,
    val tanggal: String, // YYYY-MM-DD
    val createdBy: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "dues_settings",
    foreignKeys = [
        ForeignKey(
            entity = Organization::class,
            parentColumns = ["id"],
            childColumns = ["organizationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DuesSettings(
    @PrimaryKey val organizationId: String,
    val namaIuran: String = "Iuran Bulanan",
    val nominal: Double = 0.0,
    val tanggalMulai: String, // YYYY-MM-DD
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "dues_payments",
    foreignKeys = [
        ForeignKey(
            entity = Organization::class,
            parentColumns = ["id"],
            childColumns = ["organizationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["anggotaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("organizationId"), Index("anggotaId"), Index("transaksiId")]
)
data class DuesPayment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val anggotaId: String,
    val jumlahTotal: Double,
    val tanggalBayar: String, // YYYY-MM-DD
    val transaksiId: String,
    val catatan: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "dues_allocations",
    foreignKeys = [
        ForeignKey(
            entity = Organization::class,
            parentColumns = ["id"],
            childColumns = ["organizationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DuesPayment::class,
            parentColumns = ["id"],
            childColumns = ["pembayaranId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("organizationId"), Index("anggotaId"), Index("pembayaranId"), Index("periode")]
)
data class DuesAllocation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val anggotaId: String,
    val pembayaranId: String,
    val periode: String, // YYYY-MM
    val jumlah: Double
)
