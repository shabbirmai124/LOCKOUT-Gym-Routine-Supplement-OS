package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LockoutDatabase
import com.example.data.LockoutRepository
import com.example.engine.RestTimerEngine
import com.example.engine.SupplementAlarmScheduler
import com.example.ui.screens.LockoutAppShell
import com.example.ui.theme.LockoutTheme
import com.example.viewmodel.LockoutViewModel
import com.example.viewmodel.LockoutViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LockoutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = LockoutDatabase.getInstance(applicationContext)
        val repository = LockoutRepository(database.lockoutDao())
        val timerEngine = RestTimerEngine(applicationContext)
        val alarmScheduler = SupplementAlarmScheduler(applicationContext)
        val factory = LockoutViewModelFactory(repository, timerEngine, alarmScheduler)
        viewModel = ViewModelProvider(this, factory)[LockoutViewModel::class.java]

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val isDaylight = state.prefs.isDaylightMode

            LaunchedEffect(isDaylight) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDaylight) {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDaylight) {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    }
                )
            }

            LockoutTheme(daylightMode = isDaylight) {
                LockoutAppShell(viewModel = viewModel)
            }
        }
    }
}
