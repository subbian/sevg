package com.example.sevg.utils

import android.app.Activity
import android.content.Context

object PreferencesUtils
{
    var OAUTH_TOKEN = "oauth_token"
    var USER_PREF = "user_pref"

    fun getOauthToken(activity: Activity): String?
    {
        return activity.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
            .getString(OAUTH_TOKEN, "")
    }

    fun setOauthToken(activity: Activity, token: String)
    {
        val sharedPref = activity.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(OAUTH_TOKEN, token)
            commit()
        }
    }

    fun clearAllSharedPreferences(activity: Activity)
    {
        val sharedPref = activity.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            clear()
            commit()
        }
    }
}