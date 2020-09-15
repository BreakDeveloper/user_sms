package com.ttchain.githubusers.ui.sms

import android.os.Bundle
import com.ttchain.githubusers.R
import com.ttchain.githubusers.base.BaseFragment
import kotlinx.android.synthetic.main.sms_container.*

class SmsContainerFragment : BaseFragment() {

    private val pagerAdapter by lazy {
        SmsPagerAdapter(childFragmentManager)
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
    }
}