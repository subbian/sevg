package com.example.sevg.utils

import java.text.SimpleDateFormat
import java.util.*

object ServerUtils
{
    fun getServerDateFormatted(date: Int, month: Int, year: Int): Date?
    {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.parse("${date}/${month}/${year}")
    }

    fun getServerDateFormatted(date: String?): String
    {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.parse(date!!)

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
        return formatter.format(sdf.parse(date)!!)
    }

    fun getDateFormatted(date: Long): String
    {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date(date))
    }
}