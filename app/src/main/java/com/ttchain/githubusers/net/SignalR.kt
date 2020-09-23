package com.ttchain.githubusers.net

import com.google.gson.Gson
import com.ttchain.githubusers.App
import com.ttchain.githubusers.data.ReceiptMessage
import com.ttchain.githubusers.fromJson
import com.ttchain.githubusers.toMain
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import microsoft.aspnet.signalr.client.LogLevel
import microsoft.aspnet.signalr.client.Logger
import microsoft.aspnet.signalr.client.Platform
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent
import microsoft.aspnet.signalr.client.hubs.HubConnection
import microsoft.aspnet.signalr.client.hubs.HubProxy
import microsoft.aspnet.signalr.client.transport.ClientTransport
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport
import timber.log.Timber
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

object SignalR {
    private var connection: HubConnection? = null
    private var transport: ClientTransport? = null
    private var proxy: HubProxy? = null
    var isConnected: Boolean = false
        private set

    private val monitorCallbackList: MutableList<((msg: String, data: ReceiptMessage) -> Unit)> =
        mutableListOf()

    private var disp: Disposable? = null

    var callback: ((msg: String, data: ReceiptMessage) -> Unit)? = null

    var errorCallback: ((Throwable) -> Unit)? = null

    fun addMonitorCallback(monitorCallback: (msg: String, data: ReceiptMessage) -> Unit) {
        monitorCallbackList.add(monitorCallback)
    }

    fun invoke(method: String, errorCallback: (Throwable) -> Unit = {}) {
        Timber.i("invoke method= $method")
        proxy?.invoke(String::class.java, method)
        this.errorCallback = errorCallback
    }

    fun invoke(
        method: String,
        vararg args: Any,
        callback: (msg: String, data: ReceiptMessage) -> Unit,
        errorCallback: (Throwable) -> Unit = {}
    ) {
        Timber.i("invoke method= $method")
        proxy?.invoke(String::class.java, method, *args)
        this.callback = callback
        this.errorCallback = errorCallback
    }

    fun disconnection() {
        if (isConnected) {
            connection?.disconnect()
            connection?.stop()
            connection = null
        }
        proxy = null
        isConnected = false
    }

    fun clearMonitorCallback() {
        monitorCallbackList.clear()
    }

    fun initConnection(url: String = App.preferenceHelper.userHost, initCallback: () -> Unit = {}) {
        Timber.i("initConnection")
        Platform.loadPlatformComponent(AndroidPlatformComponent())

        connection = HubConnection(url)

        val logger = Logger { message: String?, logLevel: LogLevel? ->
//            Timber.i("Logger LogLevel= ${logLevel}, Message= $message")
        }

        transport = ServerSentEventsTransport(logger)
        proxy = connection?.createHubProxy("smsReceiptHub")

        connection?.error { throwable ->
            throwable.printStackTrace()
            Timber.e("connection error ${throwable.message}")
            errorCallback?.invoke(throwable)
            isConnected = false
        }

        connection?.reconnecting {
            disp = Observable.interval(5, TimeUnit.SECONDS)
                .startWith(1)
                .toMain()
                .subscribe(
                    {
                        connection?.let { conn ->
                            val signalRFuture = conn.start(transport)
                            try {
                                signalRFuture?.get()
                                isConnected = true
                                disp?.dispose()
                                disp = null
                            } catch (exception: Exception) {
                                exception.printStackTrace()
                            }
                        }
                    },
                    { Timber.e(" ${it.message}") }
                )
        }

        // Subscribe to the connected event
        connection?.connected { Timber.i("SignalR onConnected") }

        // Subscribe to the closed event
        connection?.closed { Timber.i("SignalR onClosed") }

        // Subscribe to the received event
        proxy?.on(
            "notification",
            { msg: String?, data: String? ->
                Timber.i("msg= $msg, data= $data")
                if (msg != null && data != null) {
                    val receiptMessage: ReceiptMessage = Gson().fromJson(data)
                    callback?.invoke(msg, receiptMessage)
                    monitorCallbackList.forEach { it.invoke(msg, receiptMessage) }
                }
            },
            String::class.java, String::class.java
        )

        val signalRFuture = connection?.start(transport)

        try {
            signalRFuture?.get()
            isConnected = true
            initCallback.invoke()
            return
        } catch (e: InterruptedException) {
            Timber.e("InterruptedException= %s", e.toString())
        } catch (e: ExecutionException) {
            Timber.e("ExecutionException= $e")
        }
        isConnected = false
        return
    }
}