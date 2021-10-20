package com.example.sevg.room.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sevg.room.model.*

@Database(entities = [UserDetailsColumns::class, UserRoleColumns::class, MainGroupColumns::class, SubGroupColumns::class, ItemColumns::class, SalesDetailsColumns::class, InwardOutwardColumns::class, AddLineItemColumns::class, BranchListColumns::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase()
{
    abstract fun userDetailsDao(): UserDetailsDao?
    abstract fun userRoleDao(): UserRoleDao?
    abstract fun mainGroupDao(): MainGroupDao?
    abstract fun subGroupDao(): SubGroupDao?
    abstract fun itemDao(): ItemDao?
    abstract fun salesDetailsDao(): SalesDetailsDao?
    abstract fun inwardOutwardDao(): InwardOutwardDao?
    abstract fun addLineItemDao(): AddLineItemDao?
    abstract fun branchListDao(): BranchListDao?

    companion object
    {
        private val LOG_TAG = AppDatabase::class.java.simpleName
        private val LOCK = Any()
        private const val DATABASE_NAME = "sev_db"
        private var sInstance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase?
        {
            if (sInstance == null)
            {
                synchronized(LOCK) {
                    Log.d(LOG_TAG, "Creating new database instance")
                    sInstance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, DATABASE_NAME
                    )
                        .build()
                }
            }
            Log.d(LOG_TAG, "Getting the database instance")
            return sInstance
        }
    }
}