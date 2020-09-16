package com.ttchain.githubusers.ui.smsdb

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat.startForegroundService
import com.tbruyelle.rxpermissions2.RxPermissions
import com.ttchain.githubusers.App
import com.ttchain.githubusers.R
import com.ttchain.githubusers.base.BaseFragment
import com.ttchain.githubusers.enum.Actions
import com.ttchain.githubusers.enum.ServiceState
import com.ttchain.githubusers.serivce.SmsService
import com.ttchain.githubusers.showSendToast
import com.ttchain.githubusers.toMain
import com.ttchain.githubusers.tools.ConnectionHelper
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.sms_db.*
import timber.log.Timber

class SmsDbFragment : BaseFragment() {
    private var disposable: Disposable? = null
    override val layoutId = R.layout.sms_db

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    override fun initView() {
        editTextDbServer.setText(App.preferenceHelper.dbServer)
        editTextDbName.setText(App.preferenceHelper.dbName)
        editTextDbTable.setText(App.preferenceHelper.dbTable)
        editTextDbUser.setText(App.preferenceHelper.dbUser)
        editTextDbPwd.setText(App.preferenceHelper.dbPassword)
        dbSwitch.isChecked = App.preferenceHelper.serviceState == ServiceState.STARTED
        enableEditText(!dbSwitch.isChecked)

        dbSwitch.setOnCheckedChangeListener { _, isChecked ->
            val dbServer = editTextDbServer.text.toString()
            val dbName = editTextDbName.text.toString()
            val dbTable = editTextDbTable.text.toString()
            val dbUser = editTextDbUser.text.toString()
            val dbPassword = editTextDbPwd.text.toString()

            if (isChecked) {
                if (dbServer.isBlank() || dbName.isBlank() || dbTable.isBlank() ||
                    dbUser.isBlank() || dbPassword.isBlank()
                ) {
                    childFragmentManager.showSendToast(
                        false,
                        getString(R.string.error),
                        getString(R.string.empty_error)
                    )
                    dbSwitch.isChecked = false
                } else {
                    enableEditText(false)
                    onShowLoading()
                    App.preferenceHelper.dbServer = dbServer
                    App.preferenceHelper.dbName = dbName
                    App.preferenceHelper.dbTable = dbTable
                    App.preferenceHelper.dbUser = dbUser
                    App.preferenceHelper.dbPassword = dbPassword
                    askPermission()
                }
            } else {
                actionOnService(Actions.STOP)
                enableEditText(true)
            }
        }
    }

    private fun askPermission() {
        disposable = RxPermissions(requireActivity())
            .request(Manifest.permission.READ_SMS)
            .toMain()
            .doFinally { onHideLoading() }
            .subscribe({ granted ->
                if (granted) {
                    ConnectionHelper.connectDbAndQuery(
                        callback = { actionOnService(Actions.START) },
                        errorCallback = {
                            childFragmentManager.showSendToast(
                                false,
                                getString(R.string.db_error),
                                it.message ?: "資料庫錯誤"
                            )
                            enableEditText(true)
                            dbSwitch.isChecked = false
                        })
                } else {
                    dbSwitch.isChecked = false
                }
            }, { Timber.e(" ${it.message}") })
    }

    private fun enableEditText(enable: Boolean) {
        editTextDbServer.isEnabled = enable
        editTextDbName.isEnabled = enable
        editTextDbTable.isEnabled = enable
        editTextDbUser.isEnabled = enable
        editTextDbPwd.isEnabled = enable
    }

    private fun actionOnService(actions: Actions) {
        if (App.preferenceHelper.serviceState == ServiceState.STOPPED && actions == Actions.STOP) return

        context?.let { context ->
            Intent(context, SmsService::class.java).apply {
                action = actions.name
                startForegroundService(context, this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        disposable = null
    }
}