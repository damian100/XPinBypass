package com.damian.xpinbypass.network

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.Request

object PinnedClient {

    // 연습용 도메인
    private const val HOST = "example.com"

    // 여기 pin 값은 openssl로 진짜 뽑아서 넣으면 된다. (현재는 임시로 틀린 값)
    private val certificatePinner: CertificatePinner = CertificatePinner.Builder()
        .add(HOST, "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .build()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .build()

    fun testRequest(): String {
        val request = Request.Builder()
            .url("https://$HOST/")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}")
            }
            return response.body?.string() ?: ""
        }
    }
}



