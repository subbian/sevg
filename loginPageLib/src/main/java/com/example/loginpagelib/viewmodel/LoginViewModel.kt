package com.example.loginpagelib.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.loginpagelib.repository.LoginRepository
import com.example.volleylibrary.K
import org.json.JSONObject

class LoginViewModel : ViewModel()
{
    var jsonViewModel: MutableLiveData<HashMap<K, K>> = MutableLiveData()

    lateinit var loginRepository: LoginRepository

    fun submitLoginDetails(context: Context, jsonBody: JSONObject, url: String, method: Int)
    {
        loginRepository = LoginRepository(jsonViewModel)

        loginRepository.submitLoginDetails(context, jsonBody, url, method)
    }
}