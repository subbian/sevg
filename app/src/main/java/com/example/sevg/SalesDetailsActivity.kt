package com.example.sevg

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.android.volley.Request
import com.example.sevg.databinding.SalesDetailsActivityBinding
import com.example.sevg.generated.callback.OnClickListener
import com.example.sevg.module.SalesDetailsScreenPOJO
import com.example.sevg.room.database.AppDatabase
import com.example.sevg.room.database.AppExecutors
import com.example.sevg.room.utlis.DatabaseUtils
import com.example.sevg.utils.PreferencesUtils
import com.example.sevg.viewOnClickInterface.ViewOnClickListener
import com.example.volleylibrary.K
import com.example.volleylibrary.VolleyRequestClass
import kotlinx.android.synthetic.main.sales_details_activity.*
import kotlinx.android.synthetic.main.sales_details_activity.root_view
import kotlinx.android.synthetic.main.sales_fragment.*
import java.util.*
import kotlin.collections.HashMap


class SalesDetailsActivity : AppCompatActivity(), VolleyRequestClass.VolleyNetworkCallBack,ViewOnClickListener
{
    private lateinit var mVolleyRequestClass: VolleyRequestClass
    private lateinit var onClickInterface: ViewOnClickListener
    private lateinit var binding: SalesDetailsActivityBinding

    private var mDb: AppDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.sales_details_activity)
//        setContentView(R.layout.sales_details_activity)

        updateDefaultDisplay()
    }


    private fun startIntent()
    {
        clearAddLineItemTable()
        val salesIntent = Intent(this, SalesAndInventoryActivity::class.java)
        salesIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(salesIntent)
    }
    private fun updateDefaultDisplay()
    {
        setUpVolleyLib()

        setUpRoomDB()

        setUpStatusBar()

        setUpToolbar()

        updateDisplay()

    }

    private fun setUpStatusBar()
    {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_blue)
    }

    private fun setUpToolbar()
    {
        setSupportActionBar(binding.salesDetailsToolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material)
        upArrow?.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_ATOP
        )
        supportActionBar?.setHomeAsUpIndicator(upArrow)

        supportActionBar?.title = getString(R.string.sales_details)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun setUpVolleyLib()
    {
        mVolleyRequestClass = VolleyRequestClass(volleyNetworkCallBack = this)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.sales_details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId)
        {
            android.R.id.home ->
            {
                finishActivity()
                return true
            }
            R.id.delete ->
            {

                val salesDetailsObj = SalesDetailsScreenPOJO()

                binding.salesDetails?.apply {

                    salesDetailsObj.id = id
                    salesDetailsObj.userId = userId
                    salesDetailsObj.customerName = customerName
                    salesDetailsObj.customerMobileNo = customerMobileNo
                    salesDetailsObj.dateofSales = dateofSales
                    salesDetailsObj.loadingCharges = loadingCharges
                    salesDetailsObj.transportCharges = transportCharges
                    salesDetailsObj.invoiceNumber = invoiceNumber
                    salesDetailsObj.totalAmount = totalAmount

                    salesDetailsObj.salesDetailsColumns(salesDetailsObj)
                    val json = salesDetailsObj.constructJson()

                    mVolleyRequestClass.sendGETStringRequest(this@SalesDetailsActivity,
                    Request.Method.DELETE, getString(R.string.sales_details_delete_url) + "/" + salesDetailsObj.id,
                    PreferencesUtils.getOauthToken(this@SalesDetailsActivity))

                }

                return true
            }
            R.id.edit ->
            {
                val salesIntent = Intent(this, SalesAndInventoryActivity::class.java)
                salesIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                salesIntent.putExtra("sales_id", binding.salesDetails!!.id)
                startActivity(salesIntent)
                return true
            }
            else -> return false
        }

    }

    private fun setUpRoomDB()
    {
        mDb = DatabaseUtils.getDBInstances(this)
    }

    private fun updateDisplay()
    {
        mDb?.salesDetailsDao()?.getSalesDetails()?.observe(this, Observer { salesDetailsObj->

            salesDetailsObj?.apply {
                val salesDetailsScreenPOJO = SalesDetailsScreenPOJO()
                salesDetailsScreenPOJO.id = id
                salesDetailsScreenPOJO.userId = userId
                salesDetailsScreenPOJO.branchId = branchId
                salesDetailsScreenPOJO.customerName = customerName
                salesDetailsScreenPOJO.customerMobileNo = customerMobileNo
                salesDetailsScreenPOJO.dateofSales = dateofSales
                salesDetailsScreenPOJO.loadingCharges = loadingCharges
                salesDetailsScreenPOJO.transportCharges = transportCharges
                salesDetailsScreenPOJO.totalAmount= totalAmount
                salesDetailsScreenPOJO.invoiceNumber= invoiceNumber

                binding.salesDetails = salesDetailsScreenPOJO
            }

            binding.onClickInterface = this

        })

        mDb?.addLineItemDao()?.getAllAddLineItem()?.observe(this, { addLineItemObj->

            tableBody.removeAllViews()

            addLineItemObj?.forEach {
                val inflatedLayout: View =
                    layoutInflater.inflate(R.layout.addlineitem_details_body_items, root_view as ViewGroup?, false)

                val itemName = inflatedLayout.findViewById<TextView>(R.id.itemName)
                val quantity = inflatedLayout.findViewById<TextView>(R.id.quantity)
                val rate = inflatedLayout.findViewById<TextView>(R.id.rate)
                val amount = inflatedLayout.findViewById<TextView>(R.id.amount)

                itemName.text = it.itemName
                quantity.text = it.quantity.toString()
                rate.text = it.sellingPrice.toString()
                amount.text = (it.sellingPrice?.toInt()!! * it.quantity).toString()

                tableBody?.addView(inflatedLayout)
            }

        })
    }

    override fun notifySuccessFully(dataHashMap: HashMap<K, K>)
    {
        finish()
    }

    override fun notifyFailed(code: String?, errorMessage: String?)
    {

    }

    override fun onViewClick(v: View)
    {
        finishActivity()
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

    private fun finishActivity()
    {
        clearAddLineItemTable()
        clearSalesDetailsTable()
        val salesIntent = Intent(this, SalesAndInventoryActivity::class.java)
        salesIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(salesIntent)
    }
}