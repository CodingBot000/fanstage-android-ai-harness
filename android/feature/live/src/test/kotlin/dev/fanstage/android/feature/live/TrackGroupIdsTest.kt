package dev.fanstage.android.feature.live

import org.junit.Assert.*
import org.junit.Test

class TrackGroupIdsTest {
    @Test fun replacedGroupsInvalidateOldIdsAndReentryGetsNewId() {
        val ids = TrackGroupIds<String>()
        ids.reconcile(setOf("first", "text"))
        val first = ids.id("first")
        val text = ids.id("text")
        ids.reconcile(setOf("second", "text"))
        assertNull(ids.id("first"))
        assertEquals(text, ids.id("text"))
        assertNotEquals(first, ids.id("second"))
        ids.reconcile(setOf("first"))
        assertNotEquals(first, ids.id("first"))
    }
}
