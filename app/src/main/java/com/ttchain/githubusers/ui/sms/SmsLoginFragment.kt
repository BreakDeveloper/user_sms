package com.ttchain.githubusers.ui.sms

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.ttchain.githubusers.App
import com.ttchain.githubusers.R
import com.ttchain.githubusers.base.BaseFragment
import com.ttchain.githubusers.data.ReceiptMessage
import com.ttchain.githubusers.hideKeyboard
import com.ttchain.githubusers.net.SignalR
import com.ttchain.githubusers.showSendToast
import kotlinx.android.synthetic.main.sms_login.*
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import retrofit2.HttpException
import timber.log.Timber

class SmsLoginFragment : BaseFragment(), CoroutineScope by MainScope() {

    companion object {
        fun newInstance() = SmsLoginFragment()
        const val loginPath = "acceptor/receipt/login"
    }

    override val layoutId = R.layout.sms_login

    private val viewModel by sharedViewModel<SmsViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        editTextApiAddress.setText(App.preferenceHelper.userHost)
        editTextAccount.setText(App.preferenceHelper.userAccount)
        editTextPassword.setText(App.preferenceHelper.userPassword)

        testButton.visibility = View.GONE
        testButton.setOnClickListener {
            editTextApiAddress.setText("http://18.177.24.213:63339/")
            editTextAccount.setText("Merchant11")
            editTextPassword.setText("aaaa1234")
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

                if (!SignalR.isConnected) {
                    SignalR.initConnection(App.preferenceHelper.userHost)
                }

                val list = arrayListOf<ReceiptMessage>()
                var hasLogin = false
                var hasBankList = false
                var hasActive = false

                SignalR.invoke(
                    "login",
                    App.preferenceHelper.userAccount,
                    App.preferenceHelper.userPassword,
                    callback = { msg, data ->
                        if (msg == "error") {
                            onHideLoading()
                            launch(Dispatchers.Main) {
                                childFragmentManager.showSendToast(
                                    false,
                                    getString(R.string.error),
                                    data.message ?: "請重新登入"
                                )
                            }
                            SignalR.invoke("logout")
                            return@invoke
                        } else if (msg != "systemMessage") {
                            list.add(data)
                        }

                        data.token?.let {
                            hasLogin = true
                            App.preferenceHelper.token = it
                        }
                        data.payeeBanks?.let { hasBankList = true }
                        data.isActive?.let { hasActive = true }

                        if (hasLogin && hasBankList && hasActive) {
                            val passList = mutableListOf<ReceiptMessage>()
                            passList.addAll(list)
                            viewModel.loginResult.postValue(passList)
                            onHideLoading()
                            list.clear()
                            hasLogin = false
                            hasBankList = false
                            hasActive = false
                        }
                    },
                    errorCallback = { throwable ->
                        if (throwable is HttpException) {
                            val errorMsg = throwable.response()?.errorBody()!!.string()
                            Timber.i("errorMsg= $errorMsg")
                        }

                        launch(Dispatchers.Main) {
                            onHideLoading()
                            throwable.message?.let {
                                childFragmentManager.showSendToast(
                                    false,
                                    getString(R.string.error),
                                    it
                                )
                            }
                        }
                    }
                )
            }
        }

        if (App.preferenceHelper.token.isNotBlank()) {
            val list: MutableList<ReceiptMessage> = mutableListOf()
            list.add(ReceiptMessage(token = App.preferenceHelper.token))
            viewModel.loginResult.postValue(list)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}