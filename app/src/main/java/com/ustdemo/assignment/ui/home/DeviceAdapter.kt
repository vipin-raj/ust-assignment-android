package com.ustdemo.assignment.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ustdemo.assignment.R
import com.ustdemo.assignment.data.local.entity.DeviceEntity
import com.ustdemo.assignment.databinding.ItemDeviceBinding

class DeviceAdapter(
    private val onItemClick: (DeviceEntity) -> Unit
) : ListAdapter<DeviceEntity, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeviceViewHolder(
        private val binding: ItemDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(device: DeviceEntity) {
            binding.apply {
                tvDeviceName.text = device.deviceName
                tvDeviceIp.text = device.ipAddress
                tvServiceType.text = device.serviceType.replace("._tcp.", "").replace("_", "")

                val statusColor = if (device.isOnline) {
                    ContextCompat.getColor(root.context, R.color.status_online)
                } else {
                    ContextCompat.getColor(root.context, R.color.status_offline)
                }

                val statusText = if (device.isOnline) {
                    root.context.getString(R.string.device_online)
                } else {
                    root.context.getString(R.string.device_offline)
                }

                tvStatus.text = statusText
                tvStatus.setTextColor(statusColor)
                statusIndicator.setColorFilter(statusColor)
            }
        }
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<DeviceEntity>() {
        override fun areItemsTheSame(oldItem: DeviceEntity, newItem: DeviceEntity): Boolean {
            return oldItem.ipAddress == newItem.ipAddress
        }

        override fun areContentsTheSame(oldItem: DeviceEntity, newItem: DeviceEntity): Boolean {
            return oldItem == newItem
        }
    }
}
