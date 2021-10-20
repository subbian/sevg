package com.example.sevg.room.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.example.sevg.room.model.SalesDetailsColumns

@Dao interface SalesDetailsDao
{
    @Query("SELECT * FROM SALES_DETAILS")
    fun getSalesDetails(): LiveData<SalesDetailsColumns?>

    @Query("SELECT * FROM SALES_DETAILS WHERE id = :id")
    fun getSalesDetailsByID(id: Int): LiveData<SalesDetailsColumns?>

    @Query("DELETE FROM SALES_DETAILS")
    fun clearSalesDetails()

    @Insert fun insertSalesDetails(salesDetails: SalesDetailsColumns?)
    @Update fun updateSalesDetails(salesDetails: SalesDetailsColumns?)
    @Delete fun delete(salesDetails: SalesDetailsColumns?)
}