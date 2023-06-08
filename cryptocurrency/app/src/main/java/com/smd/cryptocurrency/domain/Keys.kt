package com.smd.cryptocurrency.domain

object Keys {

    init {
        System.loadLibrary("native-lib")
    }

    external fun apiKey(): String
}