package com.ttchain.githubusers.tools

import com.ttchain.githubusers.App
import com.ttchain.githubusers.getDbUrl
import com.ttchain.githubusers.toMain
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.sql.Connection
import java.sql.DriverManager

class ConnectionHelper {
    companion object {
        private const val SQL_DRIVER = "net.sourceforge.jtds.jdbc.Driver"
        const val DB_URL =
            "jdbc:jtds:sqlserver://54.180.61.184:1433;databasename=TTChain;user=allen.li;password=atlrnat17;"

        fun connectDbAndInsert(smsContent: String) {
            var connection: Connection? = null
            var disposable: Disposable? = null

            disposable = Observable.just(SQL_DRIVER)
                .map {
                    Class.forName(it).newInstance()
                    connection = DriverManager.getConnection(getDbUrl())
                    val query =
                        "INSERT INTO ${App.preferenceHelper.dbTable} (Content, Status, CreateDate) values (N'${smsContent}','0','${TimeUtils.getNowTimeStr()}')"
                    val pat = connection!!.prepareStatement(query)
                    pat.executeUpdate()
                    pat.close()
                }
                .toMain()
                .doFinally {
                    connection?.close()
                    disposable?.dispose()
                    disposable = null
                }
                .subscribe(
                    { connectDbAndQuery() },
                    { Timber.e(" ${it.message}") }
                )
        }

        fun connectDbAndQuery(
            callback: () -> Unit = {},
            errorCallback: (Throwable) -> Unit = {},
            finalCallback: () -> Unit = {}
        ) {
            var connection: Connection? = null
            var disposable: Disposable? = null

            disposable = Observable.just(SQL_DRIVER)
                .map {
                    Class.forName(it).newInstance()
                    connection = DriverManager.getConnection(getDbUrl())
                    val query = "SELECT * FROM ${App.preferenceHelper.dbTable} ORDER BY ID ASC"
                    val pat = connection!!.prepareStatement(query)
                    val rs = pat.executeQuery()
                    var isPrintFirst = true
                    while (rs.next()) {
                        val orderId = rs.getString(2)
                        if (isPrintFirst) {
                            Timber.i("content= $orderId")
                            isPrintFirst = false
                        }
                    }
                    rs.close()
                    pat.close()
                }
                .toMain()
                .doFinally {
                    connection?.close()
                    disposable?.dispose()
                    disposable = null
                    finalCallback.invoke()
                }
                .subscribe(
                    { callback.invoke() },
                    {
                        errorCallback.invoke(it)
                        Timber.e(" ${it.message}")
                    }
                )
        }
    }
}