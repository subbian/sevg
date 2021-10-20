package com.example.sevg

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sevg.room.database.AppDatabase
import com.example.sevg.room.database.AppExecutors
import com.example.sevg.room.utlis.DatabaseUtils
import kotlinx.android.synthetic.main.sales_inventory_main_activity.*


class SalesAndInventoryActivity: AppCompatActivity()
{
    var mDb: AppDatabase? = null
    private var salesID: Int? = null
    private var inwardOutwardID: Int? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.sales_inventory_main_activity)

        setUpStatusBar()

        getIntentValues()

        setUpRoomDB()

        val bundle = Bundle()
        bundle.putBundle("Intent", intent.extras)

        mDb!!.userDetailsDao()!!.getUserDetails().observe(this, androidx.lifecycle.Observer { userDetailsObj->

            val isInventoryEnabled = if(salesID == null && inwardOutwardID == null)
            {
                userDetailsObj?.isInventoryEnabled == true
            }
            else
            {
                false
            }

            if(isInventoryEnabled)
            {
                navigation?.visibility = View.VISIBLE
                val fm = supportFragmentManager
                val createSalesFragment = CreateSalesFragment.getInstance(bundle)
                val addInwardItemFragment = AddInwardItemFragment.getInstance(null)
                var active: Fragment = createSalesFragment!!

                if(!createSalesFragment.isAdded && addInwardItemFragment?.isAdded == false)
                {
                    fm.beginTransaction().add(R.id.main_container, createSalesFragment, "1")
                        .commit()
                    fm.beginTransaction().add(R.id.main_container, addInwardItemFragment, "2")
                        .hide(addInwardItemFragment).commit()


                    navigation?.setOnNavigationItemSelectedListener { item ->
                        when (item.itemId)
                        {
                            R.id.sales ->
                            {
                                fm.beginTransaction().hide(active).show(createSalesFragment)
                                    .commit()
                                active = createSalesFragment
                                true
                            }
                            R.id.inventory ->
                            {
                                fm.beginTransaction().hide(active).show(addInwardItemFragment)
                                    .commit()
                                active = addInwardItemFragment
                                true
                            }
                            else -> false
                        }
                    }
                }
            }
            else
            {
                navigation?.visibility = View.GONE
                val fm = supportFragmentManager

                if(salesID != null)
                {
                    val createSalesFragment = CreateSalesFragment.getInstance(bundle)

                    if (createSalesFragment?.isAdded == false)
                    {
                        fm.beginTransaction().add(R.id.main_container, createSalesFragment, "1")
                            .commit()
                    }
                }
                else
                {
                    val addInwardItemFragment = AddInwardItemFragment.getInstance(bundle)

                    if (addInwardItemFragment?.isAdded == false)
                    {
                        fm.beginTransaction().add(R.id.main_container, addInwardItemFragment, "2")
                            .commit()
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

    private fun setUpRoomDB()
    {
        mDb = DatabaseUtils.getDBInstances(this)
    }

    private fun getIntentValues()
    {
        salesID = intent.getSerializableExtra("sales_id") as Int?
        inwardOutwardID = intent.getSerializableExtra("inward_outward_id") as Int?
    }

    fun clearAllTables()
    {
        AppExecutors.instance?.diskIO()?.execute {
            mDb?.clearAllTables()
        }
    }

    override fun onBackPressed()
    {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.exit_warning_message))

        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            dialog.dismiss()
            super.onBackPressed()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()

    }
}