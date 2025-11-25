package com.damian.xpinbypass.network

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.Request

object PinnedClient {

    // 연습용 도메인 – 우선 example.com 으로 가자
    private const val HOST = "example.com"

    // ⚠ 여기 pin 값은 나중에 openssl로 진짜 뽑아서 넣으면 돼.
    // 지금은 일부러 틀린 값 넣어두면, 항상 SSL 에러가 나서
    // "우회 전/후" 비교하기 좋음.
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



