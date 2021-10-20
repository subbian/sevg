package com.example.sevg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.sevg.adapter.ViewPagerAdapter
import kotlinx.android.synthetic.main.inventory_main_fragment.*

class InventoryMainFragment: Fragment()
{
    lateinit var mActivity: SalesAndInventoryActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.inventory_main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        updateDefaultDisplay()
    }

    private fun updateDefaultDisplay()
    {
        mActivity = activity as SalesAndInventoryActivity

        setUpToolbar()

        setUpViewPager()
    }

    private fun setUpToolbar()
    {
        mActivity.run {
            val toolbar = mActivity.findViewById<Toolbar>(R.id.inventory_toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun setUpViewPager()
    {
        viewPager?.adapter = ViewPagerAdapter(mActivity.supportFragmentManager)

        tabLayout?.setupWithViewPager(viewPager)
    }
}