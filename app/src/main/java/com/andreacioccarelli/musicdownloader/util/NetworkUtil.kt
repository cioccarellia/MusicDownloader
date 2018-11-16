package com.andreacioccarelli.musicdownloader.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.andreacioccarelli.musicdownloader.App

/**
 *  Designed and developed by Andrea Cioccarelli
 */

enum class ConnectionStatus { ONLINE, OFFLINE }

object NetworkUtil {
    val n: ConnectionStatus
        get() {
            val connectionsManager = App.instance.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            connectionsManager.allNetworks.map {
                if (connectionsManager.getNetworkInfo(it).isConnected) return ConnectionStatus.ONLINE
            }

            return ConnectionStatus.OFFLINE
        }

    val connectionStatus: ConnectionStatus
        get() {
            val connectionManager = App.instance.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val wifiState: Boolean = isWifiOn()
            val dataState: Boolean = isDataOn(connectionManager)
            val isOnline = wifiState || dataState

            return if (isOnline) ConnectionStatus.ONLINE else ConnectionStatus.OFFLINE
        }

    private fun isDataOn(connectionsManager: ConnectivityManager): Boolean {
        connectionsManager.allNetworks.map {
            if (connectionsManager.getNetworkInfo(it).isConnected) return true
        }

        return connectionsManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected
    }

    private fun isWifiOn(): Boolean {
        val wifi = App.instance.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifi.isWifiEnabled
    }
}