package com.ttchain.githubusers.ui.bank

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.ttchain.githubusers.*
import com.ttchain.githubusers.base.BaseFragment
import com.ttchain.githubusers.data.ReceiptMessage
import com.ttchain.githubusers.net.SignalR
import com.ttchain.githubusers.ui.image.ImageFragment
import kotlinx.android.synthetic.main.bank_list.*
import kotlinx.coroutines.*
import timber.log.Timber

class BankFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener,
    CoroutineScope by MainScope() {

    private val list by getBundleValue("login_success", mutableListOf<ReceiptMessage>())
    private val bankAdapter = BankAdapter()
    private var banks = mutableListOf<String>()

    override val layoutId = R.layout.bank_list

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    override fun initView() {
        update_btn.setOnClickListener {
            onShowLoading()
            updateBankList()
        }

        recycler_view.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = bankAdapter
        }

        bankAdapter.clickProveCallback = { url ->
            (activity as AppCompatActivity).addFragment(R.id.container, ImageFragment()
                .withBundleValue { putString("image", url) }
            )
        }

        bankAdapter.confirmCallback = { order ->
            context?.let {
                MaterialDialog(it).show {
                    title(R.string.confirm_order)
                    positiveButton(R.string.confirm) {
                        order.orderNo?.let { orderNo -> confirmOrder(orderNo) }
                    }
                    negativeButton(R.string.cancel2)
                }
            }
        }

        if (!SignalR.isConnected) {
            SignalR.initConnection()
        }

        val items = list.find { it.payeeBanks != null }?.payeeBanks?.map {
            "${it.bankName} ${it.accountNo} ${it.accountName}"
        }

        if (items != null) {
            banks = items.toMutableList()
        }

        getBankButton.setOnClickListener {
            context?.let {
                if (banks.isNotEmpty()) {
                    MaterialDialog(it).show {
                        listItems(items = banks)
                    }
                } else {
                    onShowLoading()
                    SignalR.invoke(
                        method = "payeeBanks",
                        App.preferenceHelper.token,
                        callback = { _, data ->
                            data.payeeBanks?.let { list ->
                                banks = list.map { bank ->
                                    "${bank.bankName} ${bank.accountNo} ${bank.accountName}"
                                }.toMutableList()
                            }
                            launch(Dispatchers.Main) {
                                onHideLoading()
                                MaterialDialog(it).show { listItems(items = banks) }
                            }
                        },
                        errorCallback = {
                            launch(Dispatchers.Main) { onHideLoading() }
                        })
                }
            }
        }

        if (App.preferenceHelper.token.isNotBlank()) {
            getOrders(App.preferenceHelper.token)
        }

        swipe_layout.setOnRefreshListener(this)
    }

    private fun confirmOrder(orderNo: String) {
        onShowLoading()
        SignalR.invoke(
            method = "commitOrder",
            App.preferenceHelper.token,
            orderNo,
            callback = { _, data ->
                onHideLoading()
                launch(Dispatchers.Main) {
                    data.slaveAccountOrders?.let { list -> bankAdapter.updateData(list) }
                }
            },
            errorCallback = { th ->
                onHideLoading()
                Timber.e("commitOrder error= ${th.message}")
                launch(Dispatchers.Main) { hideRefresh() }
            })
    }

    private fun getOrders(token: String = App.preferenceHelper.token) {
        SignalR.invoke(
            method = "orders",
            token,
            callback = { _, data ->
                launch(Dispatchers.Main) {
                    data.slaveAccountOrders?.let { list ->
                        bankAdapter.updateData(list)
                        if (list.isEmpty()) empty_msg.visibility = View.VISIBLE else View.GONE
                    }
                    hideRefresh()
                    onHideLoading()
                }
            },
            errorCallback = { th ->
                Timber.e("error= ${th.message}")
                launch(Dispatchers.Main) {
                    hideRefresh()
                    onHideLoading()
                }
            })
    }

    private fun hideRefresh() {
        swipe_layout?.isRefreshing = false
    }

    private fun updateBankList() {
        if (App.preferenceHelper.token.isNotBlank()) {
            getOrders(App.preferenceHelper.token)
        }
    }

    override fun onRefresh() {
        updateBankList()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}