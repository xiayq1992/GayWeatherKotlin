package com.xyq.gayweatherkotlin.util

import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * okhttp 请求工具类
 * created by xyq
 * created on 2018/7/4
 */
class HttpUtil {
    companion object {
        fun sendOkHttpRequest(address: String, callback: okhttp3.Callback) {
            var okHttpClient = OkHttpClient()
            var request = Request.Builder().url(address).build()
            okHttpClient.newCall(request).enqueue(callback)
        }
    }
}