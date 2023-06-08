package com.smd.cryptocurrency.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smd.cryptocurrency.R
import com.smd.cryptocurrency.domain.CryptoDao
import com.smd.cryptocurrency.domain.CryptoDataRepresentation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CryptoAdapter(private val cryptoDao: CryptoDao) : RecyclerView.Adapter<CryptoAdapter.CryptoViewHolder>() {
    private var cryptoCurrencies: List<CryptoDataRepresentation> = Collections.emptyList()
    private var preferredCurrency: String = "USD"
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CryptoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.crypto_layout, parent, false)
        return CryptoViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CryptoAdapter.CryptoViewHolder, position: Int) {
        val coin = cryptoCurrencies[position]
        holder.apply {
            coinName.text = coin.name
            coinSymbol.text = coin.symbol
            when(preferredCurrency) {
                "USD" -> currencySign.text = "$"
                "EUR" -> currencySign.text = "€"
                "RON" -> currencySign.text = "RON"
            }

            coinPrice.text = String.format("%.3f", coin.quote?.get(preferredCurrency)?.price)
            oneHourChange.text = String.format("%.3f", coin.quote?.get(preferredCurrency)?.percent_change_1h) + "%"
            twentyFourHourChange.text = String.format("%.3f", coin.quote?.get(preferredCurrency)?.percent_change_24h) + "%"
            sevenDayChange.text = String.format("%.3f", coin.quote?.get(preferredCurrency)?.percent_change_7d) + "%"

            oneHourChange.setTextColor(Color.parseColor(when {
                coin.quote?.get(preferredCurrency)?.percent_change_1h.toString().contains("-") -> "#ff0000"
                else -> "#32CD32"
            }))

            twentyFourHourChange.setTextColor(Color.parseColor(when {
                coin.quote?.get(preferredCurrency)?.percent_change_24h.toString().contains("-") -> "#ff0000"
                else -> "#32CD32"
            }))

            sevenDayChange.setTextColor(Color.parseColor(when {
                coin.quote?.get(preferredCurrency)?.percent_change_7d.toString().contains("-") -> "#ff0000"
                else -> "#32CD32"
            }))

            CoroutineScope(Dispatchers.Main).launch {
                Glide.with(holder.itemView.context)
                    .load(coin.symbol?.let { cryptoDao.getLogoUrlBySymbol(it) })
                    .into(coinIcon)
            }
        }
    }

    override fun getItemCount(): Int {
        return cryptoCurrencies.size
    }

    fun updateData(cryptoCurrencies: List<CryptoDataRepresentation>, preferredCurrency: String) {
        this.cryptoCurrencies = cryptoCurrencies
        this.preferredCurrency = preferredCurrency
        notifyDataSetChanged()
    }

    inner class CryptoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var coinName: TextView
        var coinSymbol: TextView
        var coinPrice: TextView
        val oneHourChange: TextView
        var twentyFourHourChange: TextView
        var sevenDayChange: TextView
        var coinIcon: ImageView
        var currencySign: TextView

        init {
            coinName = view.findViewById(R.id.coinName)
            coinSymbol = view.findViewById(R.id.coinSymbol)
            coinPrice = view.findViewById(R.id.priceUsdText)
            oneHourChange = view.findViewById(R.id.percentChange1hText)
            twentyFourHourChange = view.findViewById(R.id.percentChange24hText)
            sevenDayChange = view.findViewById(R.id.percentChange7dayText)
            coinIcon = view.findViewById(R.id.coinIcon)
            currencySign = view.findViewById(R.id.dollarSign)
        }
    }
}