package com.coptimize.openinventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.coptimize.openinventory.navigation.AppNavigation
import com.coptimize.openinventory.ui.theme.OpenInventoryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenInventoryTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                AppNavigation(windowSizeClass = windowSizeClass)
            }
        }
    }
}