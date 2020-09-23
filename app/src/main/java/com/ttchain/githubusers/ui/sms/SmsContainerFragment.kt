package com.ttchain.githubusers.ui.sms

import android.os.Bundle
import android.view.KeyEvent
import com.ttchain.githubusers.R
import com.ttchain.githubusers.base.BaseFragment
import com.ttchain.githubusers.data.ReceiptMessage
import com.ttchain.githubusers.getBundleValue
import kotlinx.android.synthetic.main.sms_container.*

class SmsContainerFragment : BaseFragment() {

    private val list by getBundleValue("login_success", listOf<ReceiptMessage>())

    private val pagerAdapter by lazy {
        SmsPagerAdapter(childFragmentManager, list)
    }

    override val layoutId = R.layout.sms_container

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    override fun initView() {
        view_pager.offscreenPageLimit = 2
        view_pager.adapter = pagerAdapter
        tab_layout.setupWithViewPager(view_pager)
        view?.apply {
            isFocusableInTouchMode = true
            requestFocus()
            setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }
        }
    }
}