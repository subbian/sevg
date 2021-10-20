package com.example.sevg.room.database

import androidx.room.*
import com.example.sevg.room.model.UserRoleColumns

@Dao
interface UserRoleDao
{
    @Query("SELECT * FROM USER_ROLE")
    fun getUserRole(): UserRoleColumns?

    @Query("DELETE FROM USER_ROLE")
    fun clearUserRoleTable()

    @Insert fun insertUserRole(userRole: UserRoleColumns?)

    @Update fun updateUserRole(userRole: UserRoleColumns?)

    @Delete fun delete(userRole: UserRoleColumns?)
}