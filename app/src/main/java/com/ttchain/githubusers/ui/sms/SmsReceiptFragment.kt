package com.ttchain.githubusers.ui.sms

import android.Manifest
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.observe
import com.tbruyelle.rxpermissions2.RxPermissions
import com.ttchain.githubusers.*
import com.ttchain.githubusers.base.BaseFragment
import com.ttchain.githubusers.tools.SMSContentObserver
import com.ttchain.githubusers.tools.isBankAccountNo
import kotlinx.android.synthetic.main.sms_receipt.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SmsReceiptFragment : BaseFragment(), SMSContentObserver.MessageListener {

    companion object {
        fun newInstance() = SmsReceiptFragment()
    }

    override val layoutId: Int
        get() = R.layout.sms_receipt

    private val viewModel by sharedViewModel<SmsViewModel>()
    private var smsContentObserver: SMSContentObserver? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        initData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        smsContentObserver?.unRegister()
        smsContentObserver = null
    }

    override fun initView() {
        viewModel.receiptText = ""
        startButton.setOnClickListener {
            requireActivity().hideKeyboard()
            RxPermissions(requireActivity())
                .request(
                    Manifest.permission.READ_SMS
                )
                .toMain()
                .subscribe { granted ->
                    if (granted) {
                        val bankAccountNumber = editTextBankAccountNo.text.toString()
                        viewModel.bankAccountNumber = bankAccountNumber
                        if (bankAccountNumber.isBlank()|| !isBankAccountNo(bankAccountNumber)) {
                            childFragmentManager.showSendToast(
                                false,
                                getString(R.string.error),
                                getString(R.string.empty_error)
                            )
                        } else {
                            editTextBankAccountNo.isEnabled = false
                            startButton.isEnabled = false
                            cancelButton.isEnabled = true
                            onShowLoading()
                            viewModel.checkBankNumber()

                            val originalText = textResult.text
                            textResult.text ="開始\n\n" + originalText
                        }
                    } else {
                        startSettingsActivity()
                    }
                }
        }
        cancelButton.setOnClickListener {
            editTextBankAccountNo.text.clear()
            editTextBankAccountNo.isEnabled = true
            startButton.isEnabled = true
            cancelButton.isEnabled = false

            val originalText = textResult.text
            textResult.text ="取消\n\n" + originalText
        }
    }

    private fun initData() {
        viewModel.apply {
            bankResult.observe(viewLifecycleOwner) {
                onHideLoading()
                if (it) registerSMSObserver()
            }
            bankError.observe(viewLifecycleOwner) {
                onHideLoading()
                childFragmentManager.showSendToast(false, getString(R.string.error), it)
            }
            receiptResult.observe(viewLifecycleOwner) {
                collectText(it)
            }
            receiptError.observe(viewLifecycleOwner) {
                collectText(it)
            }
        }
    }

    override fun onReceived(message: String?) {
        val originalText = textResult.text
        textResult.text = message + "\n\n" + originalText
        viewModel.receipt(message.orEmpty())
    }

    /**
     * 註冊簡訊觀察器
     */
    private fun registerSMSObserver() {
        childFragmentManager.showSendToast(
            true,
            getString(R.string.correct),
            getString(R.string.start_sms_catcher)
        )
        if (smsContentObserver == null) {
            smsContentObserver = SMSContentObserver(requireContext(), Handler())
            smsContentObserver?.register(this)
        }
    }

    private fun collectText(text: String) {
        viewModel.apply {
            when {
                receiptText.isBlank() -> {
                    receiptText.plus(text)
                }
                else -> {
                    receiptText.plus("\n$text")
                }
            }
        }
    }
}