package tech.relaycorp.letro.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.relaycorp.letro.R
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.ui.navigation.LetroNavHost
import tech.relaycorp.letro.ui.theme.LetroTheme
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var snackbarStringsProvider: SnackbarStringsProvider

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
                        snackbarStringsProvider = snackbarStringsProvider,
                    )
                }
            }
        }
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
    }
}
