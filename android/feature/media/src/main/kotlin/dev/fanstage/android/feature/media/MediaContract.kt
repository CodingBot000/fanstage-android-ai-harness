package dev.fanstage.android.feature.media

import dev.fanstage.android.core.model.CatalogSnapshot

sealed interface MediaState {
    data object Loading : MediaState

    data object Error : MediaState

    data class Ready(val data: CatalogSnapshot) : MediaState
}

sealed interface MediaUiAction {
    data object RetryClicked : MediaUiAction
}
