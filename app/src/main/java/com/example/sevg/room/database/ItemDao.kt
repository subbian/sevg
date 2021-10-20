package com.example.sevg.room.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sevg.room.model.ItemColumns

@Dao interface ItemDao
{
    @Query("SELECT * FROM ITEMS ORDER BY itemname ASC")
    fun getAllItemList(): List<ItemColumns>?

    @Query("SELECT * FROM ITEMS WHERE itemmaingroupid = :id ORDER BY itemname ASC")
    fun getItemListByMGID(id: Int): LiveData<List<ItemColumns>?>

    @Query("SELECT * FROM ITEMS WHERE itemgroupid = :id ORDER BY itemname ASC")
    fun getItemListBySGID(id: Int): LiveData<List<ItemColumns>?>

    @Query("DELETE FROM ITEMS")
    fun clearItemTable()

    @Insert fun insertItemDetails(userDetails: ItemColumns?)
    @Update fun updateItemDetails(userDetails: ItemColumns?)
    @Delete fun delete(userDetails: ItemColumns?)
}