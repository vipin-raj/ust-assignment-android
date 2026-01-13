package com.ustdemo.assignment.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ustdemo.assignment.data.local.entity.DeviceEntity
import com.ustdemo.assignment.data.repository.AuthRepository
import com.ustdemo.assignment.data.repository.DeviceRepository
import com.ustdemo.assignment.discovery.NsdDiscoveryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val authRepository: AuthRepository,
    private val nsdDiscoveryManager: NsdDiscoveryManager
) : ViewModel() {

    private val _devices = MutableLiveData<List<DeviceEntity>>()
    val devices: LiveData<List<DeviceEntity>> = _devices

    private val _discoveryState = MutableLiveData<DiscoveryState>()
    val discoveryState: LiveData<DiscoveryState> = _discoveryState

    private val _logoutState = MutableLiveData<Boolean>()
    val logoutState: LiveData<Boolean> = _logoutState

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private var discoveryJob: Job? = null
    private var timeoutJob: Job? = null
    private val discoveredDevices = mutableMapOf<String, DeviceEntity>()

    companion object {
        private const val DISCOVERY_TIMEOUT_MS = 10000L // 10 seconds
    }

    init {
        loadCachedDevices()
        loadUserName()
    }

    private fun loadUserName() {
        val user = authRepository.currentUser
        _userName.value = user?.displayName ?: "User"
    }

    private fun loadCachedDevices() {
        viewModelScope.launch {
            deviceRepository.getAllDevices().collectLatest { devices ->
                _devices.value = devices
            }
        }
    }

    fun startDiscovery() {
        if (discoveryJob?.isActive == true) {
            return
        }

        // Start timeout timer
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            delay(DISCOVERY_TIMEOUT_MS)
            stopDiscovery()
        }

        discoveryJob = viewModelScope.launch {
            // Mark all existing devices as offline before starting discovery
            deviceRepository.markAllDevicesOffline()
            discoveredDevices.clear()

            _discoveryState.value = DiscoveryState.Discovering

            nsdDiscoveryManager.discoverDevices()
                .catch { e ->
                    _discoveryState.value = DiscoveryState.Error(e.message ?: "Discovery failed")
                }
                .collect { event ->
                    when (event) {
                        is NsdDiscoveryManager.DiscoveryEvent.DiscoveryStarted -> {
                            _discoveryState.value = DiscoveryState.Discovering
                        }
                        is NsdDiscoveryManager.DiscoveryEvent.DeviceFound -> {
                            handleDeviceFound(event.device)
                        }
                        is NsdDiscoveryManager.DiscoveryEvent.DeviceLost -> {
                            handleDeviceLost(event.deviceName)
                        }
                        is NsdDiscoveryManager.DiscoveryEvent.Error -> {
                            _discoveryState.value = DiscoveryState.Error(event.message)
                        }
                    }
                }
        }
    }

    private suspend fun handleDeviceFound(device: DeviceEntity) {
        // Avoid duplicates by IP address
        if (!discoveredDevices.containsKey(device.ipAddress)) {
            discoveredDevices[device.ipAddress] = device

            // Check if device already exists in database
            val existingDevice = deviceRepository.getDeviceByIp(device.ipAddress)
            if (existingDevice != null) {
                // Update existing device status
                deviceRepository.updateDeviceStatus(device.ipAddress, true)
            } else {
                // Insert new device
                deviceRepository.insertDevice(device)
            }

            _discoveryState.value = DiscoveryState.DeviceFound(device)
        }
    }

    private suspend fun handleDeviceLost(deviceName: String) {
        // Find device by name and mark as offline
        val device = discoveredDevices.values.find { it.deviceName == deviceName }
        device?.let {
            deviceRepository.updateDeviceStatus(it.ipAddress, false)
            discoveredDevices.remove(it.ipAddress)
        }
    }

    fun stopDiscovery() {
        timeoutJob?.cancel()
        discoveryJob?.cancel()
        nsdDiscoveryManager.stopDiscovery()
        _discoveryState.value = DiscoveryState.Idle
    }

    fun refreshDevices() {
        stopDiscovery()
        startDiscovery()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            _logoutState.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopDiscovery()
    }

    sealed class DiscoveryState {
        object Idle : DiscoveryState()
        object Discovering : DiscoveryState()
        data class DeviceFound(val device: DeviceEntity) : DiscoveryState()
        data class Error(val message: String) : DiscoveryState()
    }
}
