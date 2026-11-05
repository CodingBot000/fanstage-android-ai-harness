package dev.fanstage.android.core.data

import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.test.runTest

class FakeRepositoriesTest {
    @Test fun catalogIdsJoinAcrossFixtures() = runTest {
        val catalog = FakeDiscoverRepository().load()
        val posts = FakeCommunityRepository().posts()
        val conversations = FakeMessageRepository().snapshot()
        val ids = catalog.artists.map { it.id }.toSet()
        assertEquals(3, ids.size)
        assertTrue(catalog.content.all { it.artistId in ids })
        assertTrue(posts.all { it.artistId in ids })
        assertEquals(ids, conversations.map { it.artistId }.toSet())
    }
    @Test fun communityRetryDoesNotDiscardLocalChanges() = runTest {
        val repo = FakeCommunityRepository()
        repo.toggleLike("post-paper-lantern")
        repo.addComment("post-paper-lantern", "hello")
        val loaded = repo.posts().first { it.id == "post-paper-lantern" }
        assertTrue(loaded.liked)
        assertEquals(listOf("hello"), loaded.comments)
    }
    @Test fun communityMutationsPersistAndAreScoped() {
        val repo = FakeCommunityRepository()
        val id = "post-paper-lantern"
        val liked = repo.toggleLike(id)
        assertTrue(liked.first { it.id == id }.liked)
        val commented = repo.addComment(id, " hello ")
        assertEquals(listOf("hello"), commented.first { it.id == id }.comments)
        assertFalse(commented.first { it.id != id }.liked)
        assertEquals(commented, repo.addComment(id, "  "))
    }
    @Test fun messagesAreSessionScopedAndBlankInputIgnored() {
        val repo = FakeMessageRepository()
        val before = repo.snapshot()
        assertEquals(before, repo.send("paper-lantern", "  "))
        val sent = repo.send("paper-lantern", " hello ")
        assertEquals("hello", sent.first().messages.last().text)
        assertFalse(repo.markRead("paper-lantern").first().unread)
        assertTrue(repo.snapshot().last().unread)
        assertEquals(before, FakeMessageRepository().snapshot())
    }
}
