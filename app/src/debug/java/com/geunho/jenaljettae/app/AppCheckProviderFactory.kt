package com.geunho.jenaljettae.app

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

// MARK: - Debug App Check | 개발 환경에서 Debug Provider 사용
internal fun createAppCheckProviderFactory(): AppCheckProviderFactory =
    DebugAppCheckProviderFactory.getInstance()
