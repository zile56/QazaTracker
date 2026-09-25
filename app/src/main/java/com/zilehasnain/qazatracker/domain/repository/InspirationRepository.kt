package com.zilehasnain.qazatracker.domain.repository

import kotlinx.coroutines.flow.Flow

/** Which built-in verses and hadith the user has saved. */
interface InspirationRepository {
    /** Ids of saved items, most recently saved first. */
    fun observeBookmarkedIds(): Flow<List<String>>

    suspend fun setBookmarked(itemId: String, bookmarked: Boolean)
}
