package com.example.sevg

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.loginpagelib.LoginMainActivity
import com.example.sevg.module.SalesDetails
import com.example.sevg.module.UserDetails
import com.example.sevg.module.UserRole
import com.example.sevg.room.database.AppDatabase
import com.example.sevg.room.database.AppExecutors
import com.example.sevg.room.model.*
import com.example.sevg.room.utlis.DatabaseUtils
import com.example.sevg.utils.PreferencesUtils
import com.example.volleylibrary.K
import com.example.volleylibrary.utils.GsonUtils


class MainActivity : AppCompatActivity(), LoginMainActivity.LoginNetworkCallBack
{
    lateinit var mLoginMainActivity: LoginMainActivity
    private var mDb: AppDatabase? = null


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setUpRoomDB()

        if(PreferencesUtils.getOauthToken(this).isNullOrEmpty())
        {
            val viewProvider = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            mLoginMainActivity = LoginMainActivity(this, this, savedInstanceState, viewProvider, this)
        }
        else
        {
            fetchUserDataFromDB()
        }
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        if(::mLoginMainActivity.isInitialized)
        {
            outState.putBundle(
                "LoginDetailBundle",
                mLoginMainActivity.updateSavedInstanceStateObj()
            )
        }

        super.onSaveInstanceState(outState)
    }

    override fun loginSuccessful(dataHashMap: HashMap<K, K>)
    {
        val jsonString = dataHashMap["response"].toString()
        val productDetails = GsonUtils.getJSONStringFromGSON(jsonString, SalesDetails::class.java)

        /*
        updating the SharedPreferences with toke value
        */
        PreferencesUtils.setOauthToken(this, productDetails.token!!)

        insertBranchList(productDetails.branchList!!)
        insertUserDetails(productDetails.user!!)
        insertUserRoleDetails(productDetails.user!!.role!!)
        insertMainGroupDetails(productDetails.productDetails!!.mainGroup!!)
        insertSubGroupDetails(productDetails.productDetails!!.subMainGroup!!)
        insertItemDetails(productDetails.productDetails!!.itemDetails!!)

        val salesIntent = Intent(this, SalesAndInventoryActivity::class.java)
        salesIntent.putExtra("productDetails", productDetails)
        startActivity(salesIntent)
    }

    override fun loginFailed()
    {

    }

    private fun fetchUserDataFromDB()
    {
        clearAddLineItemTable()
        clearSalesDetailsTable()

        val salesIntent = Intent(this, SalesAndInventoryActivity::class.java)
        startActivity(salesIntent)
        this.finish()
    }

    private fun setUpRoomDB()
    {
        mDb = DatabaseUtils.getDBInstances(this)
    }

    private fun insertUserDetails(userRemoteDetails: UserDetails)
    {
        val userDetailsObj = UserDetailsColumns()

        userRemoteDetails.apply {
            userDetailsObj.id = this.id!!
            userDetailsObj.email = this.email
            userDetailsObj.firstName = this.firstName
            userDetailsObj.lastName = this.lastName
            userDetailsObj.active = this.active
            userDetailsObj.mobileNumber = this.mobileNumber
            userDetailsObj.fullName = this.fullName
            userDetailsObj.branchid = this.branchid!!
            userDetailsObj.isInventoryEnabled = this.isInventoryEnabled
        }
        AppExecutors.instance?.diskIO()?.execute {
            mDb!!.userDetailsDao()!!.insertUserDetails(userDetailsObj)
        }
    }

    private fun insertBranchList(branchListDetails: ArrayList<BranchListColumns>)
    {
        AppExecutors.instance?.diskIO()?.execute {
            mDb!!.branchListDao()!!.insertBranchListDetails(branchListDetails)
        }
    }

    private fun insertUserRoleDetails(userRemoteRoleDetails: UserRole)
    {
        val userRoleDetailsObj = UserRoleColumns()

        userRemoteRoleDetails.apply {
            userRoleDetailsObj.id = this.id
            userRoleDetailsObj.role = this.role
        }

        AppExecutors.instance?.diskIO()?.execute {
            mDb!!.userRoleDao()!!.insertUserRole(userRoleDetailsObj)
        }
    }

    private fun insertMainGroupDetails(mainGroupDetails: ArrayList<MainGroupColumns>)
    {
        mainGroupDetails.forEach {
            AppExecutors.instance?.diskIO()?.execute {
                mDb!!.mainGroupDao()!!.insertMainGroupDetails(it)
            }
        }
    }

    private fun insertSubGroupDetails(subGroupDetails: ArrayList<SubGroupColumns>)
    {
        subGroupDetails.forEach {
            AppExecutors.instance?.diskIO()?.execute {
                mDb!!.subGroupDao()!!.insertSubGroupDetails(it)
            }
        }
    }

    private fun insertItemDetails(itemDetails: ArrayList<ItemColumns>)
    {
        itemDetails.forEach {
            AppExecutors.instance?.diskIO()?.execute {
                mDb!!.itemDao()!!.insertItemDetails(it)
            }
        }

    }


    private fun clearAddLineItemTable()
    {
        AppExecutors.instance?.diskIO()?.execute {
            mDb?.addLineItemDao()?.clearAddLineItem()
        }
    }


    private fun clearSalesDetailsTable()
    {
        AppExecutors.instance?.diskIO()?.execute {
            mDb?.salesDetailsDao()?.clearSalesDetails()
        }
    }
}