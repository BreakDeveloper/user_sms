package com.ttchain.githubusers.ui.main

import androidx.lifecycle.MutableLiveData
import com.ttchain.githubusers.base.BaseViewModel
import com.ttchain.githubusers.data.UserListData
import com.ttchain.githubusers.repository.GitHubRepository

class MainViewModel(
    private val gitHubRepository: GitHubRepository
) : BaseViewModel() {

    var source = 0
    val userListResult = MutableLiveData<List<UserListData>>()

    fun getUserList(since: Int) {
        add(
            gitHubRepository.getUserList(since)
                .subscribe({
                    userListResult.value = it
                }, {
                })
        )
    }
}
