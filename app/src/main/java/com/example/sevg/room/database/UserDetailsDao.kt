package com.example.sevg.room.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sevg.room.model.UserDetailsColumns

@Dao interface UserDetailsDao
{
    @Query("SELECT * FROM USER_DETAILS")
    fun getUserDetails(): LiveData<UserDetailsColumns?>

    @Query("DELETE FROM USER_DETAILS")
    fun clearUserDetailsTable()

    @Insert fun insertUserDetails(userDetails: UserDetailsColumns?)
    @Update fun updateUserDetails(userDetails: UserDetailsColumns?)
    @Delete fun delete(userDetails: UserDetailsColumns?)
}