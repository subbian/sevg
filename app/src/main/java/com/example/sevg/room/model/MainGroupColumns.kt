package com.example.sevg.room.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "main_group")
class MainGroupColumns : Serializable
{
    @PrimaryKey(autoGenerate = true)
    var sl = 0
    var id = 0
    var groupname: String? = null

    @Ignore fun MainGroupColumns(groupname: String)
    {
        this.groupname = groupname
    }

    fun MainGroupColumns(id: Int, groupname: String)
    {
        this.id = id
        this.groupname = groupname
    }
}