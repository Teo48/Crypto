package com.smd.cryptocurrency

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.smd.cryptocurrency.broadcastreceiver.DarkModeReceiver
import com.smd.cryptocurrency.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var darkModeReceiver: DarkModeReceiver
    private lateinit var intentFilter: IntentFilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.mainFragmentId)
        bottomNavigationView.setupWithNavController(navController)
        darkModeReceiver = DarkModeReceiver()
        intentFilter = IntentFilter(DarkModeReceiver.DARK_MODE_ACTION)
        registerReceiver(darkModeReceiver, intentFilter)
        val intent = Intent(DarkModeReceiver.DARK_MODE_ACTION).apply {
            putExtra(DarkModeReceiver.DARK_MODE_EXTRA, getDarkModePreference())
        }
        sendBroadcast(intent)
    }

    private fun getDarkModePreference(): Boolean? {
        val prefs = getPreferences(Context.MODE_PRIVATE)
        return prefs?.getBoolean("dark_mode_enabled", false)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(darkModeReceiver)
    }
}