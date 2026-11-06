package dev.fanstage.android.feature.live

import dev.fanstage.android.core.designsystem.BackTitleBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ReplayScreen(onBack: () -> Unit, sourceUri: String = "asset:///hls/vod.m3u8", title: String = "Paper Lantern · Studio diary") {
    Scaffold(topBar = { BackTitleBar(title, onBack, backTag = "replay_back") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp).testTag("replay_detail")) {
            Text(stringResource(R.string.replay_title), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(16.dp))
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { PlayerPanel("replay-studio-diary", sourceUri) } }
        }
    }
}
