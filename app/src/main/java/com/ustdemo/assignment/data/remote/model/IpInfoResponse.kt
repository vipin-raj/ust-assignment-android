package com.ustdemo.assignment.data.remote.model

import com.google.gson.annotations.SerializedName

data class IpInfoResponse(
    @SerializedName("ip")
    val ip: String,

    @SerializedName("city")
    val city: String?,

    @SerializedName("region")
    val region: String?,

    @SerializedName("country")
    val country: String?,

    @SerializedName("loc")
    val location: String?,

    @SerializedName("org")
    val organization: String?,

    @SerializedName("postal")
    val postal: String?,

    @SerializedName("timezone")
    val timezone: String?,

    @SerializedName("hostname")
    val hostname: String?
)
