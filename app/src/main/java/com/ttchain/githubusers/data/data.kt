package com.ttchain.githubusers.data

import com.google.gson.annotations.SerializedName
import com.ttchain.githubusers.net.ApiCodeEnum

data class UserListData(
    @SerializedName("gists_url")
    val gistsUrl: String? = "",
    @SerializedName("repos_url")
    val reposUrl: String? = "",
    @SerializedName("following_url")
    val followingUrl: String? = "",
    @SerializedName("starred_url")
    val starredUrl: String? = "",
    @SerializedName("login")
    val login: String? = "",
    @SerializedName("followers_url")
    val followersUrl: String? = "",
    @SerializedName("type")
    val type: String? = "",
    @SerializedName("url")
    val url: String? = "",
    @SerializedName("subscriptions_url")
    val subscriptionsUrl: String? = "",
    @SerializedName("received_events_url")
    val receivedEventsUrl: String? = "",
    @SerializedName("avatar_url")
    val avatarUrl: String? = "",
    @SerializedName("events_url")
    val eventsUrl: String? = "",
    @SerializedName("html_url")
    val htmlUrl: String? = "",
    @SerializedName("site_admin")
    val siteAdmin: Boolean = false,
    @SerializedName("id")
    val id: Int? = 0,
    @SerializedName("gravatar_id")
    val gravatarId: String? = "",
    @SerializedName("node_id")
    val nodeId: String? = "",
    @SerializedName("organizations_url")
    val organizationsUrl: String? = ""
)

data class UserData(
    @SerializedName("gists_url")
    val gistsUrl: String? = "",
    @SerializedName("repos_url")
    val reposUrl: String? = "",
    @SerializedName("following_url")
    val followingUrl: String? = "",
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = "",
    @SerializedName("login")
    val login: String? = "",
    @SerializedName("type")
    val type: String? = "",
    @SerializedName("blog")
    val blog: String? = "",
    @SerializedName("subscriptions_url")
    val subscriptionsUrl: String? = "",
    @SerializedName("updated_at")
    val updatedAt: String? = "",
    @SerializedName("site_admin")
    val siteAdmin: Boolean = false,
    @SerializedName("company")
    val company: String? = "",
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("public_repos")
    val publicRepos: Int = 0,
    @SerializedName("gravatar_id")
    val gravatarId: String? = "",
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("organizations_url")
    val organizationsUrl: String? = "",
    @SerializedName("hireable")
    val hireable: Boolean = false,
    @SerializedName("starred_url")
    val starredUrl: String? = "",
    @SerializedName("followers_url")
    val followersUrl: String? = "",
    @SerializedName("public_gists")
    val publicGists: Int = 0,
    @SerializedName("url")
    val url: String? = "",
    @SerializedName("received_events_url")
    val receivedEventsUrl: String? = "",
    @SerializedName("followers")
    val followers: Int = 0,
    @SerializedName("avatar_url")
    val avatarUrl: String? = "",
    @SerializedName("events_url")
    val eventsUrl: String? = "",
    @SerializedName("html_url")
    val htmlUrl: String? = "",
    @SerializedName("following")
    val following: Int = 0,
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("location")
    val location: String? = "",
    @SerializedName("node_id")
    val nodeId: String? = ""
)

/**
 * 統一 Sms Api Result
 */
data class ApiResult<T>(
    @SerializedName("code")
    val code: ApiCodeEnum = ApiCodeEnum.NUMBER_1,
    val message: String = "",
    val data: T
)

/**
 * Sms內容
 */
data class SmsInfo(
    val smsBody: String?,
    val phoneNumber: String?,
    val date: String?,
    val name: String?,
    val type: Int?
)

data class LoginRequest(
    var loginId: String?,
    var password: String?
)

data class LoginResponse(
    var secretKey: String?
//    var secretKey: String?,
//    var callbackDomain: String?,
//    var callbackEndpoint: String?
)

data class BankRequest(
    var loginId: String?,
    var bankAccountNo: String?,
    var hash: String?
)

data class ReceiptBankRequest(
    var deviceId: String?,
    var loginId: String?,
    var bankAccounts: String?,
    var hash: String?
)

data class ReceiptRequest(
    var loginId: String?,
    var bankAccountNo: String?,
    var message: String?,
    var hash: String?
)