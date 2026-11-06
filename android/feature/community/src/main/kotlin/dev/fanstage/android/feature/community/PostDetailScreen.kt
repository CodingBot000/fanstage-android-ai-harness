package dev.fanstage.android.feature.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.fanstage.android.core.artwork.artworkResource
import dev.fanstage.android.core.designsystem.BackTitleBar
import dev.fanstage.android.core.designsystem.SendIcon
import dev.fanstage.android.core.model.*

@Composable
fun PostDetailScreen(
    post: CommunityPost?,
    comment: String,
    onCommentChange: (String) -> Unit,
    onAction: (CommunityUiAction) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            BackTitleBar(
                post?.title ?: stringResource(R.string.community),
                onBack,
                backTag = "post_back",
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().testTag("post_detail")) {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (post != null) {
                    item {
                        Image(
                            painterResource(artworkResource(post.image)),
                            null,
                            Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(post.title, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            stringResource(R.string.community),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            if (post.body == "A little moment from our world.")
                                stringResource(R.string.demo_post_body)
                            else post.body
                        )
                    }
                    item {
                        IconToggleButton(
                            checked = post.liked,
                            onCheckedChange = { onAction(CommunityUiAction.LikeClicked(post.id)) },
                            modifier = Modifier.testTag("post_like"),
                        ) {
                            Icon(
                                painterResource(
                                    if (post.liked) R.drawable.heart_filled
                                    else R.drawable.heart_outline
                                ),
                                contentDescription = stringResource(R.string.like),
                                tint = Color.Black,
                            )
                        }
                    }
                    items(post.comments) { Text(it) }
                } else item { Text(stringResource(R.string.community_empty)) }
            }
            if (post != null) {
                val submit: () -> Unit = {
                    onAction(CommunityUiAction.CommentSubmitted(post.id, comment))
                    onCommentChange("")
                }
                val sendDescription = stringResource(R.string.send_comment)
                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    label = { Text(stringResource(R.string.comment)) },
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp)
                            .testTag("comment_input"),
                    trailingIcon = {
                        IconButton(onClick = submit, modifier = Modifier.testTag("comment_send")) {
                            SendIcon(sendDescription)
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submit() }),
                    maxLines = 4,
                )
            }
        }
    }
}
