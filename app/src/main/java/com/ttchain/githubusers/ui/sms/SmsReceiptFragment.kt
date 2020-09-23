package com.ttchain.githubusers.ui.sms

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.tbruyelle.rxpermissions2.RxPermissions
import com.ttchain.githubusers.*
import com.ttchain.githubusers.base.BaseFragment
import com.ttchain.githubusers.data.ReceiptMessage
import com.ttchain.githubusers.net.SignalR
import com.ttchain.githubusers.tools.SMSContentObserver
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.sms_receipt.*
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SmsReceiptFragment : BaseFragment(), SMSContentObserver.MessageListener,
    CoroutineScope by MainScope() {

    private val list by getBundleValue("login_success", mutableListOf<ReceiptMessage>())
    private var banks = mutableListOf<String>()

    override val layoutId = R.layout.sms_receipt

    private val viewModel by sharedViewModel<SmsViewModel>()
    private var smsContentObserver: SMSContentObserver? = null

    private var disp: Disposable? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        testButton.visibility = View.GONE
        testButton.setOnClickListener { getSwitchBank() }

        logoutButton.setOnClickListener {
            SignalR.invoke("logout")
            App.preferenceHelper.token = ""
            activity?.supportFragmentManager?.popBackStack()
        }

        bank_switch.isEnabled = false

        SignalR.addMonitorCallback { msg, data ->
            if (msg == "error" && data.code == 9006) {
                App.preferenceHelper.token = ""
                Timber.i("token expired")
                launch(Dispatchers.Main) {
                    activity?.supportFragmentManager?.popBackStack()
                }
            } else {
                Timber.i("Monitor msg = $msg, data= $data")
            }
        }

        list.find { it.isActive != null }?.isActive?.let {
            bank_switch.isChecked = it
        }

        if (!SignalR.isConnected) {
            SignalR.initConnection(App.preferenceHelper.userHost)
        }

        bank_switch.setOnCheckedChangeListener { _, isChecked ->
            switchBank(isChecked)
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
                    MaterialDialog(it).show { listItems(items = banks) }
                } else {
                    onShowLoading()
                    SignalR.invoke(
                        method = "payeeBanks",
                        App.preferenceHelper.token,
                        callback = { _, data ->
                            onHideLoading()
                            data.payeeBanks?.let { list ->
                                banks = list.map { bank ->
                                    "${bank.bankName} ${bank.accountNo} ${bank.accountName}"
                                }.toMutableList()
                            }
                            launch(Dispatchers.Main) {
                                MaterialDialog(it).show {
                                    listItems(items = banks)
                                }
                            }
                        },
                        errorCallback = { onHideLoading() })
                }
            }
        }

        viewModel.receiptText = ""
        startButton.setOnClickListener {
            requireActivity().hideKeyboard()
            RxPermissions(requireActivity())
                .request(Manifest.permission.READ_SMS)
                .toMain()
                .subscribe { granted ->
                    if (granted) {
                        startButton.isEnabled = false
                        cancelButton.isEnabled = true
                        val originalText = textResult.text
                        textResult.text = "開始\n\n$originalText"
                        registerSMSObserver()
                    }
                }
        }
        cancelButton.setOnClickListener {
            startButton.isEnabled = true
            cancelButton.isEnabled = false

            val originalText = textResult.text
            textResult.text = "取消\n\n$originalText"
        }

        disp = Observable.create<String> { it.onNext("") }
            .delay(1, TimeUnit.SECONDS)
            .toMain()
            .subscribe(
                { getSwitchBank() },
                { Timber.i(" ${it.message}") }
            )
    }

    private fun getSwitchBank() {
        SignalR.invoke(
            method = "payeeBankStatus",
            App.preferenceHelper.token,
            callback = { _, data ->
                data.isActive?.let {
                    launch(Dispatchers.Main) {
                        bank_switch.isChecked = it
                        bank_switch.isEnabled = true
                    }
                }
            },
            errorCallback = { th ->
                bank_switch.isEnabled = true
                Timber.e("error= ${th.message}")
            })
    }

    private fun switchBank(isChecked: Boolean) {
        SignalR.invoke(
            method = "switchBank",
            App.preferenceHelper.token,
            isChecked,
            callback = { msg, data ->
                if (msg == "systemMessage") {
                    launch(Dispatchers.Main) {
                        childFragmentManager.showSendToast(
                            true,
                            getString(R.string.correct),
                            data.message ?: getString(R.string.correct)
                        )
                    }
                }
            },
            errorCallback = { th ->
                Timber.e("error= ${th.message}")
            })
    }

    private fun sendSmsContent(message: String) {
        SignalR.invoke(
            method = "smsContent",
            App.preferenceHelper.token,
            message,
            callback = { _, _ -> },
            errorCallback = { th -> Timber.e("error= ${th.message}") })
    }

    @SuppressLint("SetTextI18n")
    override fun onReceived(message: String?) {
        val originalText = textResult.text
        textResult.text = message + "\n\n" + originalText
        sendSmsContent(message.orEmpty())
    }

    private fun registerSMSObserver() {
        childFragmentManager.showSendToast(
            true,
            getString(R.string.correct),
            getString(R.string.start_sms_catcher)
        )
        if (smsContentObserver == null) {
            smsContentObserver = SMSContentObserver(requireContext(), view?.handler)
            smsContentObserver?.register(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        smsContentObserver?.unRegister()
        smsContentObserver = null
    }

    override fun onDestroy() {
        super.onDestroy()
        disp?.dispose()
        disp = null
        cancel()
    }
}