package com.ttchain.githubusers.ui.sms

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.ttchain.githubusers.App
import com.ttchain.githubusers.R
import com.ttchain.githubusers.base.BaseFragment
import com.ttchain.githubusers.data.ReceiptMessage
import com.ttchain.githubusers.hideKeyboard
import com.ttchain.githubusers.showSendToast
import kotlinx.android.synthetic.main.sms_login.*
import microsoft.aspnet.signalr.client.LogLevel
import microsoft.aspnet.signalr.client.Logger
import microsoft.aspnet.signalr.client.Platform
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent
import microsoft.aspnet.signalr.client.hubs.HubConnection
import microsoft.aspnet.signalr.client.hubs.HubProxy
import microsoft.aspnet.signalr.client.transport.ClientTransport
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.concurrent.ExecutionException

class SmsLoginFragment : BaseFragment() {

    private var connection: HubConnection? = null
    private var transport: ClientTransport? = null
    private var proxy: HubProxy? = null

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
        Platform.loadPlatformComponent(AndroidPlatformComponent())

//        if (connection == null) {
        connection = HubConnection("http://18.177.24.213:63339/")
//        }

        val logger = Logger { message: String?, logLevel: LogLevel? ->
            Timber.i("Logger LogLevel= ${logLevel}, Message= $message")
        }

        transport = ServerSentEventsTransport(logger)
        proxy = connection?.createHubProxy("smsReceiptHub")

        connection?.error { throwable ->
            throwable.printStackTrace()
            Timber.e("connection error ${throwable.message}")
        }

        // Subscribe to the connected event
        connection?.connected { Timber.i("SignalR onConnected") }

        // Subscribe to the closed event
        connection?.closed { Timber.i("SignalR onClosed") }

        // Subscribe to the received event
        connection?.received { json ->
            Timber.i("received $json")
        }

        // Subscribe to the received event
        proxy?.on(
            "notification", { message: ReceiptMessage? ->
                Timber.i(" message= $message")
            },
            ReceiptMessage::class.java
        )

        val signalRFuture = connection?.start(transport)

        Timber.i("signalRFuture get()")
        try {
            signalRFuture!!.get()
        } catch (e: InterruptedException) {
            Timber.e("InterruptedException= %s", e.toString())
            return
        } catch (e: ExecutionException) {
            Timber.e("ExecutionException= $e")
            return
        }

        editTextApiAddress.setText(App.preferenceHelper.userHost)
        editTextAccount.setText(App.preferenceHelper.userAccount)
        editTextPassword.setText(App.preferenceHelper.userPassword)

        testButton.visibility = View.VISIBLE
        testButton.setOnClickListener {
            editTextApiAddress.setText("http://18.177.24.213:63339")
            editTextAccount.setText("merchant12")
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
//                viewModel.login(loginId, password)

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

    override fun onDestroy() {
        super.onDestroy()

        connection?.apply {
            disconnect()
            stop()
        }
        connection = null
        proxy = null
    }
}