package com.example.sevg.room.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sevg.room.model.AddLineItemColumns

@Dao interface AddLineItemDao
{
    @Query("SELECT * FROM ADD_LINE_ITEM ORDER BY sl ASC")
    fun getAllAddLineItem(): LiveData<List<AddLineItemColumns>?>

    @Query("SELECT * FROM ADD_LINE_ITEM ORDER BY sl ASC")
    fun getListOfAddLineItem(): List<AddLineItemColumns>?

    @Query("SELECT * FROM ADD_LINE_ITEM WHERE sl = :id")
    fun getAddLineItemByID(id: Int): LiveData<AddLineItemColumns?>

    @Query("DELETE FROM ADD_LINE_ITEM")
    fun clearAddLineItem()

    @Query("DELETE FROM ADD_LINE_ITEM WHERE sl = :id")
    fun clearAddLineItemByID(id: Int)

    @Insert fun insertAddLineItem(userDetails: AddLineItemColumns?)
    @Update fun updateAddLineItem(userDetails: AddLineItemColumns?)
    @Delete fun delete(addLineItem: AddLineItemColumns?)
}