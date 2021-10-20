package com.example.sevg.room.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sevg.room.model.MainGroupColumns


@Dao interface MainGroupDao
{
    @Query("SELECT * FROM MAIN_GROUP ORDER BY groupname ASC")
    fun getMainGroupList(): LiveData<List<MainGroupColumns>?>

    @Query("DELETE FROM MAIN_GROUP")
    fun clearMainGroupTable()

    @Insert fun insertMainGroupDetails(userDetails: MainGroupColumns?)
    @Update fun updateMainGroupDetails(userDetails: MainGroupColumns?)
    @Delete fun delete(userDetails: MainGroupColumns?)
}