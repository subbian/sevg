package com.example.loginpagelib

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.example.loginpagelib.viewmodel.LoginViewModel
import com.example.volleylibrary.K
import kotlinx.android.synthetic.main.login_page.*
import org.json.JSONObject


class LoginMainActivity(private val mActivity: Activity,
                        loginNetworkCallBack: LoginNetworkCallBack, savedInstanceState: Bundle?, viewModelProvider: ViewModelProvider, lifecycle: LifecycleOwner)
{
    private var mLoginNetworkCallBack: LoginNetworkCallBack = loginNetworkCallBack
    private var mSavedInstanceState: Bundle? = savedInstanceState

    private var loginViewModel: LoginViewModel? = null

    init
    {
        loginViewModel = viewModelProvider.get(LoginViewModel::class.java)

        mActivity.setContentView(R.layout.login_page)

        updateDefaultDisplay()

        loginViewModel?.jsonViewModel?.observe(lifecycle, {

            val dataHashMap = HashMap<K, K>()
            dataHashMap["response"] = it["response"]
            dataHashMap["method"] = it["method"]

            mActivity.linear_progressbar?.progress = 100
            mLoginNetworkCallBack.loginSuccessful(dataHashMap)
            mActivity.linear_progressbar?.postDelayed({
            mActivity.progressbar_overview_layout?.visibility = View.GONE
            mActivity.progressbar_layout?.visibility = View.GONE
                mActivity.finish()
            }, 2000)
        })
    }

    private fun setUpStatusBarColor()
    {
        val window = mActivity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(mActivity, R.color.dark_blue)
    }

    // Interface
    interface LoginNetworkCallBack
    {
        fun loginSuccessful(dataHashMap: HashMap<K, K>)
        fun loginFailed()
    }

    private fun updateDefaultDisplay()
    {
        setUpStatusBarColor()

        setOnClickListener()

        if(mSavedInstanceState != null)
        {
            updateDisplay()
        }
    }

    fun updateSavedInstanceStateObj(): Bundle
    {
        val bundle = Bundle()

        if(mActivity.userName?.text != null && mActivity.user_password?.text != null)
        {
            bundle.putString("userName", mActivity.userName?.text.toString())
            bundle.putString("userPassword", mActivity.user_password?.text.toString())
        }
        else if(mActivity.userName?.text != null)
        {
            bundle.putString("userName", mActivity.userName?.text.toString())
        }
        else if(mActivity.user_password?.text != null)
        {
            bundle.putString("userPassword", mActivity.user_password?.text.toString())
        }

        return bundle
    }


    private fun updateDisplay()
    {
        mSavedInstanceState?.let {savedInstanceStateObj->
            val bundle = savedInstanceStateObj.getBundle("LoginDetailBundle")

            bundle?.let {bundleObj->
                mActivity.userName?.setText(bundleObj.get("userName").toString())
                mActivity.user_password?.setText(bundleObj.get("userPassword").toString())
            }

        }
    }


    private fun setOnClickListener()
    {
        mActivity.fab?.setOnClickListener {

//             if(emailValidator())
//             {
//
            mActivity.progressbar_overview_layout?.visibility = View.VISIBLE
            mActivity.progressbar_layout?.visibility = View.VISIBLE
            mActivity.linear_progressbar?.postDelayed({
                for(i in 1..10)
                {
                mActivity.linear_progressbar?.progress = i} }, 1)

            val userName = mActivity.userName?.text.toString()
            val password = mActivity.user_password?.text.toString()
            
            val jsonBody = JSONObject()
            jsonBody.put("email", "admin@gmail.com")
            jsonBody.put("password", "123456")

            loginViewModel?.submitLoginDetails(mActivity, jsonBody, mActivity.getString(R.string.login_url), Request.Method.POST)

//            mVolleyRequestClass.sendJSONRequest(
//                mActivity,
//                Request.Method.POST, mActivity.getString(R.string.login_url),
//                jsonBody
//            )



        }

        mActivity.userName?.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if (mActivity.userName_layout?.isErrorEnabled == true)
                {
                    mActivity.userName_layout?.isErrorEnabled = false
                }
            }
        })

        mActivity.user_password?.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if (mActivity.password_layout?.isErrorEnabled == true)
                {
                    mActivity.password_layout?.isErrorEnabled = false
                }
            }
        })
    }

    private fun emailValidator(): Boolean
    {
        // extract the entered data from the EditText
        val emailID = mActivity.userName?.text.toString()
        val password = mActivity.user_password?.text.toString()

        // Android offers the inbuilt patterns which the entered
        // data from the EditText field needs to be compared with
        // In this case the the entered data needs to compared with
        // the EMAIL_ADDRESS, which is implemented same below
        return if (password.isEmpty() || emailID.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(
                emailID
            ).matches()
        )
        {
            if (password.isEmpty() && emailID.isEmpty() || password.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(
                    emailID
                ).matches()
            )
            {
                mActivity.userName_layout?.isErrorEnabled = true
                mActivity.userName_layout?.error = "Enter valid Email address"

                mActivity.password_layout?.isErrorEnabled = true
                mActivity.password_layout?.error = "Enter valid password"
            }
            else if (emailID.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailID).matches())
            {
                mActivity.userName_layout?.isErrorEnabled = true
                mActivity.userName_layout?.error = "Enter valid Email address"
            }
            else
            {
                mActivity.password_layout?.isErrorEnabled = true
                mActivity.password_layout?.error = "Enter valid password"
            }

            false
        }
        else
        {
            true
        }
    }

//    override fun notifySuccessFully(dataHashMap: HashMap<K, K>)
//    {
//        mActivity.linear_progressbar?.progress = 100
//        mLoginNetworkCallBack.loginSuccessful(dataHashMap)
//        mActivity.linear_progressbar?.postDelayed({
//            mActivity.progressbar_overview_layout?.visibility = View.GONE
//            mActivity.progressbar_layout?.visibility = View.GONE
//            mActivity.finish()}, 2000)
//    }
//
//    override fun notifyFailed(code: String?, errorMessage: String?)
//    {
//        mActivity.progressbar_overview_layout?.visibility = View.GONE
//        mActivity.progressbar_layout?.visibility = View.GONE
//    }
//
//    override fun getTokenValue(): String?
//    {
//        return null
//    }

}