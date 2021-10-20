package com.example.sevg.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.sevg.AddInwardItemFragment


class ViewPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!)
{
    override fun getItem(position: Int): Fragment
    {
        val fragment = AddInwardItemFragment()
        return when (position)
        {
            0 -> fragment
            1 -> fragment
            else -> fragment
        }

    }

    override fun getCount(): Int
    {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence
    {
        return when (position)
        {
            0 ->
            {
                "Inward"
            }
            1 ->
            {
                "Outward"
            }
            else ->
            {
                ""
            }
        }
    }
}