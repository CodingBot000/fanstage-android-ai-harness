package dev.fanstage.android.feature.messages

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Navigation-entry-owned memory only; never writes user text into SavedStateHandle. */
class ConversationDraftViewModel : ViewModel() {
    private val mutableText = MutableStateFlow("")
    val text = mutableText.asStateFlow()
    fun update(value: String) { mutableText.value = value }
    fun submit(accept: (String) -> Boolean): Boolean {
        val submitted = mutableText.value
        if (submitted.isBlank()) return false
        val accepted = try { accept(submitted) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { false }
        if (accepted && mutableText.value == submitted) mutableText.value = ""
        return accepted
    }
    override fun onCleared() { mutableText.value = ""; super.onCleared() }
}
