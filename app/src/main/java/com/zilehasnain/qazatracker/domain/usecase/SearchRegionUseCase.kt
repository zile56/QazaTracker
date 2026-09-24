package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.RegionCatalog
import com.zilehasnain.qazatracker.domain.model.RegionInfo
import java.text.Normalizer
import javax.inject.Inject

/**
 * Finds countries as the user types: "pak" finds Pakistan, "uk" finds the United Kingdom, and a
 * small typo ("pakstan") still lands. Runs over the built-in catalog, so it is instant and offline.
 */
class SearchRegionUseCase @Inject constructor() {

    operator fun invoke(query: String): List<RegionInfo> = search(query)

    companion object {
        private val Aliases = mapOf(
            "UK" to "GB", "USA" to "US", "UAE" to "AE", "KSA" to "SA", "ENGLAND" to "GB",
            "BRITAIN" to "GB", "AMERICA" to "US", "HOLLAND" to "NL", "BURMA" to "MM",
            "TURKIYE" to "TR", "IVORY COAST" to "CI", "COTE D'IVOIRE" to "CI"
        )

        fun search(query: String, catalog: List<RegionInfo> = RegionCatalog.all): List<RegionInfo> {
            val q = normalize(query)
            if (q.isEmpty()) return catalog

            val aliasTarget = Aliases[q.uppercase()]
            return catalog
                .mapNotNull { region ->
                    val score = score(q, normalize(region.name), region.code, aliasTarget)
                    if (score == null) null else region to score
                }
                .sortedWith(compareBy({ it.second }, { it.first.name }))
                .map { it.first }
        }

        /** Lower is better; null means no match. */
        private fun score(q: String, name: String, code: String, aliasTarget: String?): Int? {
            if (aliasTarget == code) return 0
            if (name == q || code.equals(q, ignoreCase = true)) return 0
            if (name.startsWith(q)) return 1
            if (name.split(' ', '-').any { it.startsWith(q) }) return 2
            if (name.contains(q)) return 3
            // Typos: only worth trying once there is enough to compare.
            if (q.length >= 4) {
                val distance = name.split(' ', '-').filter { it.length >= 3 }
                    .minOfOrNull { word -> editDistance(q, word.take(q.length + 1)) } ?: return null
                val allowed = if (q.length >= 6) 2 else 1
                if (distance <= allowed) return 4 + distance
            }
            return null
        }

        private fun normalize(text: String): String =
            Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "")

        private fun editDistance(a: String, b: String): Int {
            var previous = IntArray(b.length + 1) { it }
            for (i in 1..a.length) {
                val current = IntArray(b.length + 1)
                current[0] = i
                for (j in 1..b.length) {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    current[j] = minOf(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + cost)
                }
                previous = current
            }
            return previous[b.length]
        }
    }
}
