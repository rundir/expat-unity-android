package de.expatunity.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import de.expatunity.app.ui.theme.ExpatUnityTheme
import de.expatunity.core.network.NetworkModule
import de.expatunity.feature.catalog.CatalogRoute
import de.expatunity.feature.catalog.CatalogViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpatUnityTheme {
                ExpatUnityApp()             }
        }
    }
}
