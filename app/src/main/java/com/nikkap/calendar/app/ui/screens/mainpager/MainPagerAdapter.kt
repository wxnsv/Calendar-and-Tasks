package com.nikkap.calendar.app.ui.screens.mainpager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nikkap.calendar.app.ui.screens.list.ListFragment
import com.nikkap.calendar.app.ui.screens.split.SplitFragment

class MainPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ListFragment()
            1 -> SplitFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }


}