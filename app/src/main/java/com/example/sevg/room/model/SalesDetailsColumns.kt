package com.example.sevg.room.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


@Entity(tableName = "sales_details")
class SalesDetailsColumns
{
    @PrimaryKey(autoGenerate = true)
    var sl = 0
    var id = 0
    var userId = 0
    var branchId = 0
    var customerName: String? = null
    var customerMobileNo: String? = null
    var dateofSales: Long? = null
    var loadingCharges: String? = null
    var transportCharges: String? = null
    var totalAmount: String? = null
    var invoiceNumber: String? = null
}