package com.ttchain.githubusers.ui.bank

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.ttchain.githubusers.R
import com.ttchain.githubusers.base.BaseFragment
import kotlinx.android.synthetic.main.bank_list.*
import kotlinx.android.synthetic.main.bank_list.getBankButton

class BankFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    private val bankAdapter = BankAdapter()

    override val layoutId = R.layout.bank_list

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    override fun initView() {

        val items = listOf(
            "中國銀行 123456789 陳大大",
            "上海銀行 123456789 陳二大",
            "北京銀行 123456789 陳三大",
            "北京銀行 123456789 陳三大",
            "北京銀行 123456789 陳三大"
        )

        getBankButton.setOnClickListener {
            context?.let {
                MaterialDialog(it).show {
                    listItems(items = items)
                }
            }
        }

        swipe_layout.setOnRefreshListener(this)

        recycler_view.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = bankAdapter
        }

        bankAdapter.updateData(listOf("", "", "", "", ""))
    }

    override fun onRefresh() {
        swipe_layout.isRefreshing = false
    }
}