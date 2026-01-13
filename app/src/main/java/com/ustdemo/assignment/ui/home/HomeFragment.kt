package com.ustdemo.assignment.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ustdemo.assignment.R
import com.ustdemo.assignment.data.local.entity.DeviceEntity
import com.ustdemo.assignment.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()

        // Start discovery when fragment is created
        viewModel.startDiscovery()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.home_title)
        binding.toolbar.inflateMenu(R.menu.menu_home)

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_refresh -> {
                    viewModel.refreshDevices()
                    true
                }
                R.id.action_logout -> {
                    viewModel.logout()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter { device ->
            navigateToDetail(device)
        }

        binding.recyclerView.apply {
            adapter = deviceAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshDevices()
        }

        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.accent
        )
    }

    private fun observeViewModel() {
        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            deviceAdapter.submitList(devices)
            updateEmptyState(devices.isEmpty())
        }

        viewModel.discoveryState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeViewModel.DiscoveryState.Discovering -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                    binding.tvDiscoveryStatus.visibility = View.VISIBLE
                    binding.tvDiscoveryStatus.text = getString(R.string.discovering_devices)
                }
                is HomeViewModel.DiscoveryState.DeviceFound -> {
                    // Device found, list will be updated automatically via LiveData
                }
                is HomeViewModel.DiscoveryState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.tvDiscoveryStatus.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is HomeViewModel.DiscoveryState.Idle -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.tvDiscoveryStatus.visibility = View.GONE
                }
            }
        }

        viewModel.logoutState.observe(viewLifecycleOwner) { loggedOut ->
            if (loggedOut) {
                navigateToLogin()
            }
        }

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvGreeting.text = "Hi, $name!"
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun navigateToDetail(device: DeviceEntity) {
        val bundle = bundleOf(
            "deviceIp" to device.ipAddress,
            "deviceName" to device.deviceName,
            "serviceType" to device.serviceType,
            "isOnline" to device.isOnline
        )
        findNavController().navigate(R.id.action_homeFragment_to_detailFragment, bundle)
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopDiscovery()
        _binding = null
    }
}
