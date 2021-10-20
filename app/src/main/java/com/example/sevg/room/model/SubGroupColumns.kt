package com.example.sevg.room.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "sub_group")
class SubGroupColumns : Serializable
{
    @PrimaryKey(autoGenerate = true)
    var sl = 0
    var id = 0
    var maingroupid = 0
    var groupname: String? = null

    @Ignore fun MainGroupColumns(id: Int, groupname: String)
    {
        this.id = id
        this.groupname = groupname
    }

    fun MainGroupColumns(id: Int, maingroupid: Int, groupname: String)
    {
        this.id = id
        this.maingroupid = maingroupid
        this.groupname = groupname
    }
}