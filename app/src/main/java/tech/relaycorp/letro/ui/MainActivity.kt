package tech.relaycorp.letro.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.relaycorp.letro.R
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.push.KEY_PUSH_ACTION
import tech.relaycorp.letro.push.model.PushAction
import tech.relaycorp.letro.ui.navigation.LetroNavHost
import tech.relaycorp.letro.ui.theme.LetroTheme
import tech.relaycorp.letro.ui.utils.StringsProvider
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var stringsProvider: StringsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Letro)
        super.onCreate(savedInstanceState)
        observeViewModel()

        setContent {
            val navController = rememberNavController()

            LetroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    LetroNavHost(
                        navController = navController,
                        stringsProvider = stringsProvider,
                        onGoToSettingsClick = { goToNotificationsSettings() },
                        mainViewModel = viewModel,
                    )
                }
            }
        }
        onNewIntent(intent)
    }

    @Suppress("DEPRECATION")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return
        setIntent(intent)
        val pushAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(KEY_PUSH_ACTION, PushAction::class.java)
        } else {
            intent.getParcelableExtra(KEY_PUSH_ACTION)
        }
        viewModel.onNewPushAction(pushAction)
    }

    private fun goToNotificationsSettings() {
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("app_package", packageName)
        intent.putExtra("app_uid", applicationInfo.uid)
        intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
        startActivity(intent)
    }

    private fun observeViewModel() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.openLinkSignal.collect { link ->
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(link),
                        ),
                    )
                } catch (a: ActivityNotFoundException) {
                    Toast
                        .makeText(this@MainActivity, R.string.no_browser, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.joinMeOnLetroSignal.collect { link ->
                try {
                    val text = getString(R.string.join_me_on_letro, link)
                    startActivity(
                        Intent(
                            Intent.ACTION_SEND,
                        ).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        },
                    )
                } catch (a: ActivityNotFoundException) {
                    Toast
                        .makeText(this@MainActivity, R.string.no_app_to_share, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.openFileSignal.collect { file ->
                try {
                    startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            setDataAndType(FileProvider.getUriForFile(this@MainActivity, AUTHORITY, file.toFile()), file.extension.mimeType)
                        },
                    )
                } catch (e: ActivityNotFoundException) {
                    Toast
                        .makeText(this@MainActivity, R.string.no_app_to_open_file, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private companion object {
        private const val AUTHORITY = "tech.relaycorp.letro.provider"
    }
}
