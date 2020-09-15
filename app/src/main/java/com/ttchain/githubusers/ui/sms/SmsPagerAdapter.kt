package com.ttchain.githubusers.ui.sms

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ttchain.githubusers.App.Companion.instance
import com.ttchain.githubusers.R
import com.ttchain.githubusers.ui.bank.BankFragment

class SmsPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(index: Int): Fragment {
        return when (index) {
            0 -> SmsReceiptFragment()
            1 -> BankFragment()
            else -> Fragment()
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> instance.getString(R.string.tab1)
            1 -> instance.getString(R.string.tab2)
            else -> ""
        }
    }
}