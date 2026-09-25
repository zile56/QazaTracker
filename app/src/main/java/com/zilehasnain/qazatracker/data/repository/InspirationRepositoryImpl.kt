package com.zilehasnain.qazatracker.data.repository

import com.zilehasnain.qazatracker.data.local.dao.InspirationDao
import com.zilehasnain.qazatracker.data.local.entity.InspirationBookmarkEntity
import com.zilehasnain.qazatracker.domain.repository.InspirationRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InspirationRepositoryImpl @Inject constructor(
    private val dao: InspirationDao
) : InspirationRepository {

    override fun observeBookmarkedIds(): Flow<List<String>> =
        dao.observeBookmarks().map { rows -> rows.map { it.itemId } }

    override suspend fun setBookmarked(itemId: String, bookmarked: Boolean) {
        if (bookmarked) dao.insert(InspirationBookmarkEntity(itemId, Instant.now())) else dao.delete(itemId)
    }
}
