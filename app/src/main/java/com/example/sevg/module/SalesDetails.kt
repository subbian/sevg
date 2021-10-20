package com.example.sevg.module

import com.example.sevg.room.model.BranchListColumns
import java.io.Serializable

class SalesDetails: Serializable
{
    var token: String?= null
    var user: UserDetails?= null
    var productDetails: SalesProductDetails?= null
    var branchList: ArrayList<BranchListColumns>?= null
}