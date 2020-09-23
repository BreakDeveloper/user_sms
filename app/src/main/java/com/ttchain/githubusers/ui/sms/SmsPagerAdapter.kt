package com.ttchain.githubusers.ui.sms

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ttchain.githubusers.App.Companion.instance
import com.ttchain.githubusers.R
import com.ttchain.githubusers.data.ReceiptMessage
import com.ttchain.githubusers.ui.bank.BankFragment
import com.ttchain.githubusers.withBundleValue
import java.io.Serializable

class SmsPagerAdapter(fm: FragmentManager, private val list: List<ReceiptMessage>) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(index: Int): Fragment {
        return when (index) {
            0 -> SmsReceiptFragment().withBundleValue {
                putSerializable("login_success", list as Serializable)
            }
            1 -> BankFragment().withBundleValue {
                putSerializable("login_success", list as Serializable)
            }
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