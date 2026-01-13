package com.ustdemo.assignment.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ustdemo.assignment.data.remote.model.IpInfoResponse
import com.ustdemo.assignment.data.repository.IpInfoRepository
import com.ustdemo.assignment.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val ipInfoRepository: IpInfoRepository
) : ViewModel() {

    private val _publicIpState = MutableLiveData<PublicIpState>()
    val publicIpState: LiveData<PublicIpState> = _publicIpState

    private val _ipInfoState = MutableLiveData<IpInfoState>()
    val ipInfoState: LiveData<IpInfoState> = _ipInfoState

    private var currentPublicIp: String? = null

    fun loadPublicIpInfo() {
        viewModelScope.launch {
            _publicIpState.value = PublicIpState.Loading

            when (val result = ipInfoRepository.getPublicIpAddress()) {
                is Resource.Success -> {
                    currentPublicIp = result.data
                    _publicIpState.value = PublicIpState.Success(result.data!!)
                    // Now fetch detailed info about this IP
                    loadIpDetails(result.data)
                }
                is Resource.Error -> {
                    _publicIpState.value = PublicIpState.Error(result.message ?: "Failed to get public IP")
                }
                is Resource.Loading -> {
                    _publicIpState.value = PublicIpState.Loading
                }
            }
        }
    }

    private fun loadIpDetails(ipAddress: String) {
        viewModelScope.launch {
            _ipInfoState.value = IpInfoState.Loading

            when (val result = ipInfoRepository.getIpInfo(ipAddress)) {
                is Resource.Success -> {
                    _ipInfoState.value = IpInfoState.Success(result.data!!)
                }
                is Resource.Error -> {
                    _ipInfoState.value = IpInfoState.Error(result.message ?: "Failed to get IP details")
                }
                is Resource.Loading -> {
                    _ipInfoState.value = IpInfoState.Loading
                }
            }
        }
    }

    fun retry() {
        loadPublicIpInfo()
    }

    sealed class PublicIpState {
        object Loading : PublicIpState()
        data class Success(val ip: String) : PublicIpState()
        data class Error(val message: String) : PublicIpState()
    }

    sealed class IpInfoState {
        object Loading : IpInfoState()
        data class Success(val ipInfo: IpInfoResponse) : IpInfoState()
        data class Error(val message: String) : IpInfoState()
    }
}
