package com.example.ecochallengeapp

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class EcoChallengeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "a6f3aa3f548b763604212e9573c44cf7")
    }
}
