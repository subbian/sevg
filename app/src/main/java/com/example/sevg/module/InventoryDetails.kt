package com.example.sevg.module

import com.example.sevg.room.model.AddLineItemColumns
import org.json.JSONArray
import org.json.JSONObject

class InventoryDetails
{
    var id = 0
    var currentBranchid = 0
    var inventoryType = 0
    var billNo: String? = null
    var dateTime: String? = null
    var inventoryItemsList: ArrayList<AddLineItemColumns>? = null


    fun inventoryDetailsColumns(inventoryDetails: InventoryDetails)
    {
        id = inventoryDetails.id
        currentBranchid = inventoryDetails.currentBranchid
        inventoryType = inventoryDetails.inventoryType
        billNo = inventoryDetails.billNo
        dateTime = inventoryDetails.dateTime
        inventoryItemsList = inventoryDetails.inventoryItemsList
    }

    fun constructJson(): JSONObject
    {
        val inventoryJson = JSONObject()
        val addLineItemArray = JSONArray()
        inventoryJson.put("id", id)
        inventoryJson.put("currentBranchid", currentBranchid)
        inventoryJson.put("inventoryType", inventoryType)
        inventoryJson.put("billNo", billNo)
        inventoryJson.put("dateTime", dateTime)

        inventoryItemsList?.forEach {
            addLineItemArray.put(it.constructJson())
        }

        inventoryJson.put("addLineItem", addLineItemArray)

        return inventoryJson
    }
}