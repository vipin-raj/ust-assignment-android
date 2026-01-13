package com.ustdemo.assignment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey
    val ipAddress: String,
    val deviceName: String,
    val serviceType: String,
    val port: Int,
    val isOnline: Boolean = false,
    val lastDiscoveredAt: Long = System.currentTimeMillis()
)
