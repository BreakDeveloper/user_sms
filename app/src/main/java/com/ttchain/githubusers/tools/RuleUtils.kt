package com.ttchain.githubusers.tools

import java.util.regex.Pattern

private const val BANK_ACCOUNT_NO_EXPRESSION = "^([0-9]*)((,[0-9]+)?)*"

fun isBankAccountNo(content: String): Boolean {
    return  Pattern.compile(BANK_ACCOUNT_NO_EXPRESSION).matcher(content).matches()
}