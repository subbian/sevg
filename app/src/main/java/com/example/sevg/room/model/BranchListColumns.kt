package com.example.sevg.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "branch_list")
class BranchListColumns: Serializable
{
    @PrimaryKey
    var sl = 0
    var active: String? = null
    var branchName: String? = null
    var id: Int = 0
    var createDate: String? = null
    var updateDate: String? = null
}