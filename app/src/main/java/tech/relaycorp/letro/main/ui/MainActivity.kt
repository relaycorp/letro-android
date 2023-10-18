package tech.relaycorp.letro.main.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.relaycorp.letro.R
import tech.relaycorp.letro.main.ActionWithAppStartInfo
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.push.KEY_PUSH_ACTION
import tech.relaycorp.letro.ui.navigation.Action
import tech.relaycorp.letro.ui.navigation.LetroNavHost
import tech.relaycorp.letro.ui.theme.LetroTheme
import tech.relaycorp.letro.ui.utils.StringsProvider
import tech.relaycorp.letro.utils.intent.goToNotificationSettings
import tech.relaycorp.letro.utils.intent.openAwala
import tech.relaycorp.letro.utils.intent.openFile
import tech.relaycorp.letro.utils.intent.openLink
import tech.relaycorp.letro.utils.intent.shareText
import javax.inject.Inject

@AndroidEntryPoint
@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var stringsProvider: StringsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Letro)
        super.onCreate(savedInstanceState)
        observeViewModel()

        setContent {
            LetroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    LetroNavHost(
                        stringsProvider = stringsProvider,
                        onGoToNotificationsSettingsClick = { goToNotificationSettings() },
                        onOpenAwalaClick = { openAwala() },
                        mainViewModel = viewModel,
                    )
                }
            }
        }
        if (savedInstanceState == null) {
            intent?.putExtra(IS_COLD_START, true)
            onNewIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return
        setIntent(intent)
        when (intent.action) {
            KEY_PUSH_ACTION -> handlePushIntent(intent)
            Intent.ACTION_VIEW -> handleAppLinkIntent(intent)
        }
    }

    private fun handlePushIntent(intent: Intent) {
        val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(KEY_PUSH_ACTION, Action::class.java)
        } else {
            intent.getParcelableExtra(KEY_PUSH_ACTION)
        }
        action?.let {
            viewModel.onNewAction(
                action = ActionWithAppStartInfo(
                    action = action,
                    isColdStart = intent.getBooleanExtra(IS_COLD_START, false),
                ),
            )
        }
    }

    private fun handleAppLinkIntent(intent: Intent) {
        val link = intent.data ?: return
        viewModel.onLinkOpened(
            link = link.toString(),
            isColdStart = intent.getBooleanExtra(IS_COLD_START, false),
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.openLinkSignal.collect(::openLink)
        }
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.joinMeOnLetroSignal.collect { link ->
                shareText(getString(R.string.join_me_on_letro, link))
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.openFileSignal.collect(::openFile)
        }
    }
}

private const val IS_COLD_START = "is_cold_start"
