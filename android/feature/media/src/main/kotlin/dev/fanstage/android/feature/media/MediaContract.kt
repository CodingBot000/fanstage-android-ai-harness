package dev.fanstage.android.feature.media

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import dev.fanstage.android.core.model.*

sealed interface MediaState {
    data object Loading : MediaState

    data object Error : MediaState

    data class Ready(val data: DiscoverData) : MediaState
}

sealed interface MediaUiAction {
    data object RetryClicked : MediaUiAction
}
