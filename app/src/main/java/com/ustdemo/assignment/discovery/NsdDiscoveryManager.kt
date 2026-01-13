package com.ustdemo.assignment.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.ustdemo.assignment.data.local.entity.DeviceEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NsdDiscoveryManager @Inject constructor(
    private val nsdManager: NsdManager
) {

    companion object {
        private const val TAG = "NsdDiscoveryManager"

        // Common mDNS service types for network devices
        private val SERVICE_TYPES = listOf(
            "_http._tcp.",           // HTTP services (many smart devices)
            "_https._tcp.",          // HTTPS services
            "_ipp._tcp.",            // Printers (IPP)
            "_ipps._tcp.",           // Secure printers
            "_printer._tcp.",        // Generic printers
            "_pdl-datastream._tcp.", // Printers (PDL)
            "_scanner._tcp.",        // Scanners
            "_googlecast._tcp.",     // Chromecast devices
            "_spotify-connect._tcp.", // Spotify Connect devices
            "_airplay._tcp.",        // AirPlay devices
            "_raop._tcp.",           // Remote Audio Output Protocol (Apple TV)
            "_smb._tcp.",            // SMB shares
            "_afpovertcp._tcp.",     // AFP shares (Mac)
            "_ssh._tcp.",            // SSH services
            "_sftp-ssh._tcp.",       // SFTP services
            "_workstation._tcp.",    // Workstations
            "_device-info._tcp.",    // Device info
            "_homekit._tcp.",        // HomeKit devices
            "_hap._tcp.",            // HomeKit Accessory Protocol
            "_matter._tcp.",         // Matter smart home devices
            "_companion-link._tcp.", // Apple devices
            "_sleep-proxy._udp.",    // Sleep proxy (Apple)
        )
    }

    private var discoveryListeners = mutableListOf<NsdManager.DiscoveryListener>()
    private var resolveListener: NsdManager.ResolveListener? = null

    fun discoverDevices(): Flow<DiscoveryEvent> = callbackFlow {
        val discoveredServices = mutableMapOf<String, NsdServiceInfo>()

        SERVICE_TYPES.forEach { serviceType ->
            val discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) {
                    Log.d(TAG, "Discovery started for: $regType")
                }

                override fun onServiceFound(service: NsdServiceInfo) {
                    Log.d(TAG, "Service found: ${service.serviceName}")
                    val key = "${service.serviceName}_${service.serviceType}"
                    if (!discoveredServices.containsKey(key)) {
                        discoveredServices[key] = service
                        resolveService(service) { resolvedService ->
                            val device = DeviceEntity(
                                ipAddress = resolvedService.host?.hostAddress ?: "",
                                deviceName = resolvedService.serviceName,
                                serviceType = resolvedService.serviceType,
                                port = resolvedService.port,
                                isOnline = true,
                                lastDiscoveredAt = System.currentTimeMillis()
                            )
                            if (device.ipAddress.isNotEmpty()) {
                                trySend(DiscoveryEvent.DeviceFound(device))
                            }
                        }
                    }
                }

                override fun onServiceLost(service: NsdServiceInfo) {
                    Log.d(TAG, "Service lost: ${service.serviceName}")
                    val key = "${service.serviceName}_${service.serviceType}"
                    discoveredServices.remove(key)
                    trySend(DiscoveryEvent.DeviceLost(service.serviceName))
                }

                override fun onDiscoveryStopped(serviceType: String) {
                    Log.d(TAG, "Discovery stopped for: $serviceType")
                }

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.e(TAG, "Discovery failed for $serviceType: $errorCode")
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.e(TAG, "Stop discovery failed for $serviceType: $errorCode")
                }
            }

            try {
                nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
                discoveryListeners.add(discoveryListener)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start discovery for $serviceType: ${e.message}")
            }
        }

        trySend(DiscoveryEvent.DiscoveryStarted)

        awaitClose {
            stopDiscovery()
        }
    }

    private fun resolveService(serviceInfo: NsdServiceInfo, onResolved: (NsdServiceInfo) -> Unit) {
        resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Resolve failed for ${serviceInfo.serviceName}: $errorCode")
            }

            override fun onServiceResolved(resolvedService: NsdServiceInfo) {
                Log.d(TAG, "Service resolved: ${resolvedService.serviceName} at ${resolvedService.host?.hostAddress}")
                onResolved(resolvedService)
            }
        }

        try {
            nsdManager.resolveService(serviceInfo, resolveListener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve service: ${e.message}")
        }
    }

    fun stopDiscovery() {
        discoveryListeners.forEach { listener ->
            try {
                nsdManager.stopServiceDiscovery(listener)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop discovery: ${e.message}")
            }
        }
        discoveryListeners.clear()
    }

    sealed class DiscoveryEvent {
        object DiscoveryStarted : DiscoveryEvent()
        data class DeviceFound(val device: DeviceEntity) : DiscoveryEvent()
        data class DeviceLost(val deviceName: String) : DiscoveryEvent()
        data class Error(val message: String) : DiscoveryEvent()
    }
}
