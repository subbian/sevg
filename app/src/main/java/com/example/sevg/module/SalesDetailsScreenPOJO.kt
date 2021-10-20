package com.example.sevg.module

import com.example.sevg.room.model.AddLineItemColumns
import com.example.sevg.utils.ServerUtils
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList

class SalesDetailsScreenPOJO
{
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
    var addLineItem: ArrayList<AddLineItemColumns>? = null

    fun salesDetailsColumns(salesDetails: SalesDetailsScreenPOJO)
    {
        id = salesDetails.id
        userId = salesDetails.userId
        branchId = salesDetails.branchId
        customerName = salesDetails.customerName
        customerMobileNo = salesDetails.customerMobileNo
        dateofSales = salesDetails.dateofSales
        loadingCharges = salesDetails.loadingCharges
        transportCharges = salesDetails.transportCharges
        totalAmount = salesDetails.totalAmount
        invoiceNumber = salesDetails.invoiceNumber
        addLineItem = salesDetails.addLineItem
    }

    fun constructJson(): JSONObject
    {
        val salesJson = JSONObject()
        val addLineItemArray = JSONArray()
        salesJson.put("id", id)
        salesJson.put("userId", userId)
        salesJson.put("branchId", branchId)
        salesJson.put("customerName", customerName)
        salesJson.put("customerMobileNo", customerMobileNo)
        salesJson.put("dateofSales", dateofSales)
        salesJson.put("loadingCharges", loadingCharges)
        salesJson.put("transportCharges", transportCharges)
        salesJson.put("totalAmount", totalAmount)

        addLineItem?.forEach {
            addLineItemArray.put(it.constructJson())
        }

        salesJson.put("addLineItem", addLineItemArray)

        return salesJson
    }

    fun getDateFormatted(): String
    {
        dateofSales?.let {
            ServerUtils.getDateFormatted(it)
        }

        return ""
    }
}