package com.ttchain.githubusers.ui.smsdb

import android.os.Bundle
import com.ttchain.githubusers.App
import com.ttchain.githubusers.R
import com.ttchain.githubusers.base.BaseFragment
import com.ttchain.githubusers.showSendToast
import com.ttchain.githubusers.tools.ConnectionHelper
import kotlinx.android.synthetic.main.sms_db.*
import timber.log.Timber
import kotlin.random.Random

class SmsDbFragment : BaseFragment() {
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

        dbSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
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
                } else {
                    enableEditText(false)
                    onShowLoading()
                    App.preferenceHelper.dbServer = dbServer
                    App.preferenceHelper.dbName = dbName
                    App.preferenceHelper.dbTable = dbTable
                    App.preferenceHelper.dbUser = dbUser
                    App.preferenceHelper.dbPassword = dbPassword

                    ConnectionHelper.connectDbAndQuery(
                        finalCallback = { onHideLoading() },
                        callback = {
                            Timber.i("StartService")
                            ConnectionHelper.connectDbAndInsert("test_${Random.nextInt()}")
                        },
                        errorCallback = {
                            childFragmentManager.showSendToast(
                                false,
                                getString(R.string.db_error),
                                it.message ?: "資料庫錯誤"
                            )
                            enableEditText(true)
                            compoundButton.isChecked = false
                        })
                }
            } else {
                enableEditText(true)
            }
        }
    }

    private fun enableEditText(enable: Boolean) {
        editTextDbServer.isEnabled = enable
        editTextDbName.isEnabled = enable
        editTextDbTable.isEnabled = enable
        editTextDbUser.isEnabled = enable
        editTextDbPwd.isEnabled = enable
    }
}