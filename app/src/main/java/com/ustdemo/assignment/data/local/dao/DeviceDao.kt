package com.ustdemo.assignment.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ustdemo.assignment.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Query("SELECT * FROM devices ORDER BY lastDiscoveredAt DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices ORDER BY lastDiscoveredAt DESC")
    suspend fun getAllDevicesSync(): List<DeviceEntity>

    @Query("SELECT * FROM devices WHERE ipAddress = :ipAddress")
    suspend fun getDeviceByIp(ipAddress: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<DeviceEntity>)

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Query("UPDATE devices SET isOnline = :isOnline WHERE ipAddress = :ipAddress")
    suspend fun updateDeviceStatus(ipAddress: String, isOnline: Boolean)

    @Query("UPDATE devices SET isOnline = 0")
    suspend fun markAllDevicesOffline()

    @Query("DELETE FROM devices WHERE ipAddress = :ipAddress")
    suspend fun deleteDevice(ipAddress: String)

    @Query("DELETE FROM devices")
    suspend fun deleteAllDevices()
}
