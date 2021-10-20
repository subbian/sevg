package com.example.sevg.room.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sevg.room.model.InwardOutwardColumns

@Dao interface InwardOutwardDao
{
    @Query("SELECT * FROM INWARD_OUTWARD_CREATION")
    fun getInwardOutwardDetails(): LiveData<InwardOutwardColumns>?

    @Query("DELETE FROM INWARD_OUTWARD_CREATION")
    fun clearInwardOutwardTable()

    @Insert fun insertInwardOutwardDetails(inwardOutwardDetails: InwardOutwardColumns?)
    @Update fun updateInwardOutwardDetails(inwardOutwardDetails: InwardOutwardColumns?)
    @Delete fun delete(inwardOutwardDetails: InwardOutwardColumns?)
}