package com.example.sevg

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.example.sevg.constant.IntConstant
import com.example.sevg.module.InventoryDetails
import com.example.sevg.module.InventoryDetailsObj
import com.example.sevg.room.database.AppExecutors
import com.example.sevg.room.model.*
import com.example.sevg.swipeToDismiss.SwipeDismissTouchListener
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
import kotlinx.android.synthetic.main.inward_items.addLineItemView
import kotlinx.android.synthetic.main.inward_items.addLineItem_bt
import kotlinx.android.synthetic.main.inward_items.date_picker
import kotlinx.android.synthetic.main.inward_items.root_view
import kotlinx.android.synthetic.main.inward_items.submit
import java.lang.NumberFormatException
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class AddInwardItemFragment : Fragment(), VolleyRequestClass.VolleyNetworkCallBack
{
    private lateinit var mActivity: SalesAndInventoryActivity
    private lateinit var mVolleyRequestClass: VolleyRequestClass

    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDate: Int = 0

    private var inventoryDetails: InventoryDetails? = null
    private var toolbar: Toolbar? = null
    private var inwardOutwardID: Int? = null
    private var userBranchID: Int? = null

    private var branchArrayList: List<BranchListColumns>? = null

    private var createInventoryViewModel: ViewModelClass? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.inward_items, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        updateDefaultDisplay()

        setHasOptionsMenu(true)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        val toolbarMenu = toolbar?.menu

        if(toolbarMenu?.children?.count()?: 0 < 1)
        {
            inflater.inflate(R.menu.toolbar_menu, toolbarMenu)
        }

        super.onCreateOptionsMenu(toolbarMenu!!, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {

        if(item.itemId == R.id.logout)
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

        inventoryDetails = InventoryDetails()

        val viewProvider = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())

        createInventoryViewModel = viewProvider.get(ViewModelClass::class.java)

        setUpToolbar()

        setUpVolleyLib()

        getIntentValues()

        setUpMandatoryFields()

        setUpDate()

        setUpOnClickListener()

        updateTransferStatusSpinner(IntConstant.OUTWARD)

        fetchUserDetailsFromDB()

        fetchBranchListFromDB()

        updateDisplay()
    }


    private fun setUpToolbar()
    {
        mActivity.run {
            toolbar = mActivity.findViewById(R.id.inward_toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.title = getString(R.string.inventory)

            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun setUpVolleyLib()
    {
        mVolleyRequestClass = VolleyRequestClass(volleyNetworkCallBack = this)
    }

    private fun getIntentValues()
    {
        inwardOutwardID = mActivity.intent.getSerializableExtra("inward_outward_id") as Int?
    }


    private fun updateDisplay()
    {
        mActivity.mDb?.branchListDao()?.getBranchListDetails()?.observe(viewLifecycleOwner, { updateCurrentWarehouseSpinner(userBranchID, it) })

        if(inwardOutwardID != null)
        {
            mActivity.mDb?.inwardOutwardDao()?.getInwardOutwardDetails()
                ?.observe(viewLifecycleOwner, { inventoryDetails->

                    inventoryDetails?.apply{

                        date_picker?.text = ServerUtils.getDateFormatted(dateTime!!.toLong())

                        updateTransferStatusSpinner(inventoryType)

                        this@AddInwardItemFragment.billNo.setText(billNo)
                    }
                })

            mActivity.mDb?.addLineItemDao()?.getAllAddLineItem()
                ?.observe(viewLifecycleOwner, { addLineItemObj ->

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

        resultUpdate()
    }

    private fun resultUpdate()
    {
        createInventoryViewModel?.jsonViewModel?.observe(viewLifecycleOwner, { dataHashMap ->

            val jsonString = dataHashMap["response"].toString()
            val inventoryDetails = GsonUtils.getJSONStringFromGSON(jsonString, InventoryDetailsObj::class.java).inventoryDetails

            inventoryDetails?.apply {
                val inwardOutwardDetails = InwardOutwardColumns()

                clearAddLineItemTable()

                inwardOutwardDetails.id = id
                inwardOutwardDetails.currentBranchid = currentBranchid
                inwardOutwardDetails.inventoryType = inventoryType
                inwardOutwardDetails.billNo = billNo
                inwardOutwardDetails.dateTime = dateTime

                insertInwardOutwardDetails(inwardOutwardDetails)

                inventoryItemsList?.forEach {
                    insertAddLineItem(it)
                }
            }

            val inventoryDetailsIntent = Intent(mActivity, InventoryItemDetailsActivity::class.java)
            inventoryDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(inventoryDetailsIntent)

            mActivity.finish()

        })
    }


    private fun fetchUserDetailsFromDB()
    {
        mActivity.mDb?.userDetailsDao()?.getUserDetails()?.observe(activity as LifecycleOwner,
            { userBranchID = it?.id!! })
    }

    private fun fetchBranchListFromDB()
    {
        branchArrayList = ArrayList()
        mActivity.mDb?.branchListDao()?.getBranchListDetails()?.observe(activity as LifecycleOwner,{
            branchArrayList = it })
    }


    private fun setUpMandatoryFields()
    {
        billNo_layout?.markRequiredInRed()
    }

    private fun constructAddLineItemView()
    {
        mActivity.mDb?.addLineItemDao()?.getAllAddLineItem()
            ?.observe(viewLifecycleOwner, { lineItemList->

                if (lineItemList != null)
                {
                    addLineItemView?.removeAllViews()
                    addLineItem_error?.visibility = View.GONE

                    inventoryDetails?.inventoryItemsList = ArrayList()
                    inventoryDetails?.inventoryItemsList?.addAll(lineItemList)

                    var touchListener: SwipeDismissTouchListener? = null

                    for (addLineItemObj in lineItemList)
                    {
                        val inflatedLayout: View = mActivity.layoutInflater.inflate(R.layout.add_line_item_details, root_view as ViewGroup?, false)

                        val itemName = inflatedLayout.findViewById<TextView>(R.id.itemName)
                        val quantity = inflatedLayout.findViewById<TextView>(R.id.quantity)
                        val rate = inflatedLayout.findViewById<TextView>(R.id.rate)
                        val symbol = inflatedLayout.findViewById<TextView>(R.id.symbol)
                        val totalAmount =
                            inflatedLayout.findViewById<TextView>(R.id.lineItem_total_amount)

                        var inflatedViewParam: ViewGroup.LayoutParams? = null
                        var viewHeight = 0

                        inflatedLayout.id = addLineItemObj.sl
                        itemName?.text = addLineItemObj.itemName
                        quantity?.visibility = View.GONE
                        rate?.visibility = View.GONE
                        symbol?.visibility = View.GONE
                        totalAmount?.text = ((BigDecimal(addLineItemObj.quantity).toBigInteger()).toString())

                        val dismissTouchListener = object :
                            SwipeDismissTouchListener.DismissCallbacks
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

                                        for(i in 0 until inventoryDetails?.inventoryItemsList?.size!!)
                                        {
                                            if(inventoryDetails?.inventoryItemsList?.get(i)?.sl == inflatedLayout.id)
                                            {
                                                inventoryDetails?.inventoryItemsList?.removeAt(i)

                                                break
                                            }
                                        }
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
                }

            })
    }

    private fun insertAddLineItem(addLineItemDetails: AddLineItemColumns)
    {
        AppExecutors.instance?.diskIO()?.execute {
            mActivity.mDb?.addLineItemDao()?.insertAddLineItem(addLineItemDetails)
        }
    }


    private fun updateTransferStatusSpinner(inventoryType: Int?)
    {
        val transferStatusAdapterList: ArrayList<String> = ArrayList()
        transferStatusAdapterList.add("Outward")
        transferStatusAdapterList.add("Inward")

        val transferStatusAdapter: ArrayAdapter<String> =
            ArrayAdapter(mActivity, android.R.layout.simple_spinner_item, transferStatusAdapterList)
        transferStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        transfer_status_spinner?.adapter = transferStatusAdapter

        transfer_status_spinner?.setSelection(if(inventoryType == 0) 0 else 1)
    }

    private fun updateCurrentWarehouseSpinner(branchID: Int?, branchList: List<BranchListColumns>)
    {
        val branchAdapterList: ArrayList<String> = ArrayList()
        var position = -1
        for ((index, branch) in branchList.withIndex())
        {
            branchAdapterList.add(branch.branchName!!)

            if(branch.id == branchID)
            {
                position = index
            }
        }

        val currentWarehouseAdapter: ArrayAdapter<String> =
            ArrayAdapter(mActivity, android.R.layout.simple_spinner_item, branchAdapterList)
        currentWarehouseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        current_warehouse_spinner?.adapter = currentWarehouseAdapter

        current_warehouse_spinner?.setSelection(if(position == -1) 0 else position)

        current_warehouse_spinner?.isEnabled = false
    }

    @SuppressLint("SetTextI18n")
    private fun setUpDate()
    {
        val c: Calendar = Calendar.getInstance()
        mYear = c.get(Calendar.YEAR)
        mMonth= c.get(Calendar.MONTH)
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
                .setSelection(date!!.time)
                .setTheme(R.style.ThemeOverlay_App_DatePicker)
                .build()


            datePicker.addOnPositiveButtonClickListener {

                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = it
                mYear = calendar.get(Calendar.YEAR)
                mMonth = calendar.get(Calendar.MONTH)
                mDate = calendar.get(Calendar.DAY_OF_MONTH)

                date_picker?.text = "${mDate}/${mMonth}/${mYear}"

            }
            datePicker.show(mActivity.supportFragmentManager, "Datepickerdialog")

        }

        submit?.setOnClickListener {

            if(checkMandatoryFields())
            {
                val inwardOutwardDetailsObj = InwardOutwardColumns()
                inwardOutwardDetailsObj.inwardOutwardColumns(updateObj(inwardOutwardDetailsObj))
                val json = inwardOutwardDetailsObj.constructJson()

                val url: String?
                val method: Int?

                if(inwardOutwardID != null)
                {
                    url = getString(R.string.inward_outward_update_url)
                    method = Request.Method.PUT
                }
                else
                {
                    url =  getString(R.string.inward_outward_creation_url)
                    method = Request.Method.POST
                }

                createInventoryViewModel?.submitRequestDetails(mActivity, json, url, method, PreferencesUtils.getOauthToken(mActivity))
            }
        }

        addLineItem_bt?.setOnClickListener {
            ViewUtils.hideKeyboardFrom(mActivity, root_view)
            val inventoryIntent = Intent(mActivity, AddLineItemActivity::class.java)
            inventoryIntent.putExtra("isFromInventory", true)
            startActivityForResult(inventoryIntent, 1)
        }
    }

    companion object
    {
        private var addInwardItemFragment: AddInwardItemFragment? = null

        fun getInstance(bundle: Bundle?): AddInwardItemFragment?
        {
            if(addInwardItemFragment == null)
            {
                addInwardItemFragment = AddInwardItemFragment()
                addInwardItemFragment!!.arguments = bundle
            }
            return addInwardItemFragment
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK)
        {
            data?.let {
                val quantity = it.getStringExtra("quantity")
                val mainGroupName = it.getStringExtra("mainGroupName")
                val mainGroupID = it.getIntExtra("mainGroupID", 0)
                val subGroupName = it.getStringExtra("subGroupName")
                val subGroupID = it.getIntExtra("subGroupID", 0)
                val itemName = it.getStringExtra("itemName")
                val itemID = it.getIntExtra("itemID", 0)

                val addLineItem = AddLineItemColumns()

                addLineItem.quantity = quantity?.toInt() ?: 0
                addLineItem.productGroupId = mainGroupID
                addLineItem.mainGroupName = mainGroupName
                addLineItem.productsubGroupId = subGroupID
                addLineItem.subGroupName = subGroupName
                addLineItem.itemId = itemID
                addLineItem.itemName = itemName
                addLineItem.type = if(transfer_status_spinner?.selectedItemPosition == 0) IntConstant.OUTWARD else IntConstant.INWARD

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

    override fun notifySuccessFully(dataHashMap: HashMap<K, K>)
    {
        val jsonString = dataHashMap["response"].toString()
        val inventoryDetails = GsonUtils.getJSONStringFromGSON(jsonString, InventoryDetails::class.java)

        inventoryDetails.apply {
            val inwardOutwardDetails = InwardOutwardColumns()

            inwardOutwardDetails.id = id
            inwardOutwardDetails.currentBranchid = currentBranchid
            inwardOutwardDetails.inventoryType = inventoryType
            inwardOutwardDetails.billNo = billNo
            inwardOutwardDetails.dateTime = dateTime

            insertInwardOutwardDetails(inwardOutwardDetails)

            inventoryItemsList?.forEach {
                insertAddLineItem(it)
            }
        }

        val inventoryDetailsIntent = Intent(mActivity, InventoryItemDetailsActivity::class.java)
        inventoryDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(inventoryDetailsIntent)

        mActivity.finish()
    }

    override fun notifyFailed(code: String?, errorMessage: String?)
    {
        TODO("Not yet implemented")
    }

    private fun insertInwardOutwardDetails(inwardOutwardDetails: InwardOutwardColumns)
    {
        AppExecutors.instance?.diskIO()?.execute {
            mActivity.mDb!!.inwardOutwardDao()!!.insertInwardOutwardDetails(inwardOutwardDetails)
        }
    }

    private fun updateAddLineItem(addLineItemDetails: AddLineItemColumns)
    {
        AppExecutors.instance?.diskIO()?.execute {
            mActivity.mDb?.addLineItemDao()?.updateAddLineItem(addLineItemDetails)
        }
    }


    private fun checkMandatoryFields(): Boolean
    {
        if(billNo?.text.isNullOrEmpty())
        {
            billNo_layout?.error = "Enter a valid customer name"
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

    private fun updateObj(inwardOutwardDetailsObj: InwardOutwardColumns): InwardOutwardColumns
    {
        inwardOutwardDetailsObj.inventoryType = if(transfer_status_spinner?.selectedItemPosition == 0) IntConstant.OUTWARD else IntConstant.INWARD

        inwardOutwardDetailsObj.dateTime = ServerUtils.getServerDateFormatted(mDate, mMonth, mYear)?.time.toString()

        inwardOutwardDetailsObj.billNo = billNo.text.toString()

        inwardOutwardDetailsObj.currentBranchid = branchArrayList?.get(current_warehouse_spinner?.selectedItemPosition!!)?.id!!

        inwardOutwardDetailsObj.addLineItem = inventoryDetails?.inventoryItemsList

        return inwardOutwardDetailsObj
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