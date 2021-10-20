package com.example.volleylibrary.utils

import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request

object VolleyUtils
{
    fun <T> retry(request: Request<T>, socketTiming: Int)
    {
        request.retryPolicy = DefaultRetryPolicy(
            socketTiming,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
    }
}