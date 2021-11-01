package com.example.volleylibrary

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.example.volleylibrary.utils.VolleyUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject


class VolleyRequestClass(mutableLiveData: MutableLiveData<HashMap<K, K>>? = null, volleyNetworkCallBack: VolleyNetworkCallBack? = null)
{
    private var mVolleyNetworkCallBack: VolleyNetworkCallBack? = volleyNetworkCallBack
    private val MY_SOCKET_TIMEOUT_MS: Int = 20000
    private var jsonLiveData: MutableLiveData<HashMap<K, K>>? = null

    init
    {
        jsonLiveData = mutableLiveData
    }


    // Interface
    interface VolleyNetworkCallBack
    {
        fun notifySuccessFully(dataHashMap: HashMap<K, K>)
        fun notifyFailed(code: String?, errorMessage: String?)
    }

    fun sendGETStringRequest(context: Context, method: Int, url: String, token: String? = null)
    {
        VolleySingleton.getInstance(context).requestQueue

        val headers = HashMap<String, String>()

        if(token != null)
        {
            headers["Authorization"] = "Bearer $token"
        }

        // Formulate the request and handle the response.
        val stringRequest = object : StringRequest(method, url,
            { response ->

                val dataHashMap = HashMap<K, K>()
                dataHashMap["response"] = response


                // Do something with the response
//                    mVolleyNetworkCallBack.notifySuccessFully(dataHashMap)

                jsonLiveData?.value = dataHashMap
            },
            { error ->
                // Handle error

                if (error is TimeoutError || error is NoConnectionError)
                {

                    MaterialAlertDialogBuilder(context)
                        .setMessage(context.getString(R.string.time_out_either_network_error))
                        .setPositiveButton(context.getString(android.R.string.ok)) { dialog, which ->
                        }
                        .show()

                } else if (error is AuthFailureError)
                {
                    // Error indicating that there was an Authentication Failure while performing the request

                } else if (error is ServerError)
                {
                    //Indicates that the server responded with a error response

                } else if (error is NetworkError)
                {
                    //Indicates that there was network error while performing the request

                } else if (error is ParseError)
                {
                    // Indicates that the server response could not be parsed

                }

            })

        {
            override fun getHeaders(): MutableMap<String, String>
            {
                return headers
            }
        }



        // Add the request to the RequestQueue.
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest)

        VolleyUtils.retry(stringRequest, MY_SOCKET_TIMEOUT_MS)
    }


    fun sendJSONRequest(context: Context, method: Int, url: String, jsonObject: JSONObject, token: String? = null)
    {
        VolleySingleton.getInstance(context).requestQueue

        // Formulate the request and handle the response.

        val headers = HashMap<String, String>()
        headers["Content-Type"] = "application/json"
        headers["Accept"] = "application/json"

        if(token != null)
        {
            headers["Authorization"] = "Bearer $token"
        }

        Log.d("Method: ", method.toString())
        Log.d("URL: ", url)
        Log.d("JSON: ", jsonObject.toString())

        val request = object : JsonObjectRequest(method, url, jsonObject,
            { response ->

                val dataHashMap = HashMap<K, K>()
                dataHashMap["response"] = response
                dataHashMap["method"] = method
                dataHashMap["isError"] = false

                jsonLiveData?.value = dataHashMap

            }, { error ->
                // Error in request

                val dataHashMap = HashMap<K, K>()
                dataHashMap["isError"] = true

                jsonLiveData?.value = dataHashMap

                var errorMessage = ""
                if (error is TimeoutError || error is NoConnectionError)
                {
                    //This indicates that the reuest has either time out or there is no connection
                    errorMessage = error.toString()

                } else if (error is AuthFailureError)
                {
                    // Error indicating that there was an Authentication Failure while performing the request
                    errorMessage = error.toString()

                } else if (error is ServerError)
                {
                    //Indicates that the server responded with a error response
                    errorMessage = error.toString()

                } else if (error is NetworkError)
                {
                    //Indicates that there was network error while performing the request
                    errorMessage = error.toString()

                } else if (error is ParseError)
                {
                    // Indicates that the server response could not be parsed
                    errorMessage = error.toString()

                }

                MaterialAlertDialogBuilder(context)
                    .setMessage(errorMessage)
                    .setPositiveButton(context.getString(android.R.string.ok)) { dialog, which ->
                    }
                    .show()

            })
        {
            override fun getHeaders(): MutableMap<String, String>
            {
                return headers
            }
        }

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance(context).addToRequestQueue(request)

        VolleyUtils.retry(request, MY_SOCKET_TIMEOUT_MS)
    }
}

typealias K = Any?