package com.example.sevg

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.example.sevg.room.database.AppDatabase
import com.example.sevg.room.model.*
import com.example.sevg.room.utlis.DatabaseUtils
import com.example.sevg.utils.PreferencesUtils
import com.example.sevg.utils.ViewUtils
import com.example.sevg.utils.ViewUtils.markRequiredInRed
import kotlinx.android.synthetic.main.add_line_activity.*
import kotlinx.android.synthetic.main.add_line_activity.item_name_spinner
import kotlinx.android.synthetic.main.add_line_activity.main_group_spinner
import kotlinx.android.synthetic.main.add_line_activity.quantity
import kotlinx.android.synthetic.main.add_line_activity.quantity_layout
import kotlinx.android.synthetic.main.add_line_activity.rate
import kotlinx.android.synthetic.main.add_line_activity.root_view
import kotlinx.android.synthetic.main.add_line_activity.sub_group_spinner
import kotlinx.android.synthetic.main.add_line_activity.sub_group_spinner_layout
import kotlinx.android.synthetic.main.add_line_item_details.*
import kotlinx.android.synthetic.main.sales_fragment.*

class AddLineItemActivity: AppCompatActivity()
{
    private var mDb: AppDatabase? = null
    private var mainGroupList: List<MainGroupColumns>? = null
    private var subGroupList: List<SubGroupColumns>? = null
    private var itemList: List<ItemColumns>? = null
    private var addLineItemColumns: AddLineItemColumns? = null
    private var addLineItemID: Int? = null
    private var isFromInventory: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.add_line_activity)

        updateDefaultDisplay()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            android.R.id.home ->
            {
                ViewUtils.hideKeyboardFrom(this, root_view)
                finishActivity()
                true
            }
            else -> false
        }
    }

    private fun updateDefaultDisplay()
    {

        setUpRoomDB()

        setUpMandatoryFields()

        getIntentValues()

        setUpOnClickListener()

        setUpStatusBar()

        setUpToolbar()

        updateDisplay()
    }

    private fun setUpRoomDB()
    {
        mDb = DatabaseUtils.getDBInstances(this)
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
        val toolbar = findViewById<Toolbar>(R.id.addLineItem_toolbar)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material)
        upArrow?.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_ATOP
        )
        supportActionBar?.setHomeAsUpIndicator(upArrow)

        supportActionBar?.title = getString(R.string.addLineItem_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun updateDisplay()
    {
        if(isFromInventory)
        {
            rate_layout?.visibility = View.GONE
            quantity?.gravity = Gravity.START
        }

        mDb!!.mainGroupDao()!!.getMainGroupList().observe(this, { mainGroupList ->
            updateMainGroupSpinner(mainGroupList)
        })
    }

    private fun setUpOnClickListener()
    {
        main_group_spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                sub_group_spinner?.adapter = null
                item_name_spinner?.adapter = null
                mainGroupList?.get(position)?.id?.let {
                    mDb?.subGroupDao()?.getSubGroupListByID(it)?.observe(this@AddLineItemActivity,
                        { subGroupList ->

                            if(!subGroupList.isNullOrEmpty())
                            {
                                sub_group_spinner_layout?.visibility = View.VISIBLE
                                updateSubGroupSpinner(subGroupList)
                            } else
                            {
                                sub_group_spinner_layout?.visibility = View.GONE

                                mDb!!.itemDao()!!.getItemListByMGID(it)
                                    .observe(this@AddLineItemActivity, androidx.lifecycle.Observer { itemList ->

                                        if (!itemList.isNullOrEmpty() && itemList[0].itemgroupid == 0)
                                        {
                                            this@AddLineItemActivity.subGroupList = null
                                            updateItemNameSpinner(itemList)
                                        }
                                    })
                            }
                        })
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?)
            {
            }
        }

        sub_group_spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                item_name_spinner?.adapter = null
                mDb?.itemDao()?.getItemListBySGID(subGroupList!![position].id)
                    ?.observe(this@AddLineItemActivity, androidx.lifecycle.Observer { itemList ->

                        if (!itemList.isNullOrEmpty())
                        {
                            updateItemNameSpinner(itemList)
                        }
                    })
            }

            override fun onNothingSelected(parent: AdapterView<*>?)
            {
            }
        }

        save?.setOnClickListener {

            if(checkMandatoryFields())
            {
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, updateObj(resultIntent))

                ViewUtils.hideKeyboardFrom(this, root_view)
                finish()
            }

        }
    }

    private fun setUpMandatoryFields()
    {
        rate_layout?.markRequiredInRed()
        quantity_layout?.markRequiredInRed()
    }

    private fun getIntentValues()
    {
        addLineItemID = intent.getSerializableExtra("addLineItem_id") as Int?
        isFromInventory = intent.getBooleanExtra("isFromInventory", false)
    }

    private fun finishActivity()
    {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.addLineItem_back_press))

        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            dialog.dismiss()
            finish()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun checkMandatoryFields(): Boolean
    {
        if(rate?.text.isNullOrEmpty() && !isFromInventory)
        {
            rate_layout?.error = "Enter a valid selling Price"
            return false
        }

        if(quantity?.text.isNullOrEmpty())
        {
            quantity_layout?.error = "Enter a valid quantity"
            return false
        }

        return true
    }

    private fun updateObj(bundle: Intent): Intent
    {
        bundle.putExtra("id", addLineItemID)
        bundle.putExtra("quantity", quantity?.text.toString())
        bundle.putExtra("rate", rate?.text.toString())

        if(mainGroupList?.isNotEmpty() == true)
        {
            if((main_group_spinner?.adapter)?.getItem(main_group_spinner?.selectedItemPosition!!) == mainGroupList!![main_group_spinner?.selectedItemPosition!!].groupname)
            {
                bundle.putExtra("mainGroupName", mainGroupList!![main_group_spinner?.selectedItemPosition!!].groupname)
                bundle.putExtra("mainGroupID", mainGroupList!![main_group_spinner?.selectedItemPosition!!].id)
            }
        }

        if(subGroupList?.isNotEmpty() == true)
        {
            if((sub_group_spinner?.adapter)?.getItem(sub_group_spinner?.selectedItemPosition!!) == subGroupList!![sub_group_spinner?.selectedItemPosition!!].groupname)
            {
                bundle.putExtra("subGroupName", subGroupList!![sub_group_spinner?.selectedItemPosition!!].groupname)
                bundle.putExtra("subGroupID", subGroupList!![sub_group_spinner?.selectedItemPosition!!].id)
            }
        }

        if(itemList?.isNotEmpty() == true)
        {
            if((item_name_spinner?.adapter)?.getItem(item_name_spinner?.selectedItemPosition!!) == itemList!![item_name_spinner?.selectedItemPosition!!].itemname)
            {
                bundle.putExtra("itemName", itemList!![item_name_spinner?.selectedItemPosition!!].itemname)
                bundle.putExtra("itemID", itemList!![item_name_spinner?.selectedItemPosition!!].id)
            }
        }

        return bundle
    }


    private fun updateMainGroupSpinner(mainGroupList: List<MainGroupColumns>?)
    {
        this.mainGroupList = mainGroupList
        val mainGroupAdapterList: ArrayList<String> = ArrayList()
        var position: Int? = null

        if(addLineItemID != null)
        {
            mDb?.addLineItemDao()?.getAddLineItemByID(addLineItemID!!)
                ?.observe(this, { addLineItemColumnObj ->

                    addLineItemColumns = addLineItemColumnObj

                    for ((index, value) in mainGroupList?.withIndex()!!)
                    {
                        mainGroupAdapterList.add(value.groupname!!)

                        addLineItemColumns?.let {

                            if (value.id == it.productGroupId)
                            {
                                position = index
                            }
                        }
                    }

                    rate?.setText(addLineItemColumns?.sellingPrice!!)
                    quantity?.setText(addLineItemColumns?.quantity.toString())

                    val productGroupAdapter: ArrayAdapter<String> =
                        ArrayAdapter(this, android.R.layout.simple_spinner_item, mainGroupAdapterList)
                    productGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                    main_group_spinner?.adapter = productGroupAdapter

                    position?.let { main_group_spinner?.setSelection(it) }

                })
        }
        else
        {
            mainGroupList?.forEach {
                mainGroupAdapterList.add(it.groupname!!)
            }

            val productGroupAdapter: ArrayAdapter<String> =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, mainGroupAdapterList)
            productGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            main_group_spinner?.adapter = productGroupAdapter
        }
    }


    private fun updateSubGroupSpinner(subGroupList: List<SubGroupColumns>?)
    {
        this.subGroupList = subGroupList

        var position: Int? = null

        val subGroupAdapterList: ArrayList<String> = ArrayList()

        for ((index, value) in subGroupList?.withIndex()!!)
        {
            subGroupAdapterList.add(value.groupname!!)

            addLineItemColumns?.let {

                if (value.id == it.productsubGroupId)
                {
                    position = index
                }
            }
        }

        val productGroupAdapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, subGroupAdapterList)
        productGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        sub_group_spinner?.adapter = productGroupAdapter

        position?.let { sub_group_spinner?.setSelection(it) }
    }

    private fun updateItemNameSpinner(itemList: List<ItemColumns>?)
    {
        this.itemList = itemList

        var position: Int? = null

        val itemNameAdapterList: ArrayList<String> = ArrayList()

        for ((index, value) in itemList?.withIndex()!!)
        {
            itemNameAdapterList.add(value.itemname!!)

            addLineItemColumns?.let {

                if (value.id == it.itemId)
                {
                    position = index
                }
            }
        }

        val itemNameAdapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, itemNameAdapterList)
        itemNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        item_name_spinner?.adapter = itemNameAdapter

        position?.let { item_name_spinner?.setSelection(it) }
    }
}