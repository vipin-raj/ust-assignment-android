package com.ustdemo.assignment.data.remote.api

import com.ustdemo.assignment.data.remote.model.IpInfoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface IpApiService {

    @GET("{ip}/geo")
    suspend fun getIpInfo(
        @Path("ip") ipAddress: String
    ): Response<IpInfoResponse>
}
