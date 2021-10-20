package com.example.sevg

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.example.sevg.viewOnClickInterface.ViewOnClickListener
import com.example.sevg.databinding.InventoryItemDetailsBinding
import com.example.sevg.module.InventoryDetailsObj
import com.example.sevg.room.database.AppDatabase
import com.example.sevg.room.model.AddLineItemColumns
import com.example.sevg.room.model.InwardOutwardColumns
import com.example.sevg.room.utlis.DatabaseUtils
import com.example.sevg.utils.PreferencesUtils
import com.example.sevg.utils.ServerUtils
import com.example.sevg.viewModel.ViewModelClass
import com.example.volleylibrary.K
import com.example.volleylibrary.VolleyRequestClass
import com.example.volleylibrary.utils.GsonUtils
import kotlinx.android.synthetic.main.sales_details_activity.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class InventoryItemDetailsActivity: AppCompatActivity(), VolleyRequestClass.VolleyNetworkCallBack, ViewOnClickListener
{
    private lateinit var mVolleyRequestClass: VolleyRequestClass
    private lateinit var binding: InventoryItemDetailsBinding

    private var mDb: AppDatabase? = null
    private var inventoryDetailsViewModel: ViewModelClass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.inventory_item_details)

        updateDefaultDisplay()
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
                mDb?.inwardOutwardDao()?.clearInwardOutwardTable()
                finish()
                return true
            }
            R.id.delete ->
            {

                val inwardOutwardDetailsObj = InwardOutwardColumns()
                inwardOutwardDetailsObj.inwardOutwardColumns(binding.inwardOutwardDetails!!)

                mDb?.inwardOutwardDao()?.getInwardOutwardDetails()?.observe(this, { inwardOutwardDetails->

                    inwardOutwardDetailsObj.id = inwardOutwardDetails.id
                })

                mDb?.addLineItemDao()?.getAllAddLineItem()?.observe(this, { addLineItemObj->

                    inwardOutwardDetailsObj.addLineItem = addLineItemObj as ArrayList<AddLineItemColumns>?

                    inwardOutwardDetailsObj.dateTime = ServerUtils.getServerDateFormatted(inwardOutwardDetailsObj.dateTime)

                    val json = inwardOutwardDetailsObj.constructJson()

                    inventoryDetailsViewModel?.submitRequestDetails(this, url = getString(R.string.inward_outward_delete_url) +"/"+ inwardOutwardDetailsObj.id, method = Request.Method.DELETE, token = PreferencesUtils.getOauthToken(this))

                })

                return true
            }
            R.id.edit ->
            {
                val salesIntent = Intent(this, SalesAndInventoryActivity::class.java)
                salesIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                salesIntent.putExtra("inward_outward_id", binding.inwardOutwardDetails!!.id)
                startActivity(salesIntent)
                return true
            }
            else -> return false
        }

    }

    private fun updateDefaultDisplay()
    {
        val viewProvider = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())

        inventoryDetailsViewModel = viewProvider.get(ViewModelClass::class.java)

        setUpVolleyLib()

        setUpRoomDB()

        setUpStatusBar()

        setUpToolbar()

        updateDisplay()

        resultUpdate()
    }

    private fun resultUpdate()
    {
        inventoryDetailsViewModel?.jsonViewModel?.observe(this, {

            if(it["method"] == 3)
            {
                val jsonString = it["response"].toString()
                val inventoryDetails = GsonUtils.getJSONStringFromGSON(jsonString, InventoryDetailsObj::class.java).inventoryDetails

                inventoryDetails?.apply {
                    val inwardOutwardDetails = InwardOutwardColumns()

                    inwardOutwardDetails.id = id
                    inwardOutwardDetails.currentBranchid = currentBranchid
                    inwardOutwardDetails.inventoryType = inventoryType
                    inwardOutwardDetails.billNo = billNo
                    inwardOutwardDetails.dateTime = dateTime

                    mDb?.inwardOutwardDao()?.delete(inwardOutwardDetails)

                    inventoryItemsList?.forEach { addLineItemColumn ->
                        mDb?.addLineItemDao()?.delete(addLineItemColumn)
                    }
                }
            }
        })
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
        val toolbar = findViewById<Toolbar>(R.id.inventory_details_toolbar)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material)
        upArrow?.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_ATOP
        )
        supportActionBar?.setHomeAsUpIndicator(upArrow)

        supportActionBar?.title = getString(R.string.inventory_details)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpRoomDB()
    {
        mDb = DatabaseUtils.getDBInstances(this)
    }

    private fun updateDisplay()
    {
        mDb?.inwardOutwardDao()?.getInwardOutwardDetails()?.observe(this, { inwardOutwardDetailsObj->

            inwardOutwardDetailsObj?.let {

                it.dateTime = ServerUtils.getDateFormatted(it.dateTime!!.toLong())

                binding.inwardOutwardDetails = it
            }

        })

        mDb?.addLineItemDao()?.getAllAddLineItem()?.observe(this, { addLineItemObj->

            tableBody.removeAllViews()

            addLineItemObj?.forEach {
                val inflatedLayout: View =
                    layoutInflater.inflate(R.layout.addlineitem_details_body_items, root_view as ViewGroup?, false)

                val itemName = inflatedLayout.findViewById<TextView>(R.id.itemName)
                val rateQuantityLayout = inflatedLayout.findViewById<LinearLayout>(R.id.cal_rate_layout)
                val amount = inflatedLayout.findViewById<TextView>(R.id.amount)

                itemName.text = it.itemName
                rateQuantityLayout.visibility = View.GONE
                amount.text = (it.quantity).toString()

                tableBody?.addView(inflatedLayout)
            }

        })
    }

    private fun setUpVolleyLib()
    {
        mVolleyRequestClass = VolleyRequestClass(volleyNetworkCallBack = this)
    }


    override fun notifySuccessFully(dataHashMap: HashMap<K, K>)
    {
        finish()
    }

    override fun notifyFailed(code: String?, errorMessage: String?)
    {
        TODO("Not yet implemented")
    }

    override fun onViewClick(v: View)
    {
        mDb?.inwardOutwardDao()?.clearInwardOutwardTable()
        val salesIntent = Intent(this, SalesAndInventoryActivity::class.java)
        salesIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(salesIntent)
    }
}