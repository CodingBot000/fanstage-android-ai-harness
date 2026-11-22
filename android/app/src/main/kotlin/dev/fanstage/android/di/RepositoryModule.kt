package dev.fanstage.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.fanstage.android.core.data.FakeCatalog
import dev.fanstage.android.core.data.FakeCatalogRepository
import dev.fanstage.android.core.data.FakeCommunityRepository
import dev.fanstage.android.core.data.FakeMessageRepository
import dev.fanstage.android.core.model.ArtistCatalog
import dev.fanstage.android.core.model.CatalogRepository
import dev.fanstage.android.core.model.CommunityRepository
import dev.fanstage.android.core.model.MessageRepository

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides fun catalog(): CatalogRepository = FakeCatalogRepository()
    @Provides fun artists(): ArtistCatalog = FakeCatalog
    @Provides fun community(): CommunityRepository = FakeCommunityRepository()
    @Provides fun messages(): MessageRepository = FakeMessageRepository()
}
