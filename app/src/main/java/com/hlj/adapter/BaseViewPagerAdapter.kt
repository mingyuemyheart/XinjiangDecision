package com.hlj.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter

/**
 * @ClassName: MyPagerAdapter
 * @Description: TODO填充ViewPager的数据适配器
 * @author Panyy
 * @date 2013 2013年11月6日 下午2:37:47
 *
 */
class BaseViewPagerAdapter(fm: FragmentManager, fs : ArrayList<Fragment>) : FragmentStatePagerAdapter(fm) {

    private val fragments : ArrayList<Fragment> = fs

    init {
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItem(arg0: Int): Fragment {
        return fragments[arg0]
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}