package com.zilehasnain.qazatracker.ui.dashboard

import com.zilehasnain.qazatracker.domain.model.MilestoneData

/**
 * Turns the stream of "all milestones so far" into "which ones just happened", so the card is
 * an event rather than something that reappears on every launch.
 *
 * The first emission only establishes what already existed (nothing is celebrated for it).
 * Milestones are append-only and arrive newest first, so after that any growth in the list
 * is exactly the new entries at its front. Only ones achieved today are celebrated.
 */
class MilestoneCelebrationTracker {
    private var knownCount: Int? = null

    fun newCelebrations(current: List<MilestoneData>): List<MilestoneData> {
        val known = knownCount
        knownCount = current.size
        if (known == null || current.size <= known) return emptyList()
        return current.take(current.size - known).filter { it.isNewToday }
    }
}
