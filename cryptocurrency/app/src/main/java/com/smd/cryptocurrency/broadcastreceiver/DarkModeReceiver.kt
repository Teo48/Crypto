package com.smd.cryptocurrency.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class DarkModeReceiver : BroadcastReceiver() {
    companion object {
        const val DARK_MODE_ACTION = "com.example.app.DARK_MODE_ACTION"
        const val DARK_MODE_EXTRA = "dark_mode_extra"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DARK_MODE_ACTION) {
            val darkModeEnabled = intent.getBooleanExtra(DARK_MODE_EXTRA, false)
            setDarkMode(darkModeEnabled)
        }
    }

    private fun setDarkMode(darkModeEnabled: Boolean) {
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}