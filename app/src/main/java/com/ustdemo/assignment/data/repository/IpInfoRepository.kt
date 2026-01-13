package com.ustdemo.assignment.data.repository

import com.ustdemo.assignment.data.remote.api.IpApiService
import com.ustdemo.assignment.data.remote.api.IpifyApiService
import com.ustdemo.assignment.data.remote.model.IpInfoResponse
import com.ustdemo.assignment.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IpInfoRepository @Inject constructor(
    private val ipifyApiService: IpifyApiService,
    private val ipApiService: IpApiService
) {

    suspend fun getPublicIpAddress(): Resource<String> {
        return try {
            val response = ipifyApiService.getPublicIp()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.ip)
            } else {
                Resource.Error("Failed to get public IP address")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getIpInfo(ipAddress: String): Resource<IpInfoResponse> {
        return try {
            val response = ipApiService.getIpInfo(ipAddress)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to get IP info")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
