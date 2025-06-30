package com.nebula.files.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nebula.files.ui.screens.FileListScreen
import com.nebula.files.ui.screens.SettingsScreen

@Composable
fun NebulaNavigation(
    windowSizeClass: WindowSizeClass
) {
    val navController = rememberNavController()
    val isExpandedScreen = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    
    NavHost(
        navController = navController,
        startDestination = "file_list"
    ) {
        composable("file_list") {
            FileListScreen(
                navController = navController,
                isExpandedScreen = isExpandedScreen
            )
        }
        
        composable("settings") {
            SettingsScreen(
                navController = navController
            )
        }
    }
}