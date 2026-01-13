package com.ustdemo.assignment.data.remote.api

import com.ustdemo.assignment.data.remote.model.IpifyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface IpifyApiService {

    @GET(".")
    suspend fun getPublicIp(
        @Query("format") format: String = "json"
    ): Response<IpifyResponse>
}
