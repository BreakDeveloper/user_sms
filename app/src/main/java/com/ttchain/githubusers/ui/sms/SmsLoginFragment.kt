package com.ttchain.githubusers.ui.sms

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.ttchain.githubusers.App
import com.ttchain.githubusers.R
import com.ttchain.githubusers.base.BaseFragment
import com.ttchain.githubusers.hideKeyboard
import com.ttchain.githubusers.showSendToast
import kotlinx.android.synthetic.main.sms_login.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SmsLoginFragment : BaseFragment() {

    companion object {
        fun newInstance() = SmsLoginFragment()
        const val loginPath = "acceptor/receipt/login"
    }

    override val layoutId: Int
        get() = R.layout.sms_login

    private val viewModel by sharedViewModel<SmsViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        initData()
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        editTextApiAddress.setText(App.preferenceHelper.userHost)
        editTextAccount.setText(App.preferenceHelper.userAccount)
        editTextPassword.setText(App.preferenceHelper.userPassword)

        testButton.visibility = View.VISIBLE
        testButton.setOnClickListener {
            editTextApiAddress.setText("https://api.fandc.site")
            editTextAccount.setText("jsttestaa01")
            editTextPassword.setText("jsttestaa01")
        }

        loginButton.setOnClickListener {
            requireActivity().hideKeyboard()
            val apiAddress = editTextApiAddress.text.toString()
            App.apiAddress = when {
                apiAddress.contains(loginPath, true) ->
                    apiAddress.replace(loginPath, "")
                else -> apiAddress
            }
            val loginId = editTextAccount.text.toString()
            val password = editTextPassword.text.toString()
            if (apiAddress.isBlank() || loginId.isBlank() || password.isBlank()) {
                childFragmentManager.showSendToast(
                    false,
                    getString(R.string.error),
                    getString(R.string.empty_error)
                )
            } else {
                onShowLoading()
                App.preferenceHelper.userHost = apiAddress
                App.preferenceHelper.userAccount = loginId
                App.preferenceHelper.userPassword = password
                viewModel.login(loginId, password)
            }
        }
    }

    private fun initData() {
        viewModel.apply {
            loginError.observe(viewLifecycleOwner) {
                onHideLoading()
                childFragmentManager.showSendToast(false, getString(R.string.error), it)
            }
        }
    }
}