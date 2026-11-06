package dev.fanstage.android.feature.live

/** Session-local opaque identifiers stop callbacks from selecting a replaced track group. */
internal class TrackGroupIds<T> {
    private val ids = mutableMapOf<T, Long>()
    private var next = 1L
    fun reconcile(groups: Set<T>) {
        ids.keys.retainAll(groups)
        groups.forEach { ids.getOrPut(it) { next++ } }
    }
    fun id(group: T): Long? = ids[group]
}
