package com.example.laboratoriodeldolor

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember

@Composable
fun AboutScreen(onBack: () -> Unit = {}, onOpenPrivacy: () -> Unit = {}) {
    val ctx = LocalContext.current
    val version = remember {
        try {
            val pi = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            pi.versionName ?: "1.0"
        } catch (e: Exception) { "1.0" }
    }

    com.example.laboratoriodeldolor.ui.AppScaffold { innerPadding ->
        // Keep the Surface transparent so the app-wide gradient behind AppScaffold remains visible
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding), color = androidx.compose.ui.graphics.Color.Transparent) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(id = R.string.about_title), style = MaterialTheme.typography.headlineSmall)
                Text(text = stringResource(id = R.string.about_version, version), modifier = Modifier.padding(top = 8.dp))

                Card(modifier = Modifier.padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        com.example.laboratoriodeldolor.ui.components.SecondaryButton(
                            text = stringResource(id = R.string.about_privacy),
                            onClick = {
                                onOpenPrivacy()
                            },
                            modifier = Modifier.padding(top = 0.dp)
                        )
                        Text(text = stringResource(id = R.string.about_terms), modifier = Modifier.padding(top = 8.dp))
                    }
                }

                Text(text = stringResource(id = R.string.about_acknowledgments), modifier = Modifier.padding(top = 16.dp))

                com.example.laboratoriodeldolor.ui.components.SecondaryButton(text = stringResource(id = R.string.about_close), onClick = onBack, modifier = Modifier.padding(top = 24.dp))
            }
        }
    }
}
