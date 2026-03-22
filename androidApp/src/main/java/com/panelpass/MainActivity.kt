package com.panelpass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.panelpass.features.home.ui.MainScreen
import com.panelpass.platform.auth.GoogleAuthRepository
import com.panelpass.shell.ActivityHolder

class MainActivity : ComponentActivity() {

    override fun onResume() {
        super.onResume()
        ActivityHolder.set(this)
    }

    override fun onPause() {
        super.onPause()
        ActivityHolder.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GoogleAuthRepository.REQUEST_CODE_SIGN_IN) {
            (application as? PanelPassApp)?.deliverSignInResult(data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }
}
