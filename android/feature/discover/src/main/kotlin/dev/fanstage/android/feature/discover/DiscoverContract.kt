package dev.fanstage.android.feature.discover

import dev.fanstage.android.core.model.DiscoverData

sealed interface DiscoverState {
    data object Loading : DiscoverState

    data object Error : DiscoverState

    data class Ready(val data: DiscoverData) : DiscoverState
}

sealed interface DiscoverUiAction {
    data object RetryClicked : DiscoverUiAction
}
