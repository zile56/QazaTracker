package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.PrayerCalculationMethod
import com.zilehasnain.qazatracker.domain.model.RegionCatalog
import com.zilehasnain.qazatracker.domain.model.RegionInfo
import com.zilehasnain.qazatracker.domain.model.RegionSelection
import javax.inject.Inject

/** What method (and Asr rule) to use for the user's choices, falling back to a neutral default. */
class GetRecommendedMethodUseCase @Inject constructor() {

    /** The country's suggestion, or null if the code isn't in the catalog. */
    operator fun invoke(countryCode: String?): RegionInfo? = RegionCatalog.byCode(countryCode)

    /** An explicit choice wins; otherwise the country's recommendation; otherwise Muslim World League. */
    fun effectiveMethod(selection: RegionSelection): PrayerCalculationMethod =
        selection.methodOverride
            ?: RegionCatalog.byCode(selection.countryCode)?.recommended
            ?: PrayerCalculationMethod.MWL

    fun usesHanafiAsr(selection: RegionSelection): Boolean =
        RegionCatalog.byCode(selection.countryCode)?.hanafiAsr ?: false
}
