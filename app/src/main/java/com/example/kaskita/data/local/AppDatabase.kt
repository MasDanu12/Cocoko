package com.example.kaskita.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        User::class,
        Organization::class,
        OrganizationMember::class,
        Account::class,
        Category::class,
        Member::class,
        Transaction::class,
        DuesSettings::class,
        DuesPayment::class,
        DuesAllocation::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kasKitaDao(): KasKitaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kaskita_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
