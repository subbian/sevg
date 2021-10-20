package com.example.sevg.viewModel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sevg.module.ResponseHandler
import com.example.sevg.repository.RepositoryClass
import com.example.volleylibrary.K
import org.json.JSONObject

class ViewModelClass: ViewModel()
{
    var jsonViewModel: MutableLiveData<HashMap<K, K>> = MutableLiveData()

    lateinit var repositoryClass: RepositoryClass

    fun submitRequestDetails(context: Context, jsonBody: JSONObject? = null, url: String, method: Int, token: String?)
    {
        repositoryClass = RepositoryClass(jsonViewModel)

        repositoryClass.submitRequestDetails(context, jsonBody, url, method, token!!)
    }
}