package com.example.sevg.room.model

import androidx.lifecycle.LiveData
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "inward_outward_creation")
class InwardOutwardColumns
{
    @PrimaryKey(autoGenerate = true)
    var sl = 0
    var id = 0
    var currentBranchid = 0
    var inventoryType = 0
    var billNo: String? = null
    var dateTime: String? = null

    @Ignore var addLineItem: ArrayList<AddLineItemColumns>? = null

    @Ignore fun inwardOutwardColumns(inwardOutwardDetails: InwardOutwardColumns)
    {
        id = inwardOutwardDetails.id
        currentBranchid = inwardOutwardDetails.currentBranchid
        inventoryType = inwardOutwardDetails.inventoryType
        billNo = inwardOutwardDetails.billNo
        dateTime = inwardOutwardDetails.dateTime
        addLineItem = inwardOutwardDetails.addLineItem
    }

    @Ignore fun constructJson(): JSONObject
    {
        val inwardOutwardJson = JSONObject()
        val addLineItemArray = JSONArray()
        inwardOutwardJson.put("id", id)
        inwardOutwardJson.put("currentBranchid", currentBranchid)
        inwardOutwardJson.put("inventoryType", inventoryType)
        inwardOutwardJson.put("billNo", billNo)
        inwardOutwardJson.put("dateTime", dateTime)

        addLineItem?.forEach {
            addLineItemArray.put(it.constructJson())
        }


        inwardOutwardJson.put("inventoryItemsList", addLineItemArray)

        return inwardOutwardJson
    }
}