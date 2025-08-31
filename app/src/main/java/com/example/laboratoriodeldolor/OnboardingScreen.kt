package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onFinish: () -> Unit, onSkip: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())) {

        Text(text = stringResource(id = R.string.onboarding_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(id = R.string.onboarding_intro), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(id = R.string.onboarding_smärtgel_usage), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(id = R.string.onboarding_warnings), style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(20.dp))
    com.example.laboratoriodeldolor.ui.components.PrimaryButton(text = stringResource(id = R.string.onboarding_continue), onClick = { onFinish() })
        Spacer(modifier = Modifier.height(8.dp))
    com.example.laboratoriodeldolor.ui.components.SecondaryButton(text = stringResource(id = R.string.onboarding_skip), onClick = { onSkip() })
    }
}
