package dev.fanstage.android

import dev.fanstage.android.feature.discover.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.fanstage.android.core.data.FakeCommunityRepository
import dev.fanstage.android.core.data.FakeDiscoverRepository
import dev.fanstage.android.core.data.FakeMessageRepository

/** Activity-session dependencies; no Android UI or network client is retained. */
class AppContainer {
    private val discover = FakeDiscoverRepository()
    private val community = FakeCommunityRepository()
    private val messages = FakeMessageRepository()
    val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val value = when (modelClass) {
                DiscoverViewModel::class.java -> DiscoverViewModel(discover)
                CommunityViewModel::class.java -> CommunityViewModel(community)
                MessagesViewModel::class.java -> MessagesViewModel(messages)
                else -> error("Unsupported ViewModel")
            }
            return modelClass.cast(value)!!
        }
    }
}
