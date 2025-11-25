package com.damian.xpinbypass

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookMain : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 어떤 앱이 로딩되는지 로그
        XposedBridge.log("XPinBypass: loaded package = ${lpparam.packageName}")

        // 🔒 자기 앱에서만 동작
        if (lpparam.packageName != "com.damian.xpinbypass") return

        try {
            hookMainActivity(lpparam)
            hookCertificatePinner(lpparam)
        } catch (t: Throwable) {
            XposedBridge.log("XPinBypass: error in HookMain: ${t.message}")
        }
    }

    private fun hookMainActivity(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz = lpparam.classLoader.loadClass(
            "com.damian.xpinbypass.MainActivity"
        )

        XposedBridge.hookAllMethods(clazz, "onCreate", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                XposedBridge.log("XPinBypass: MainActivity.onCreate called!")
            }
        })
    }

    // 🔥 OkHttp CertificatePinner.check 우회
    private fun hookCertificatePinner(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val pinnerClass = lpparam.classLoader.loadClass("okhttp3.CertificatePinner")

            val hook = object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val host = param.args.getOrNull(0)
                    XposedBridge.log("XPinBypass: bypassing pinning for host=$host")
                    // void 메서드라 null 설정하면 "아무 일도 안 하고 성공"으로 처리됨
                    param.result = null
                }
            }

            // 1) 일단 공개 메서드 이름도 후킹
            XposedBridge.hookAllMethods(pinnerClass, "check", hook)

            // 2) OkHttp 4.x 실제 구현 이름인 check$okhttp 도 같이 후킹
            XposedBridge.hookAllMethods(pinnerClass, "check\$okhttp", hook)

            XposedBridge.log("XPinBypass: CertificatePinner.check & check\$okhttp hooked")
        } catch (t: Throwable) {
            XposedBridge.log("XPinBypass: failed to hook CertificatePinner: ${t.message}")
        }
    }

}