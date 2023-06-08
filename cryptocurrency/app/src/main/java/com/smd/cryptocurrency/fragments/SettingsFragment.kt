package com.smd.cryptocurrency.fragments

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.smd.cryptocurrency.R
import com.smd.cryptocurrency.broadcastreceiver.DarkModeReceiver
import com.smd.cryptocurrency.databinding.FragmentSettingsBinding
import com.smd.cryptocurrency.domain.CryptoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsFragment: Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var darkModeReceiver: DarkModeReceiver
    private lateinit var intentFilter: IntentFilter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val cryptoDatabase = CryptoDatabase.getDatabase(this.requireContext())
        val cryptoDao = cryptoDatabase.cryptoDao()
        lateinit var availableCurrencies: List<String>
        darkModeReceiver = DarkModeReceiver()
        intentFilter = IntentFilter(DarkModeReceiver.DARK_MODE_ACTION)
        val prefs = activity?.getPreferences(Context.MODE_PRIVATE)
        binding.switchDarkMode.isChecked = getDarkModePreference() == true

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val intent = Intent(DarkModeReceiver.DARK_MODE_ACTION).apply {
                putExtra(DarkModeReceiver.DARK_MODE_EXTRA, isChecked)
            }
            setDarkModePreference(isChecked)
            requireContext().sendBroadcast(intent)
        }

        setupSpinnerCurrency(prefs)
        setupSpinnerShowLimit(prefs)

        val job = CoroutineScope(Dispatchers.IO).launch {
            availableCurrencies = cryptoDao.getAllCryptoSymbols()
        }
        runBlocking {
            job.join()
        }

        val spinnerCryptoCurrencyAdapter= ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, availableCurrencies)
        spinnerCryptoCurrencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCryptoCurrency.adapter = spinnerCryptoCurrencyAdapter
        binding.spinnerCryptoCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedOption = parent.getItemAtPosition(position) as String
                prefs?.edit()?.putString("preferred_crypto_currency", selectedOption)?.apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.priceAlertButton.setOnClickListener {
            prefs?.edit()?.putString("price_alert", binding.editCryptoPriceAlert.text.toString())?.apply()
        }
        return binding.root
    }

    private fun setupSpinnerCurrency(prefs: SharedPreferences?) {
        this.context?.let { context ->
            ArrayAdapter.createFromResource(
                context,
                R.array.currency_options,
                android.R.layout.simple_spinner_item
            ).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCurrency.adapter = it
            }
        }

        val selectedOptionIndex = when (prefs?.getString("preferred_currency", "USD")) {
            "USD" -> 0
            "EUR" -> 1
            "RON" -> 2
            else -> 3
        }
        binding.spinnerCurrency.setSelection(selectedOptionIndex)

        binding.spinnerCurrency.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedOption = parent.getItemAtPosition(position) as String
                    prefs?.edit()?.putString("preferred_currency", selectedOption)?.apply()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun setupSpinnerShowLimit(prefs: SharedPreferences?) {
        this.context?.let { context ->
            ArrayAdapter.createFromResource(
                context,
                R.array.crypto_limit,
                android.R.layout.simple_spinner_item
            ).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCryptoShowLimit.adapter = it
            }
        }

        val selectedOptionIndex = when (prefs?.getString("show_limit", "10")) {
            "10" -> 0
            "20" -> 1
            "25" -> 2
            "50" -> 3
            "75" -> 4
            "100" -> 5
            else -> 6
        }
        binding.spinnerCryptoShowLimit.setSelection(selectedOptionIndex)

        binding.spinnerCryptoShowLimit.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedOption = parent.getItemAtPosition(position) as String
                    prefs?.edit()?.putString("show_limit", selectedOption)?.apply()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun getDarkModePreference(): Boolean? {
        val prefs = activity?.getPreferences(Context.MODE_PRIVATE)
        return prefs?.getBoolean("dark_mode_enabled", false)
    }

    private fun setDarkModePreference(enabled: Boolean) {
        val prefs = activity?.getPreferences(Context.MODE_PRIVATE)
        prefs?.edit()?.putBoolean("dark_mode_enabled", enabled)?.apply()
    }

    override fun onStart() {
        super.onStart()
        requireContext().registerReceiver(darkModeReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(darkModeReceiver)
    }
}