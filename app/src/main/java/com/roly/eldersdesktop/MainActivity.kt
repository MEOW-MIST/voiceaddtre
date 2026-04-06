package com.roly.eldersdesktop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.roly.eldersdesktop.launcher.EldersLauncherApp
import com.roly.eldersdesktop.ui.theme.EldersdesktopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EldersdesktopTheme {
                EldersLauncherApp()
            }
        }
    }
}
