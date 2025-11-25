package com.damian.xpinbypass

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.damian.xpinbypass.network.PinnedClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bt1 = findViewById<Button>(R.id.btn_test_pin)
        val tv1 = findViewById<TextView>(R.id.tv_result)
        bt1.setOnClickListener {
            // 네트워크는 IO 스레드에서
            lifecycleScope.launch(Dispatchers.IO) {
                runCatching {
                    PinnedClient.testRequest()
                }.onSuccess { body ->
                    Log.i("XPinBypass", "Response OK")
                    val text = "성공:\n${body.take(200)}..."
                    withContext(Dispatchers.Main) {
                        tv1.text = text
                    }
                }.onFailure { e ->
                    Log.e("XPinBypass", "Pinned request failed", e)
                    val text = "실패: ${e.javaClass.simpleName}\n${e.message}"
                    withContext(Dispatchers.Main) {
                        tv1.text = text
                    }
                }
            }
        }
    }
}