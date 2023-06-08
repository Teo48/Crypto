package com.smd.cryptocurrency.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.smd.cryptocurrency.databinding.FragmentConvertCurrencyBinding
import com.smd.cryptocurrency.domain.CryptoDatabase
import kotlinx.coroutines.*

class ConvertCurrencyFragment : Fragment() {
    private lateinit var binding: FragmentConvertCurrencyBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConvertCurrencyBinding.inflate(inflater, container, false)
        val cryptoDatabase = CryptoDatabase.getDatabase(this.requireContext())
        val cryptoDao = cryptoDatabase.cryptoDao()
        val spinnerFromCurrency = binding.spinnerFromCurrency
        val spinnerToCurrency = binding.spinnerToCurrency
        val convertButton = binding.buttonConvert
        val conversionResultValue = binding.conversionResultValue
        lateinit var availableCurrencies: List<String>
        binding.toCurrencyImage.visibility = View.GONE
        conversionResultValue.visibility = View.GONE
        val job = CoroutineScope(Dispatchers.IO).launch {
            availableCurrencies = cryptoDao.getAllCryptoSymbols()
        }
        runBlocking {
            job.join()
        }

        val fromCurrencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, availableCurrencies)
        val toCurrencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, availableCurrencies)
        fromCurrencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        toCurrencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFromCurrency.adapter = fromCurrencyAdapter
        spinnerToCurrency.adapter = toCurrencyAdapter
        spinnerToCurrency.onItemSelectedListener = spinnerOnItemSelectedListener()

        convertButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val insertedAmount = binding.editAmount.text.toString()
                val priceFromCurrency =
                    cryptoDao.getPriceBySymbol(spinnerFromCurrency.selectedItem as String)
                val priceToCurrency =
                    cryptoDao.getPriceBySymbol(spinnerToCurrency.selectedItem as String)
                withContext(Dispatchers.Main) {
                    conversionResultValue.text = "Conversion Result: " + String.format(
                        "%.4f",
                        (insertedAmount.toDouble() * priceFromCurrency / priceToCurrency)
                    )
                    context?.let { it1 ->
                        Glide.with(it1)
                            .load(cryptoDao.getLogoUrlBySymbol(spinnerToCurrency.selectedItem as String))
                            .into(binding.toCurrencyImage)
                    }
                    binding.toCurrencyImage.visibility = View.VISIBLE
                    conversionResultValue.visibility = View.VISIBLE
                }
            }
        }
        return binding.root
    }

    private fun spinnerOnItemSelectedListener(): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.toCurrencyImage.visibility = View.GONE
                binding.conversionResultValue.visibility = View.GONE
            }
        }
    }
}