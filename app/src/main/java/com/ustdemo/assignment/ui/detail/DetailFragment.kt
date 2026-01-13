package com.ustdemo.assignment.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ustdemo.assignment.R
import com.ustdemo.assignment.data.remote.model.IpInfoResponse
import com.ustdemo.assignment.databinding.FragmentDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailViewModel by viewModels()

    private var deviceIp: String? = null
    private var deviceName: String? = null
    private var serviceType: String? = null
    private var isOnline: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deviceIp = it.getString("deviceIp")
            deviceName = it.getString("deviceName")
            serviceType = it.getString("serviceType")
            isOnline = it.getBoolean("isOnline", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        displayDeviceInfo()
        setupClickListeners()
        observeViewModel()

        // Load public IP info
        viewModel.loadPublicIpInfo()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.detail_title)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun displayDeviceInfo() {
        binding.apply {
            tvDeviceNameValue.text = deviceName ?: "-"
            tvDeviceIpValue.text = deviceIp ?: "-"
            tvServiceTypeValue.text = serviceType?.replace("._tcp.", "")?.replace("_", "") ?: "-"

            val statusColor = if (isOnline) {
                ContextCompat.getColor(requireContext(), R.color.status_online)
            } else {
                ContextCompat.getColor(requireContext(), R.color.status_offline)
            }

            val statusText = if (isOnline) {
                getString(R.string.device_online)
            } else {
                getString(R.string.device_offline)
            }

            tvDeviceStatusValue.text = statusText
            tvDeviceStatusValue.setTextColor(statusColor)
        }
    }

    private fun setupClickListeners() {
        binding.btnRetry.setOnClickListener {
            viewModel.retry()
        }
    }

    private fun observeViewModel() {
        viewModel.publicIpState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DetailViewModel.PublicIpState.Loading -> {
                    showPublicIpLoading()
                }
                is DetailViewModel.PublicIpState.Success -> {
                    binding.tvPublicIpValue.text = state.ip
                }
                is DetailViewModel.PublicIpState.Error -> {
                    showError(state.message)
                }
            }
        }

        viewModel.ipInfoState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DetailViewModel.IpInfoState.Loading -> {
                    showIpInfoLoading()
                }
                is DetailViewModel.IpInfoState.Success -> {
                    displayIpInfo(state.ipInfo)
                    showContent()
                }
                is DetailViewModel.IpInfoState.Error -> {
                    showError(state.message)
                }
            }
        }
    }

    private fun showPublicIpLoading() {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            errorLayout.visibility = View.GONE
            publicIpInfoCard.visibility = View.VISIBLE
            tvPublicIpValue.text = getString(R.string.loading)
        }
    }

    private fun showIpInfoLoading() {
        binding.apply {
            tvCityValue.text = getString(R.string.loading)
            tvRegionValue.text = getString(R.string.loading)
            tvCountryValue.text = getString(R.string.loading)
            tvLocationValue.text = getString(R.string.loading)
            tvOrganizationValue.text = getString(R.string.loading)
            tvPostalValue.text = getString(R.string.loading)
            tvTimezoneValue.text = getString(R.string.loading)
        }
    }

    private fun displayIpInfo(ipInfo: IpInfoResponse) {
        binding.apply {
            tvCityValue.text = ipInfo.city ?: "-"
            tvRegionValue.text = ipInfo.region ?: "-"
            tvCountryValue.text = ipInfo.country ?: "-"
            tvLocationValue.text = ipInfo.location ?: "-"
            tvOrganizationValue.text = ipInfo.organization ?: "-"
            tvPostalValue.text = ipInfo.postal ?: "-"
            tvTimezoneValue.text = ipInfo.timezone ?: "-"
        }
    }

    private fun showContent() {
        binding.apply {
            progressBar.visibility = View.GONE
            errorLayout.visibility = View.GONE
            scrollView.visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        binding.apply {
            progressBar.visibility = View.GONE
            scrollView.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
            tvErrorMessage.text = message
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
