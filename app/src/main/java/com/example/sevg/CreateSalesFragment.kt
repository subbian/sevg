package com.example.sevg

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.example.sevg.constant.IntConstant
import com.example.sevg.module.SalesDetailsResponds
import com.example.sevg.module.SalesDetailsScreenPOJO
import com.example.sevg.room.database.AppExecutors
import com.example.sevg.room.model.*
import com.example.sevg.swipeToDismiss.SwipeDismissTouchListener
import com.example.sevg.swipeToDismiss.SwipeDismissTouchListener.DismissCallbacks
import com.example.sevg.utils.PreferencesUtils
import com.example.sevg.utils.ServerUtils
import com.example.sevg.utils.ViewUtils
import com.example.sevg.utils.ViewUtils.markRequiredInRed
import com.example.sevg.viewModel.ViewModelClass
import com.example.volleylibrary.K
import com.example.volleylibrary.VolleyRequestClass
import com.example.volleylibrary.utils.GsonUtils
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.add_line_activity.*
import kotlinx.android.synthetic.main.inward_items.*
import kotlinx.android.synthetic.main.sales_fragment.*
import kotlinx.android.synthetic.main.sales_fragment.addLineItemView
import kotlinx.android.synthetic.main.sales_fragment.addLineItem_bt
import kotlinx.android.synthetic.main.sales_fragment.addLineItem_error
import kotlinx.android.synthetic.main.sales_fragment.date_picker
import kotlinx.android.synthetic.main.sales_fragment.root_view
import kotlinx.android.synthetic.main.sales_fragment.submit
import org.json.JSONObject
import java.lang.NumberFormatException
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CreateSalesFragment : Fragment(), VolleyRequestClass.VolleyNetworkCallBack
{
    private lateinit var mActivity: SalesAndInventoryActivity
    private lateinit var mVolleyRequestClass: VolleyRequestClass

    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDate: Int = 0


    private var userDetails: UserDetailsColumns? = null
    private var salesDetailsObj: SalesDetailsScreenPOJO? = null
    private var salesID: Int? = null

    private var toolbar: Toolbar? = null

    private var createSalesViewModel: ViewModelClass? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.sales_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        updateDefaultDisplay()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        val toolbarMenu = toolbar?.menu

        if (toolbarMenu?.children?.count() ?: 0 < 1)
        {
            inflater.inflate(R.menu.toolbar_menu, toolbarMenu)
        }

        super.onCreateOptionsMenu(toolbarMenu!!, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {

        if (item.itemId == R.id.logout)
        {
            mActivity.clearAllTables()
            PreferencesUtils.clearAllSharedPreferences(mActivity)

            val loginIntent = Intent(mActivity, MainActivity::class.java)
            startActivity(loginIntent)

            mActivity.finish()
            return true
        }

        return false
    }


    private fun updateDefaultDisplay()
    {
        mActivity = activity as SalesAndInventoryActivity

        salesDetailsObj = SalesDetailsScreenPOJO()

        val viewProvider = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())

        createSalesViewModel = viewProvider.get(ViewModelClass::class.java)

        setUpToolbar()

        setUpVolleyLib()

        getIntentValues()

        setUpMandatoryFields()

        setUpDate()

        setUpOnClickListener()

        updateDisplay()

        resultUpdate()
    }

    private fun setUpToolbar()
    {
        mActivity.run {
            toolbar = mActivity.findViewById<Toolbar>(R.id.sales_toolbar)
            setSupportActionBar(toolbar)

            supportActionBar?.title = getString(R.string.sales)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun updateDisplay()
    {

        mActivity.mDb!!.userDetailsDao()!!.getUserDetails()
            .observe(this, { userDetailsObj ->

                userDetails = userDetailsObj

                userName?.let {
                    it.setText(userDetailsObj?.fullName)
                    it.isEnabled = false
                }
            })

        if (salesID != null)
        {
            mActivity.mDb?.salesDetailsDao()?.getSalesDetails()
                ?.observe(this, { salesDetailsColumnsObj ->

                    salesDetailsColumnsObj?.apply {

                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val date: Date? = formatter.parse(Date(dateofSales!!).toString())

                        this@CreateSalesFragment.customerName?.setText(customerName)
                        this@CreateSalesFragment.customerPhoneNo?.setText(customerMobileNo)
                        this@CreateSalesFragment.total_amount?.text = total_amount.toString()

                        loading_charges?.setText(loadingCharges)
                        transport_charge?.setText(transportCharges)
                        date_picker?.text = date.toString()
                    }
                })

            mActivity.mDb?.addLineItemDao()?.getAllAddLineItem()
                ?.observe(this, { addLineItemObj ->

                    addLineItemObj?.let {
                        try
                        {
                            constructAddLineItemView()
                        }
                        catch (e: NumberFormatException)
                        {

                        }
                    }
                })
        }
    }

    private fun setUpMandatoryFields()
    {
        customerName_layout?.markRequiredInRed()
        customerPhoneNo_layout?.markRequiredInRed()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun constructAddLineItemView()
    {
        mActivity.mDb?.addLineItemDao()?.getAllAddLineItem()
            ?.observe(this, { lineItemList ->

                if (lineItemList != null)
                {
                    addLineItemView?.removeAllViews()
                    addLineItem_error?.visibility = View.INVISIBLE

                    salesDetailsObj?.addLineItem = ArrayList()
                    salesDetailsObj?.addLineItem?.addAll(lineItemList)

                    var touchListener: SwipeDismissTouchListener? = null

                    for (addLineItemObj in lineItemList)
                    {
                        val inflatedLayout: View = mActivity.layoutInflater.inflate(R.layout.add_line_item_details, root_view as ViewGroup?, false)

                        val itemName = inflatedLayout.findViewById<TextView>(R.id.itemName)
                        val quantity = inflatedLayout.findViewById<TextView>(R.id.quantity)
                        val rate = inflatedLayout.findViewById<TextView>(R.id.rate)
                        val totalAmount =
                            inflatedLayout.findViewById<TextView>(R.id.lineItem_total_amount)

                        var inflatedViewParam: ViewGroup.LayoutParams? = null
                        var viewHeight = 0

                        inflatedLayout.id = addLineItemObj.sl
                        itemName?.text = addLineItemObj.itemName
                        quantity?.text = addLineItemObj.quantity.toString()
                        rate?.text = addLineItemObj.sellingPrice
                        totalAmount?.text =
                            ((BigDecimal(addLineItemObj.quantity * addLineItemObj.sellingPrice?.toDouble()!!).toBigInteger()).toString())

                        val dismissTouchListener = object : DismissCallbacks
                        {
                            override fun canDismiss(token: Any?): Boolean
                            {
                                return true
                            }

                            override fun onDismiss(view: View?, token: Any?)
                            {
                                MaterialAlertDialogBuilder(mActivity)
                                    .setMessage(getString(R.string.addLineItem_delete))
                                    .setNegativeButton(getString(R.string.no)) { _, _ ->

                                        touchListener?.resetTheDeletedView(inflatedViewParam, viewHeight, view!!)

                                    }
                                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                        clearAddLineItemTableByID(inflatedLayout.id)

                                        for(i in 0 until salesDetailsObj?.addLineItem?.size!!)
                                        {
                                            if(salesDetailsObj?.addLineItem?.get(i)?.sl == inflatedLayout.id)
                                            {
                                                salesDetailsObj?.addLineItem?.removeAt(i)

                                                break
                                            }
                                        }

                                        totalAmountSumUp()
                                    }
                                    .show()
                            }

                            override fun shareDismissView(viewLayoutParams: ViewGroup.LayoutParams?,
                                                          height: Int)
                            {
                                inflatedViewParam = viewLayoutParams
                                viewHeight = height
                            }
                        }


                        touchListener = SwipeDismissTouchListener(inflatedLayout, null,
                            dismissTouchListener)

                        inflatedLayout.setOnTouchListener(touchListener)

                        inflatedLayout.setOnClickListener {
                            val addLineItemIntent =
                                Intent(activity, AddLineItemActivity::class.java)
                            ViewUtils.hideKeyboardFrom(mActivity, root_view)
                            addLineItemIntent.putExtra("addLineItem_id", inflatedLayout.id)
                            startActivityForResult(addLineItemIntent, 1)
                        }

                        addLineItemView?.addView(inflatedLayout)
                    }

                    totalAmountSumUp()
                }

            })
    }


    @SuppressLint("SetTextI18n")
    private fun setUpDate()
    {
        val c: Calendar = Calendar.getInstance()
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH) + 1
        mDate = c.get(Calendar.DAY_OF_MONTH)

        date_picker?.text = "${mDate}/${mMonth}/${mYear}"
    }


    @SuppressLint("SetTextI18n")
    private fun setUpOnClickListener()
    {
        date_picker?.setOnClickListener {

            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date: Date? = formatter.parse("${mDate + 1}/${mMonth}/${mYear}")

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(date?.time)
                .setTheme(R.style.ThemeOverlay_App_DatePicker)
                .build()


            datePicker.addOnPositiveButtonClickListener { datePickerObj ->

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = datePickerObj
                mYear = calendar.get(Calendar.YEAR)
                mMonth = calendar.get(Calendar.MONTH) + 1
                mDate = calendar.get(Calendar.DAY_OF_MONTH)

                date_picker?.text = "${mDate}/${mMonth}/${mYear}"

            }
            datePicker.show(mActivity.supportFragmentManager, "Datepickerdialog")

        }

        loading_charges?.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
                s?.let {

                    totalAmountSumUp()
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {

            }
        })

        transport_charge?.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
                s?.let {

                    totalAmountSumUp()
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {

            }
        })

        submit?.setOnClickListener {

            if(checkMandatoryFields())
            {
                var json: JSONObject? = null

                salesDetailsObj?.let {
                    it.salesDetailsColumns(updateObj(it))
                    json = it.constructJson()
                }

                val url: String?
                val method: Int?

                if(salesID != null)
                {
                    url = getString(R.string.sales_update_url)
                    method = Request.Method.PUT
                }
                else
                {
                    url =  getString(R.string.sales_creation_url)
                    method = Request.Method.POST
                }

                createSalesViewModel?.submitRequestDetails(mActivity, json, url, method, PreferencesUtils.getOauthToken(mActivity))
            }
        }

        addLineItem_bt?.setOnClickListener {

            val salesDetailsIntent = Intent(mActivity, AddLineItemActivity::class.java)
            ViewUtils.hideKeyboardFrom(mActivity, root_view)
            startActivityForResult(salesDetailsIntent, 1)
        }
    }

    private fun resultUpdate()
    {
        createSalesViewModel?.jsonViewModel?.observe(viewLifecycleOwner, { dataHashMap ->

            val jsonString = dataHashMap["response"].toString()

            val salesDetails =
                GsonUtils.getJSONStringFromGSON(jsonString, SalesDetailsResponds::class.java)

            salesDetails.salesDetails?.apply {
                val salesDetailsColumnsObj = SalesDetailsColumns()
                clearAddLineItemTable()

                salesDetailsColumnsObj.id = id
                salesDetailsColumnsObj.userId = userId
                salesDetailsColumnsObj.branchId = branchId
                salesDetailsColumnsObj.customerMobileNo = customerMobileNo
                salesDetailsColumnsObj.customerName = customerName
                salesDetailsColumnsObj.dateofSales = dateofSales
                salesDetailsColumnsObj.loadingCharges = loadingCharges
                salesDetailsColumnsObj.transportCharges = transportCharges
                salesDetailsColumnsObj.invoiceNumber = invoiceNumber
                salesDetailsColumnsObj.totalAmount = totalAmount

                insertSalesDetails(salesDetailsColumnsObj)

                addLineItem?.forEach {
                    insertAddLineItem(it)
                }
            }

            val salesDetailsIntent = Intent(mActivity, SalesDetailsActivity::class.java)
            salesDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            mActivity.startActivity(salesDetailsIntent)

            mActivity.finish()
        })
    }

    private fun totalAmountSumUp()
    {

        var itemTotalAmount = 0.0

        salesDetailsObj?.addLineItem?.let {

            for(addLineItem in it)
            {
                itemTotalAmount += addLineItem.itemTotalAmount?.toDouble()?: 0.0
            }
        }


        val loadingAmount =
            if (loading_charges?.text.isNullOrEmpty()) 0.0 else loading_charges?.text.toString()
                .toDouble()
        val transportAmount =
            if (transport_charge?.text.isNullOrEmpty()) 0.0 else transport_charge?.text.toString()
                .toDouble()

        total_amount?.text = (itemTotalAmount + loadingAmount + transportAmount).toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK)
        {

            data?.let {
                val quantity = it.getStringExtra("quantity")
                val rate = it.getStringExtra("rate")
                val mainGroupName = it.getStringExtra("mainGroupName")
                val mainGroupID = it.getIntExtra("mainGroupID", 0)
                val subGroupName = it.getStringExtra("subGroupName")
                val subGroupID = it.getIntExtra("subGroupID", 0)
                val itemName = it.getStringExtra("itemName")
                val itemID = it.getIntExtra("itemID", 0)

                val addLineItem = AddLineItemColumns()


                addLineItem.quantity = quantity?.toInt() ?: 0
                addLineItem.sellingPrice = rate
                addLineItem.productGroupId = mainGroupID
                addLineItem.mainGroupName = mainGroupName
                addLineItem.productsubGroupId = subGroupID
                addLineItem.subGroupName = subGroupName
                addLineItem.itemId = itemID
                addLineItem.itemName = itemName
                addLineItem.itemTotalAmount = (quantity?.toInt()!! * rate?.toDouble()!!).toString()
                addLineItem.type = IntConstant.SALES

                if(it.getIntExtra("id", 0) > 0)
                {
                    addLineItem.sl = it.getIntExtra("id", 0)
                    updateAddLineItem(addLineItem)
                }
                else
                {
                    insertAddLineItem(addLineItem)
                }

                try
                {
                    constructAddLineItemView()
                }
                catch (e: NumberFormatException)
                {

                }
            }
        }
    }

    private fun checkMandatoryFields(): Boolean
    {
        if (customerName?.text.isNullOrEmpty())
        {
            customerName_layout?.error = "Enter a valid customer name"
            return false
        }

        if (customerPhoneNo?.text.isNullOrEmpty())
        {
            customerPhoneNo_layout?.error = "Enter a valid customer mobile number"
            return false
        }

        if(addLineItemView?.childCount == null || addLineItemView?.childCount?: 0 < 1)
        {
            addLineItem_error?.visibility = View.VISIBLE
            addLineItem_error?.text = "Add at least one line item"
            return false
        }

        return true
    }

    private fun updateObj(salesDetailsObj: SalesDetailsScreenPOJO): SalesDetailsScreenPOJO
    {
        salesDetailsObj.customerName = customerName?.text.toString()

        salesDetailsObj.customerMobileNo = customerPhoneNo?.text.toString()

        userDetails?.let { userDetailsObj ->

            salesDetailsObj.userId = userDetailsObj.id
            salesDetailsObj.branchId = userDetailsObj.branchid

        }

        salesDetailsObj.dateofSales = ServerUtils.getServerDateFormatted(mDate, mMonth, mYear)?.time

        salesDetailsObj.loadingCharges = loading_charges?.text.toString()

        salesDetailsObj.transportCharges = transport_charge?.text.toString()

        salesDetailsObj.totalAmount = total_amount?.text.toString()


        return salesDetailsObj
    }

    companion object
    {
        var createSalesFragment: CreateSalesFragment? = null

        fun getInstance(bundle: Bundle): CreateSalesFragment?
        {
            if (createSalesFragment == null)
            {
                createSalesFragment = CreateSalesFragment()
                createSalesFragment!!.arguments = bundle
            }
            return createSalesFragment
        }
    }

    override fun notifySuccessFully(dataHashMap: HashMap<K, K>)
    {
        val jsonString = dataHashMap["response"].toString()
        val salesDetails =
            GsonUtils.getJSONStringFromGSON(jsonString, SalesDetailsResponds::class.java)

        salesDetails.salesDetails?.apply {
            val salesDetailsColumnsObj = SalesDetailsColumns()

            salesDetailsColumnsObj.id = id
            salesDetailsColumnsObj.userId = userId
            salesDetailsColumnsObj.branchId = branchId
            salesDetailsColumnsObj.customerMobileNo = customerMobileNo
            salesDetailsColumnsObj.customerName = customerName
            salesDetailsColumnsObj.dateofSales = dateofSales
            salesDetailsColumnsObj.loadingCharges = loadingCharges
            salesDetailsColumnsObj.transportCharges = transportCharges
            salesDetailsColumnsObj.invoiceNumber = invoiceNumber
            salesDetailsColumnsObj.totalAmount = totalAmount

            insertSalesDetails(salesDetailsColumnsObj)

            addLineItem?.forEach {
                insertAddLineItem(it)
            }
        }

        val salesDetailsIntent = Intent(mActivity, SalesDetailsActivity::class.java)
        salesDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        mActivity.startActivity(salesDetailsIntent)

        mActivity.finish()
    }

    override fun notifyFailed(code: String?, errorMessage: String?)
    {
        MaterialAlertDialogBuilder(mActivity)
            .setMessage(errorMessage)
            .setPositiveButton(getString(android.R.string.ok)) { dialog, which ->
            }
            .show()
    }

    private fun insertSalesDetails(salesDetails: SalesDetailsColumns)
    {
        AppExecutors.instance?.diskIO()?.execute {
            mActivity.mDb!!.salesDetailsDao()!!.insertSalesDetails(salesDetails)
        }
    }

    private fun insertAddLineItem(addLineItemDetails: AddLineItemColumns)
    {
        AppExecutors.instance?.diskIO()?.execute {
            mActivity.mDb?.addLineItemDao()?.insertAddLineItem(addLineItemDetails)
        }
    }

    private fun updateAddLineItem(addLineItemDetails: AddLineItemColumns)
    {
        AppExecutors.instance?.diskIO()?.execute {
            mActivity.mDb?.addLineItemDao()?.updateAddLineItem(addLineItemDetails)
        }
    }

    private fun setUpVolleyLib()
    {
        mVolleyRequestClass = VolleyRequestClass(volleyNetworkCallBack = this)
    }

    private fun getIntentValues()
    {
        salesID = mActivity.intent.getSerializableExtra("sales_id") as Int?
    }

    private fun clearAddLineItemTableByID(id: Int)
    {
        AppExecutors.instance?.diskIO()?.execute {
            mActivity.mDb?.addLineItemDao()?.clearAddLineItemByID(id)
        }
    }

    private fun clearAddLineItemTable()
    {
        AppExecutors.instance?.diskIO()?.execute {
            mActivity.mDb?.addLineItemDao()?.clearAddLineItem()
        }
    }

}