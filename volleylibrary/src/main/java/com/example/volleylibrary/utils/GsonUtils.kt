package com.example.volleylibrary.utils

import com.google.gson.Gson

object GsonUtils
{
    fun <T> getJSONStringFromGSON(jsonString: String, moduleClass: Class<T>): T
    {
        return Gson().fromJson(jsonString, moduleClass)
    }
}