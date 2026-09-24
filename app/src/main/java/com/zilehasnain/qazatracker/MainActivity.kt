package com.zilehasnain.qazatracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.zilehasnain.qazatracker.notification.NotificationPermissionGate
import com.zilehasnain.qazatracker.notification.NotificationPermissionPolicy
import com.zilehasnain.qazatracker.ui.navigation.QazaNavHost
import com.zilehasnain.qazatracker.ui.theme.QazaTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var notificationPermissionGate: NotificationPermissionGate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QazaTrackerTheme {
                RequestNotificationPermissionOnFirstLaunch(notificationPermissionGate)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    QazaNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Prompts for POST_NOTIFICATIONS at most once per install. Denying doesn't break anything —
 * QazaNotificationWorker already checks the permission itself before posting, so a denial (or
 * simply never being asked, on API <33 where the permission doesn't exist) just means reminder
 * notifications silently never appear. There is deliberately no re-prompt or nag path.
 */
@Composable
private fun RequestNotificationPermissionOnFirstLaunch(gate: NotificationPermissionGate) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        // Granted or denied, there's nothing else to do here — see the kdoc above.
    }

    LaunchedEffect(Unit) {
        val alreadyPrompted = gate.observeHasPrompted().first()
        val alreadyGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (NotificationPermissionPolicy.shouldPrompt(Build.VERSION.SDK_INT, alreadyPrompted, alreadyGranted)) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        gate.markPrompted()
    }
}
