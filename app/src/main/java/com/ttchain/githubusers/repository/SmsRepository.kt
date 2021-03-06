package com.ttchain.githubusers.repository

import com.ttchain.githubusers.data.*
import com.ttchain.githubusers.net.RepositoryResult
import com.ttchain.githubusers.net.SmsApi
import io.reactivex.Single

class SmsRepository(private var smsApi: SmsApi) : BaseRepository() {

    fun login(loginId: String, password: String): Single<RepositoryResult<LoginResponse>> {
        val request = LoginRequest(loginId, password)
        return getApi(smsApi.acceptorLogin(request))
            .map { checkResultRepository(it) }
    }

//    fun bank(
//        loginId: String,
//        bankAccountNo: String,
//        hash: String
//    ): Single<RepositoryResult<Boolean>> {
//        val request = BankRequest(loginId, bankAccountNo, hash)
//        return getApi(smsApi.acceptorBank(request))
//            .map { checkResultRepository(it) }
//    }

    fun bank(
        deviceId: String,
        loginId: String,
        bankAccounts: String,
        hash: String
    ): Single<RepositoryResult<Boolean>> {
//        val request = BankRequest(loginId, bankAccountNo, hash)
        val request = ReceiptBankRequest(deviceId, loginId, bankAccounts, hash)
        return getApi(smsApi.acceptorReceiptBank(request))
            .map { checkResultRepository(it) }
    }

    fun receipt(
        loginId: String,
        bankAccountNo: String,
        message: String,
        hash: String
    ): Single<RepositoryResult<Boolean>> {
        val request = ReceiptRequest(loginId, bankAccountNo, message, hash)
        return getApi(smsApi.acceptorReceipt(request))
            .map { checkResultRepository(it) }
    }
}