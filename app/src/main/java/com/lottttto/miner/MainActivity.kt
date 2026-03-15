package com.lottttto.miner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.lottttto.miner.navigation.AppNavGraph
import com.lottttto.miner.navigation.OnboardingNavGraph
import com.lottttto.miner.ui.theme.LotttttoTheme
import com.lottttto.miner.viewmodels.PinViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            LotttttoTheme {
                val pinViewModel: PinViewModel = hiltViewModel()
                val pinUiState by pinViewModel.uiState.collectAsStateWithLifecycle()

                if (pinUiState.isPinSet && pinUiState.isPinValidated) {
                    AppNavGraph(
                        onLogout = {
                            pinViewModel.logout()
                        }
                    )
                } else {
                    OnboardingNavGraph(
                        onOnboardingComplete = { pinViewModel.markPinValidated() }
                    )
                }
            }
        }
    }
}
