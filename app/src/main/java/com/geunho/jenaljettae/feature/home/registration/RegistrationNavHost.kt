package com.geunho.jenaljettae.features.home.registration

import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.geunho.jenaljettae.features.home.registration.pass.PassManualEntryScreen
import com.geunho.jenaljettae.features.home.registration.subscription.SubscriptionManualEntryScreen
import com.geunho.jenaljettae.features.home.registration.schedule.ScheduleManualEntryScreen
import com.geunho.jenaljettae.ui.theme.AppTheme

// MARK: - Registration Routes | 등록 화면 경로
private object RegistrationRoute {
    const val GRAPH = "registration"
    const val TYPE = "registration/type"
    const val METHOD = "registration/method"
    const val RECOGNITION = "registration/recognition"
    const val MANUAL = "registration/manual"
    const val PASS_MANUAL = "registration/pass/manual"
    const val SUBSCRIPTION_MANUAL = "registration/subscription/manual"
    const val SCHEDULE_MANUAL = "registration/schedule/manual"
}

// MARK: - Registration Flow ViewModel | 기존 AI 쿠폰 분석 상태
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

    fun completeAnalysis(draft: CouponDraft) { couponDraft = draft }

    fun startManualEntry() {
        imageData = null
        imageUri = null
        couponDraft = null
    }
}

// MARK: - Registration Flow ViewModel Scope | 등록 그래프 내 ViewModel 공유
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

// MARK: - Registration Nav Host | 등록 화면 내비게이션
@Composable
fun RegistrationNavHost(onClose: () -> Unit) {
    val navController = rememberNavController()

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
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
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
                    },
                    onPassSelected = {
                        navController.navigate(RegistrationRoute.PASS_MANUAL)
                    },
                    onSubscriptionSelected = {
                        navController.navigate(RegistrationRoute.SUBSCRIPTION_MANUAL)
                    },
                    onScheduleSelected = {
                        navController.navigate(RegistrationRoute.SCHEDULE_MANUAL)
                    }
                )
            }

            // MARK: - Coupon Registration Method | 등록 방식 선택
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

            // MARK: - Coupon Recognition | AI 분석
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
                    LaunchedEffect(Unit) { navController.popBackStack() }
                }
            }

            // MARK: - Coupon Manual | 쿠폰 직접 입력
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

            // MARK: - Pass | 이용권 등록
            composable(RegistrationRoute.PASS_MANUAL) {
                PassManualEntryScreen(onBack = { navController.popBackStack() })
            }

            // MARK: - Subscription | 정기 구독 등록
            composable(RegistrationRoute.SUBSCRIPTION_MANUAL) {
                SubscriptionManualEntryScreen(onBack = { navController.popBackStack() })
            }

            // MARK: - Schedule | 일정 등록
            composable(RegistrationRoute.SCHEDULE_MANUAL) {
                ScheduleManualEntryScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
