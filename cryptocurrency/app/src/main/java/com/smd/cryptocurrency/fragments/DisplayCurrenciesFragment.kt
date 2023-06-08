package com.smd.cryptocurrency.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.smd.cryptocurrency.R
import com.smd.cryptocurrency.adapter.CryptoAdapter
import com.smd.cryptocurrency.broadcastreceiver.DarkModeReceiver
import com.smd.cryptocurrency.databinding.FragmentDisplayCurrenciesBinding
import com.smd.cryptocurrency.domain.*
import com.smd.cryptocurrency.service.CryptoAPIService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.properties.Delegates

private const val BASE_LOGO_URL = "https://s2.coinmarketcap.com/static/img/coins/64x64/%s.png"
private const val BASE_URL = "https://pro-api.coinmarketcap.com/"
private const val API_KEY = "a92dbe33-bc38-4e8d-899f-96c434838dd8"
private const val POST_NOTIFICATIONS_CODE = 1337

class DisplayCurrenciesFragment : Fragment() {
    private lateinit var binding: FragmentDisplayCurrenciesBinding
    private lateinit var cryptoAdapter: CryptoAdapter
    private lateinit var preferredCurrency: String
    private lateinit var preferredCryptoCurrency: String
    private var priceAlert by Delegates.notNull<Double>()
    private lateinit var notificationManager: NotificationManager
    private var limit by Delegates.notNull<Int>()

    companion object {
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "PriceAlertChannel"
        private const val CONTEXT_TEXT = "Price alert for %s"
        private const val NOTIFICATION_CHANNEL_NAME = "Price Alert"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDisplayCurrenciesBinding.inflate(inflater, container, false)
        binding.cryptoRecyclerView.layoutManager = LinearLayoutManager(this.context)

        val cryptoDatabase = CryptoDatabase.getDatabase(this.requireContext())
        val cryptoDao = cryptoDatabase.cryptoDao()
        cryptoAdapter = CryptoAdapter(cryptoDao)
        binding.cryptoRecyclerView.adapter = cryptoAdapter
        val prefs = activity?.getPreferences(Context.MODE_PRIVATE)
        preferredCurrency = prefs?.getString("preferred_currency", "USD").toString()
        preferredCryptoCurrency = prefs?.getString("preferred_crypto_currency", "BTC").toString()
        priceAlert = prefs?.getString("price_alert", "10000.0")!!.toDouble()
        limit = prefs.getString("show_limit", "10")!!.toInt()

        notificationManager = activity?.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        getCryptoPrices(cryptoDao, preferredCurrency)

        CoroutineScope(Dispatchers.IO).launch {
            if (cryptoDao.getPriceBySymbol(preferredCryptoCurrency) >= priceAlert) {
                withContext(Dispatchers.Main) {
                    displayNotification()
                }
            }
        }

        return binding.root
    }

    private fun getCryptoPrices(cryptoDao: CryptoDao, preferredCurrency: String) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val cryptoAPIService = retrofit.create(CryptoAPIService::class.java)

        cryptoAPIService.getLatestListings(Keys.apiKey(), limit, preferredCurrency)
            .enqueue(object : Callback<CryptoApiResponse> {
                override fun onResponse(
                    call: Call<CryptoApiResponse>,
                    response: Response<CryptoApiResponse>
                ) {
                    val cryptoCurrencies = response.body()?.data ?: emptyList()
                    CoroutineScope(Dispatchers.IO).launch {
                        processCryptoData(cryptoCurrencies, cryptoDao)
                    }
                    response.body()?.data?.let { cryptoAdapter.updateData(it, preferredCurrency) }
                }

                override fun onFailure(call: Call<CryptoApiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@DisplayCurrenciesFragment.activity,
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private suspend fun processCryptoData(cryptoCurrencies: List<CryptoDataRepresentation>,
                                          cryptoDao: CryptoDao,
                                          ) {
        withContext(Dispatchers.IO) {
            for (coin in cryptoCurrencies) {
                val id = coin.id
                val symbol = coin.symbol
                val logoUrl = BASE_LOGO_URL.format(id)
                val price = coin.quote?.get(preferredCurrency)?.price
                cryptoDao.insertCrypto(Crypto(id, symbol, logoUrl, price))
            }
        }
    }

    private fun displayNotification() {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_NONE
        )
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setContentTitle("PRICE ALERT")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(CONTEXT_TEXT.format("BTC"))
            .build()

        notificationManager.createNotificationChannel(notificationChannel)
        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    POST_NOTIFICATIONS_CODE)
            }
            notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            POST_NOTIFICATIONS_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayNotification()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Notification permission denied.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}