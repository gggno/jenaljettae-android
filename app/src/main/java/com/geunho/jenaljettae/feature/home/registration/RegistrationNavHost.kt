package com.geunho.jenaljettae.features.home.registration

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.geunho.jenaljettae.data.repository.GeminiCouponAnalysisRepository
import com.geunho.jenaljettae.domain.model.CouponDraft
import com.geunho.jenaljettae.domain.usecase.AnalyzeCouponImageUseCase
import com.geunho.jenaljettae.features.home.registration.coupon.CouponImageRecognitionScreen
import com.geunho.jenaljettae.features.home.registration.coupon.CouponManualEntryScreen
import com.geunho.jenaljettae.features.home.registration.coupon.CouponRegistrationMethodScreen
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import com.geunho.jenaljettae.ui.theme.AppTheme


// MARK: - Registration Routes | 등록 화면의 이동 경로 정의
private object RegistrationRoute {
    const val GRAPH = "registration"
    const val TYPE = "registration/type"
    const val METHOD = "registration/method"
    const val RECOGNITION = "registration/recognition"
    const val MANUAL = "registration/manual"
}

// MARK: - Registration Flow ViewModel | 등록 중 이미지와 AI 분석 결과 공유
class RegistrationFlowViewModel : ViewModel() {
    var imageData by mutableStateOf<ByteArray?>(null)
        private set

    var imageUri by mutableStateOf<Uri?>(null)
        private set

    var couponDraft by mutableStateOf<CouponDraft?>(null)
        private set

    fun selectImage(data: ByteArray, uri: Uri) {
        imageData = data
        imageUri = uri
        couponDraft = null
    }

    fun completeAnalysis(draft: CouponDraft) {
        couponDraft = draft
    }

    fun startManualEntry() {
        imageData = null
        imageUri = null
        couponDraft = null
    }
}

// MARK: - Registration Flow ViewModel Scope | 등록 그래프 전체에서 동일한 ViewModel 사용
@Composable
private fun registrationFlowViewModel(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry
): RegistrationFlowViewModel {
    val graphEntry = remember(backStackEntry) {
        navController.getBackStackEntry(RegistrationRoute.GRAPH)
    }

    return viewModel(viewModelStoreOwner = graphEntry)
}

// MARK: - Registration Nav Host | iOS NavigationStack처럼 등록 화면 백스택 관리
@Composable
fun RegistrationNavHost(onClose: () -> Unit) {
    val navController = rememberNavController()

    // MARK: - AI Dependencies | 실제 Gemini 분석 Repository와 UseCase 연결
    val analyzeCouponImageUseCase = remember {
        AnalyzeCouponImageUseCase(
            repository = GeminiCouponAnalysisRepository(
                workerBaseUrl = "https://jenaljettae-api.geunhojeong-jgh.workers.dev"
            )
        )
    }

    NavHost(
        navController = navController,
        startDestination = RegistrationRoute.GRAPH,
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // MARK: - Registration Graph | 등록 흐름의 공통 백스택 범위
        navigation(
            startDestination = RegistrationRoute.TYPE,
            route = RegistrationRoute.GRAPH
        ) {
            // MARK: - Type Selection | 등록 유형 선택
            composable(RegistrationRoute.TYPE) {
                RegistrationTypeSelectionScreen(
                    onClose = onClose,
                    onCouponSelected = {
                        navController.navigate(RegistrationRoute.METHOD)
                    }
                )
            }

            // MARK: - Registration Method | 직접 입력 또는 AI 이미지 선택
            composable(RegistrationRoute.METHOD) { entry ->
                val flowViewModel = registrationFlowViewModel(navController, entry)

                CouponRegistrationMethodScreen(
                    onBack = { navController.popBackStack() },
                    onManualEntry = {
                        flowViewModel.startManualEntry()
                        navController.navigate(RegistrationRoute.MANUAL)
                    },
                    onImageSelected = { imageData, imageUri ->
                        flowViewModel.selectImage(imageData, imageUri)
                        navController.navigate(RegistrationRoute.RECOGNITION)
                    }
                )
            }

            // MARK: - Image Recognition | Gemini 분석 후 다음 화면 Push
            composable(RegistrationRoute.RECOGNITION) { entry ->
                val flowViewModel = registrationFlowViewModel(navController, entry)
                val imageData = flowViewModel.imageData

                if (imageData != null) {
                    CouponImageRecognitionScreen(
                        imageData = imageData,
                        analyzeCouponImageUseCase = analyzeCouponImageUseCase,
                        onBack = { navController.popBackStack() },
                        onAnalysisComplete = { draft, _ ->
                            flowViewModel.completeAnalysis(draft)
                            navController.navigate(RegistrationRoute.MANUAL)
                        }
                    )
                } else {
                    // MARK: - Missing Image | 이미지 상태가 없으면 이전 화면으로 복귀
                    LaunchedEffect(Unit) { navController.popBackStack() }
                }
            }

            // MARK: - Coupon Manual Entry | AI 분석 정보와 원본 이미지 전달
            composable(RegistrationRoute.MANUAL) { entry ->
                val flowViewModel = registrationFlowViewModel(navController, entry)

                CouponManualEntryScreen(
                    onBack = { navController.popBackStack() },
                    initialDraft = flowViewModel.couponDraft,
                    initialImageUri = if (flowViewModel.couponDraft != null) {
                        flowViewModel.imageUri
                    } else {
                        null
                    }
                )
            }
        }
    }
}
