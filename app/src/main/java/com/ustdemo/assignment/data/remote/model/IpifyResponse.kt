package com.ustdemo.assignment.data.remote.model

import com.google.gson.annotations.SerializedName

data class IpifyResponse(
    @SerializedName("ip")
    val ip: String
)
