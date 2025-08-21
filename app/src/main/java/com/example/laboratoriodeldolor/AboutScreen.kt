package com.example.laboratoriodeldolor

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import android.content.pm.PackageManager

@Composable
fun AboutScreen(onBack: () -> Unit = {}) {
    val ctx = LocalContext.current
    val version = remember {
        try {
            val pi = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            pi.versionName ?: "1.0"
        } catch (e: Exception) { "1.0" }
    }

    Scaffold { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(id = R.string.about_title), style = MaterialTheme.typography.headlineSmall)
                Text(text = stringResource(id = R.string.about_version, version), modifier = Modifier.padding(top = 8.dp))

                Card(modifier = Modifier.padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = stringResource(id = R.string.about_privacy))
                        Text(text = stringResource(id = R.string.about_terms), modifier = Modifier.padding(top = 8.dp))
                    }
                }

                Text(text = stringResource(id = R.string.about_acknowledgments), modifier = Modifier.padding(top = 16.dp))

                Button(onClick = onBack, modifier = Modifier.padding(top = 24.dp)) {
                    Text(text = stringResource(id = R.string.about_close))
                }
            }
        }
    }
}
