package com.example.loginpagelib.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.volleylibrary.K
import com.example.volleylibrary.VolleyRequestClass
import org.json.JSONObject

class LoginRepository(private val jsonViewModel: MutableLiveData<HashMap<K, K>>)
{

    private lateinit var mVolleyRequestClass: VolleyRequestClass

    private fun setUpVolleyLib()
    {
        mVolleyRequestClass = VolleyRequestClass(jsonViewModel)
    }

    fun submitLoginDetails(context: Context, jsonBody: JSONObject, url: String, method: Int)
    {
        setUpVolleyLib()

        mVolleyRequestClass.sendJSONRequest(context, method, url, jsonBody)
    }
}