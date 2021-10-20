package com.example.sevg.room.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "add_line_item")
class AddLineItemColumns
{
    @PrimaryKey(autoGenerate = true)
    var sl = 0
    var id = 0
    var quantity = 0
    var productGroupId = 0
    var productsubGroupId = 0
    var itemId = 0
    var salesID = 0
    var inventoryID = 0
    var sellingPrice: String? = null
    var mainGroupName: String? = null
    var subGroupName: String? = null
    var itemName: String? = null
    var itemTotalAmount: String? = null
    var type: Int? = null

    @Ignore fun addLineItemColumns(addLineItemObj: AddLineItemColumns)
    {
        id = addLineItemObj.id
        quantity = addLineItemObj.quantity
        productGroupId = addLineItemObj.productGroupId
        productsubGroupId = addLineItemObj.productsubGroupId
        itemId = addLineItemObj.itemId
        sellingPrice = addLineItemObj.sellingPrice
        mainGroupName = addLineItemObj.mainGroupName
        subGroupName = addLineItemObj.subGroupName
        itemName = addLineItemObj.itemName
    }

    @Ignore fun constructJson(): JSONObject
    {
        val salesJson = JSONObject()
        salesJson.put("id", id)
        salesJson.put("quantity", quantity)
        salesJson.put("productGroupId", productGroupId)
        salesJson.put("productsubGroupId", productsubGroupId)
        salesJson.put("itemId", itemId)
        salesJson.put("sellingPrice", sellingPrice)
        salesJson.put("mainGroupName", mainGroupName)
        salesJson.put("subGroupName", subGroupName)
        salesJson.put("itemName", itemName)
        salesJson.put("itemTotalAmount", itemTotalAmount)

        if(salesID != 0)
        {
            salesJson.put("salesID", salesID)
        }

        if(inventoryID != 0)
        {
            salesJson.put("inventoryID", inventoryID)
        }

        return salesJson
    }
}