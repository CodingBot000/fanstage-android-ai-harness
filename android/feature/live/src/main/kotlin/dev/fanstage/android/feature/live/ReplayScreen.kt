package dev.fanstage.android.feature.live

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ReplayScreen(onBack: () -> Unit) {
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).testTag("replay_detail")) {
            TextButton(onClick = onBack, modifier = Modifier.testTag("replay_back")) { Text(stringResource(R.string.replay_back_generic)) }
            Text(stringResource(R.string.replay_title), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Text("Paper Lantern · Studio diary", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { PlayerPanel("replay-studio-diary") } }
        }
    }
}
