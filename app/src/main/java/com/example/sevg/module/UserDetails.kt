package com.example.sevg.module

import java.io.Serializable

class UserDetails : Serializable
{
    var id: Int?= null
    var branchid: Int?= null
    var email: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var active: Boolean? = null
    var mobileNumber: String? = null
    var fullName: String? = null
    var isInventoryEnabled: Boolean? = true
    var role: UserRole? = null
}