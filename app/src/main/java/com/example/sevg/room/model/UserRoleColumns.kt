package com.example.sevg.room.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "user_role")
class UserRoleColumns
{
    @PrimaryKey(autoGenerate = true)
    var sl = 0
    var id = 0
    var role: String? = null

    @Ignore fun UserRoleColumns(role: String)
    {
        this.role = role
    }

    fun UserRoleColumns(id: Int, role: String)
    {
        this.id = id
        this.role = role
    }
}