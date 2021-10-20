package com.example.sevg.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout


object ViewUtils
{
    fun TextInputLayout.markRequiredInRed()
    {
        hint = buildSpannedString {
            append(hint)
            color(Color.RED) { append(" *") } // Mind the space prefix.
        }
    }

    fun hideKeyboardFrom(context: Activity, view: View)
    {
        view.windowToken?.let {
            val imm: InputMethodManager =
                context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it, 0)
        }
    }

    fun closeKeyboardFromFragment(activity: Activity, fragment: Fragment)
    {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        val view = fragment.view?.rootView?.windowToken

//        if (view == null)
//        {
//            view = fragment.view?.rootView?.windowToken as View
//        }

        // hide the keyboard
        imm.hideSoftInputFromWindow(view, 0)
    }
}