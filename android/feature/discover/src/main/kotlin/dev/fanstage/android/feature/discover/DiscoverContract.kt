package dev.fanstage.android.feature.discover

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import dev.fanstage.android.core.designsystem.*
import dev.fanstage.android.core.model.*

sealed interface DiscoverState {
    data object Loading : DiscoverState

    data object Error : DiscoverState

    data class Ready(val data: DiscoverData) : DiscoverState
}

sealed interface DiscoverUiAction {
    data object RetryClicked : DiscoverUiAction
}
