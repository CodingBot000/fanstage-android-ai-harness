package dev.fanstage.android.core.designsystem

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

@Composable
fun SendIcon(contentDescription: String) {
    Icon(painterResource(R.drawable.ic_send), contentDescription = contentDescription)
}
