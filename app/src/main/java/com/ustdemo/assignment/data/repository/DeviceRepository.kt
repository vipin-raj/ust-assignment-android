package com.ustdemo.assignment.data.repository

import com.ustdemo.assignment.data.local.dao.DeviceDao
import com.ustdemo.assignment.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val deviceDao: DeviceDao
) {

    fun getAllDevices(): Flow<List<DeviceEntity>> {
        return deviceDao.getAllDevices()
    }

    suspend fun getAllDevicesSync(): List<DeviceEntity> {
        return deviceDao.getAllDevicesSync()
    }

    suspend fun getDeviceByIp(ipAddress: String): DeviceEntity? {
        return deviceDao.getDeviceByIp(ipAddress)
    }

    suspend fun insertDevice(device: DeviceEntity) {
        deviceDao.insertDevice(device)
    }

    suspend fun insertDevices(devices: List<DeviceEntity>) {
        deviceDao.insertDevices(devices)
    }

    suspend fun updateDevice(device: DeviceEntity) {
        deviceDao.updateDevice(device)
    }

    suspend fun updateDeviceStatus(ipAddress: String, isOnline: Boolean) {
        deviceDao.updateDeviceStatus(ipAddress, isOnline)
    }

    suspend fun markAllDevicesOffline() {
        deviceDao.markAllDevicesOffline()
    }

    suspend fun deleteDevice(ipAddress: String) {
        deviceDao.deleteDevice(ipAddress)
    }

    suspend fun deleteAllDevices() {
        deviceDao.deleteAllDevices()
    }
}
