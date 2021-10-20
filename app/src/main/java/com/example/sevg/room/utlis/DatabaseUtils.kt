package com.example.sevg.room.utlis

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.example.sevg.room.database.AppDatabase
import com.example.sevg.room.model.BranchListColumns
import com.example.sevg.room.model.UserDetailsColumns

object DatabaseUtils
{
    private var mDb: AppDatabase? = null

    fun getDBInstances(activity: Activity): AppDatabase?
    {
        if(mDb == null)
        {
            mDb = AppDatabase.getInstance(activity.applicationContext)
        }

        return mDb
    }
}