package com.geunho.jenaljettae.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck

// MARK: - Jenaljettae Application | Firebase 및 App Check 초기화
class JenaljettaeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // MARK: - Firebase Initialization | Firebase 프로젝트 초기화
        checkNotNull(FirebaseApp.initializeApp(this)) {
            "Firebase 초기화에 실패했어요. google-services.json을 확인해주세요."
        }

        // MARK: - App Check Provider | Debug 또는 Release 인증 Provider 등록
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            createAppCheckProviderFactory()
        )

        Log.d("JNT_FIREBASE", "Application 초기화 및 App Check Provider 등록 완료")
    }
}
