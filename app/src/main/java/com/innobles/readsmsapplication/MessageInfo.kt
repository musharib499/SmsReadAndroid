package com.innobles.readsmsapplication


import com.google.gson.annotations.SerializedName

data class MessageInfo(
    @SerializedName("date")
    var date: String?, // 2423424
    @SerializedName("message")
    var message: String?,
    @SerializedName("phoneNumber")
    var smsProvider: String?
)