package com.example.sevg.room.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sevg.room.model.SubGroupColumns

@Dao interface SubGroupDao
{
//    @Query("SELECT * FROM SUB_GROUP ORDER BY groupname ASC")
//    fun getAllSubGroupList(): List<SubGroupColumns>?

    @Query("SELECT * FROM SUB_GROUP WHERE maingroupid = :id ORDER BY groupname ASC")
    fun getSubGroupListByID(id: Int): LiveData<List<SubGroupColumns>?>

    @Query("DELETE FROM SUB_GROUP")
    fun clearSubGroupTable()

    @Insert fun insertSubGroupDetails(userDetails: SubGroupColumns?)
    @Update fun updateSubGroupDetails(userDetails: SubGroupColumns?)
    @Delete fun delete(userDetails: SubGroupColumns?)
}