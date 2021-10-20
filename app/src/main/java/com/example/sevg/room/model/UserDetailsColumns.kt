package com.example.sevg.room.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "user_details")
class UserDetailsColumns
{
    @PrimaryKey(autoGenerate = true)
    var sl = 0
    var id = 0
    var branchid = 0
    var email: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var mobileNumber: String? = null
    var active: Boolean? = null
    var fullName: String? = null
    var isInventoryEnabled: Boolean? = true


    @Ignore fun UserDetailsColumns(fullName: String, email: String?, mobileNumber: String, firstName: String, lastName: String, active: Boolean, isInventoryEnabled: Boolean)
    {
        this.fullName = fullName
        this.email = email
        this.firstName = firstName
        this.lastName = lastName
        this.mobileNumber = mobileNumber
        this.active = active
        this.isInventoryEnabled = isInventoryEnabled
    }

    fun UserDetailsColumns(id: Int, fullName: String, email: String?, mobileNumber: String, firstName: String, lastName: String, active: Boolean, isInventoryEnabled: Boolean)
    {
        this.id = id
        this.fullName = fullName
        this.email = email
        this.firstName = firstName
        this.lastName = lastName
        this.mobileNumber = mobileNumber
        this.active = active
        this.isInventoryEnabled = isInventoryEnabled
    }
}