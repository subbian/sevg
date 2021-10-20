package com.example.sevg.module

import com.example.sevg.room.model.ItemColumns
import com.example.sevg.room.model.MainGroupColumns
import com.example.sevg.room.model.SubGroupColumns
import java.io.Serializable


class SalesProductDetails : Serializable
{
    var mainGroup: ArrayList<MainGroupColumns>?= null
    var subMainGroup: ArrayList<SubGroupColumns>?= null
    var itemDetails: ArrayList<ItemColumns>?= null
}