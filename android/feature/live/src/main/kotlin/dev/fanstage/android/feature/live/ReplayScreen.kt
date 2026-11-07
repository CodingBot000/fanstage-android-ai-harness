package dev.fanstage.android.feature.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.fanstage.android.core.designsystem.BackTitleBar

@Composable
fun ReplayScreen(
    onBack: () -> Unit,
    sourceUri: String = "asset:///hls/captionless.m3u8",
    title: String = "Paper Lantern · Studio diary",
) {
    VideoPlaybackTheme {
        Scaffold(
            modifier = Modifier.testTag("video_playback_dark_screen"),
            containerColor = Color.Black,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = { BackTitleBar(title, onBack, backTag = "replay_back") },
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .background(Color.Black)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("replay_detail"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.replay_title),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
                Box(Modifier.fillMaxWidth().background(Color.Black)) {
                    PlayerPanel("replay-studio-diary", sourceUri, settingsAtEnd = true)
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().testTag("vod_chat_unavailable"),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("ⓘ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            stringResource(R.string.vod_chat_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
