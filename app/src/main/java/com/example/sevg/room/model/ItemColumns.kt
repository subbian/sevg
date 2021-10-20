package com.example.sevg.room.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "items")
class ItemColumns : Serializable
{
    @PrimaryKey(autoGenerate = true)
    var sl = 0
    var id = 0
    var itemmaingroupid = 0
    var itemgroupid = 0
    var itembranchid = 0
    var itemname: String? = null

    @Ignore fun ItemColumns(id: Int, maingroupid: Int, subgroupid: Int, itemname: String)
    {
        this.id = id
        this.itemmaingroupid = maingroupid
        this.itemname = itemname
    }

    fun ItemColumns(id: Int, maingroupid: Int, subgroupid: Int, itemgroupid: Int, itemname: String)
    {
        this.id = id
        this.itemmaingroupid = maingroupid
        this.itemgroupid = itemgroupid
        this.itemname = itemname
    }
}