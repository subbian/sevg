package com.example.sevg.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.sevg.module.ResponseHandler
import com.example.volleylibrary.K
import com.example.volleylibrary.VolleyRequestClass
import org.json.JSONObject

class RepositoryClass(private val jsonViewModel: MutableLiveData<HashMap<K, K>>)
{
    private lateinit var mVolleyRequestClass: VolleyRequestClass

    private fun setUpVolleyLib()
    {
        mVolleyRequestClass = VolleyRequestClass(jsonViewModel)
    }

    fun submitRequestDetails(context: Context, jsonBody: JSONObject? = null, url: String, method: Int, token: String)
    {
        setUpVolleyLib()

        if(jsonBody != null)
        {
            mVolleyRequestClass.sendJSONRequest(context, method, url, jsonBody, token)
        }
        else
        {
            mVolleyRequestClass.sendGETStringRequest(context, method, url, token)
        }
    }
}