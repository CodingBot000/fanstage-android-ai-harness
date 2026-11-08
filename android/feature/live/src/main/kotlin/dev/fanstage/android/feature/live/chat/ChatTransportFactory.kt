package dev.fanstage.android.feature.live.chat

import javax.inject.Inject

class ChatTransportFactory @Inject constructor() {
    fun create(roomId: String): ChatTransport = FakeChatTransport(roomId, memoryBudget = ProcessChatMemory.budget)
}
