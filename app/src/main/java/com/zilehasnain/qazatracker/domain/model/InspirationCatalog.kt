package com.zilehasnain.qazatracker.domain.model

/**
 * The built-in library: short, well-known verses and hadith about prayer, remembrance, patience
 * and trust in Allah. It ships inside the app, so nothing is downloaded and it works offline.
 *
 * The English is a plain rendering of the meaning, not a published translation, and Arabic text
 * is written with standard diacritics. Have a qualified person check both before each release.
 */
object InspirationCatalog {

    private fun v(
        surah: String,
        number: Int,
        ayah: Int,
        arabic: String,
        english: String,
        reflection: String
    ) = QuranVerse("q$number:$ayah", surah, number, ayah, arabic, english, reflection)

    private fun h(id: Int, narrator: String, source: String, text: String) =
        Hadith("h${id.toString().padStart(2, '0')}", narrator, text, source)

    val verses: List<QuranVerse> = listOf(
        v(
            "Al-Fatiha", 1, 5,
            "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
            "You alone we worship, and You alone we ask for help.",
            "Recited in every rak'ah: worship and asking for help belong together."
        ),
        v(
            "Al-Fatiha", 1, 6,
            "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
            "Guide us to the straight path.",
            "Even a lifelong believer asks for guidance every day. Returning to prayer is part of that path."
        ),
        v(
            "Al-Baqarah", 2, 25,
            "وَبَشِّرِ الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ أَنَّ لَهُمْ جَنَّاتٍ تَجْرِي مِنْ تَحْتِهَا الْأَنْهَارُ",
            "Give good news to those who believe and do righteous deeds: for them are gardens beneath which rivers flow.",
            "Belief and steady good deeds are promised together."
        ),
        v(
            "Al-Baqarah", 2, 45,
            "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ",
            "Seek help through patience and prayer.",
            "When the debt feels heavy, patience and prayer are the tools you are told to use."
        ),
        v(
            "Al-Baqarah", 2, 152,
            "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
            "So remember Me, and I will remember you. Be grateful to Me and do not deny Me.",
            "Every moment of remembrance is answered."
        ),
        v(
            "Al-Baqarah", 2, 153,
            "يَا أَيُّهَا الَّذِينَ آمَنُوا اسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
            "You who believe, seek help through patience and prayer. Indeed, Allah is with the patient.",
            "Clearing a long backlog is a test of patience, and Allah is with you in it."
        ),
        v(
            "Al-Baqarah", 2, 186,
            "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
            "When My servants ask you about Me: I am near. I answer the call of the caller when he calls on Me.",
            "There is no distance to cross before you can ask."
        ),
        v(
            "Al-Baqarah", 2, 201,
            "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            "Our Lord, give us good in this world and good in the Hereafter, and protect us from the punishment of the Fire.",
            "A short dua that asks for everything that matters."
        ),
        v(
            "Al-Baqarah", 2, 238,
            "حَافِظُوا عَلَى الصَّلَوَاتِ وَالصَّلَاةِ الْوُسْطَىٰ وَقُومُوا لِلَّهِ قَانِتِينَ",
            "Guard the prayers, and the middle prayer, and stand before Allah in devotion.",
            "Guarding prayer is the goal every make-up prayer is working toward."
        ),
        v(
            "Al-Baqarah", 2, 286,
            "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
            "Allah does not burden a soul beyond what it can bear.",
            "Your pace is enough. A steady few each day is within your ability."
        ),
        v(
            "Al-Imran", 3, 139,
            "وَلَا تَهِنُوا وَلَا تَحْزَنُوا وَأَنْتُمُ الْأَعْلَوْنَ إِنْ كُنْتُمْ مُؤْمِنِينَ",
            "Do not weaken and do not grieve, for you will be uppermost if you are believers.",
            "Discouragement is the first thing to put down."
        ),
        v(
            "Al-Imran", 3, 159,
            "فَإِذَا عَزَمْتَ فَتَوَكَّلْ عَلَى اللَّهِ إِنَّ اللَّهَ يُحِبُّ الْمُتَوَكِّلِينَ",
            "Once you have made a decision, put your trust in Allah. Allah loves those who trust in Him.",
            "Decide, then act, then rely on Him for the result."
        ),
        v(
            "Al-Imran", 3, 173,
            "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
            "Allah is enough for us, and He is the best disposer of affairs.",
            "A phrase to reach for whenever a task feels bigger than you."
        ),
        v(
            "An-Nisa", 4, 103,
            "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَوْقُوتًا",
            "Indeed, prayer has been decreed upon the believers at fixed times.",
            "Prayer has its times, and coming back to them is always possible."
        ),
        v(
            "Al-Anfal", 8, 46,
            "وَاصْبِرُوا إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
            "And be patient. Indeed, Allah is with the patient.",
            "Patience is not waiting passively. It is continuing."
        ),
        v(
            "At-Tawbah", 9, 51,
            "قُلْ لَنْ يُصِيبَنَا إِلَّا مَا كَتَبَ اللَّهُ لَنَا هُوَ مَوْلَانَا وَعَلَى اللَّهِ فَلْيَتَوَكَّلِ الْمُؤْمِنُونَ",
            "Say: Nothing will befall us except what Allah has decreed for us. He is our protector, and in Allah let the believers put their trust.",
            "Trust removes the weight of worrying about what you cannot control."
        ),
        v(
            "Hud", 11, 114,
            "وَأَقِمِ الصَّلَاةَ طَرَفَيِ النَّهَارِ وَزُلَفًا مِنَ اللَّيْلِ إِنَّ الْحَسَنَاتِ يُذْهِبْنَ السَّيِّئَاتِ ذَٰلِكَ ذِكْرَىٰ لِلذَّاكِرِينَ",
            "Establish prayer at the two ends of the day and in the early part of the night. Indeed, good deeds erase bad deeds. That is a reminder for those who remember.",
            "Every prayer you make up is a good deed that counts."
        ),
        v(
            "Yusuf", 12, 87,
            "وَلَا تَيْأَسُوا مِنْ رَوْحِ اللَّهِ",
            "Do not despair of the mercy of Allah.",
            "No backlog is too large for hope."
        ),
        v(
            "Ar-Ra'd", 13, 28,
            "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            "Truly, in the remembrance of Allah do hearts find rest.",
            "A calm heart is one of the first rewards of coming back."
        ),
        v(
            "Ibrahim", 14, 40,
            "رَبِّ اجْعَلْنِي مُقِيمَ الصَّلَاةِ وَمِنْ ذُرِّيَّتِي رَبَّنَا وَتَقَبَّلْ دُعَاءِ",
            "My Lord, make me one who establishes prayer, and from my descendants too. Our Lord, accept my prayer.",
            "Even Ibrahim ﷺ asked for help to keep up prayer."
        ),
        v(
            "Al-Isra", 17, 78,
            "أَقِمِ الصَّلَاةَ لِدُلُوكِ الشَّمْسِ إِلَىٰ غَسَقِ اللَّيْلِ وَقُرْآنَ الْفَجْرِ إِنَّ قُرْآنَ الْفَجْرِ كَانَ مَشْهُودًا",
            "Establish prayer from the decline of the sun to the darkness of the night, and the recitation at dawn. Indeed, the dawn recitation is witnessed.",
            "Fajr is singled out as a prayer that is witnessed."
        ),
        v(
            "Ta-Ha", 20, 14,
            "إِنَّنِي أَنَا اللَّهُ لَا إِلَٰهَ إِلَّا أَنَا فَاعْبُدْنِي وَأَقِمِ الصَّلَاةَ لِذِكْرِي",
            "Indeed, I am Allah. There is no god but Me, so worship Me and establish prayer for My remembrance.",
            "Prayer exists so that you remember Him."
        ),
        v(
            "Al-Mu'minun", 23, 2,
            "الَّذِينَ هُمْ فِي صَلَاتِهِمْ خَاشِعُونَ",
            "Those who are humble in their prayers.",
            "Presence of heart matters as much as the count."
        ),
        v(
            "Al-Ankabut", 29, 45,
            "وَأَقِمِ الصَّلَاةَ إِنَّ الصَّلَاةَ تَنْهَىٰ عَنِ الْفَحْشَاءِ وَالْمُنْكَرِ",
            "Establish prayer. Indeed, prayer keeps one away from shameful and wrong deeds.",
            "Prayer protects you as well as pleasing Him."
        ),
        v(
            "Al-Ahzab", 33, 41,
            "يَا أَيُّهَا الَّذِينَ آمَنُوا اذْكُرُوا اللَّهَ ذِكْرًا كَثِيرًا",
            "You who believe, remember Allah with much remembrance.",
            "Remembrance fits into any moment of the day."
        ),
        v(
            "Az-Zumar", 39, 10,
            "إِنَّمَا يُوَفَّى الصَّابِرُونَ أَجْرَهُمْ بِغَيْرِ حِسَابٍ",
            "Indeed, the patient will be given their reward without measure.",
            "Patience is the one reward that is not counted out."
        ),
        v(
            "Az-Zumar", 39, 53,
            "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنْفُسِهِمْ لَا تَقْنَطُوا مِنْ رَحْمَةِ اللَّهِ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا إِنَّهُ هُوَ الْغَفُورُ الرَّحِيمُ",
            "Say: My servants who have transgressed against themselves, do not despair of the mercy of Allah. Indeed, Allah forgives all sins. He is the Forgiving, the Merciful.",
            "Missed prayers are a reason to return, never a reason to give up."
        ),
        v(
            "Ghafir", 40, 60,
            "وَقَالَ رَبُّكُمُ ادْعُونِي أَسْتَجِبْ لَكُمْ",
            "Your Lord says: Call on Me and I will answer you.",
            "An open invitation, at any hour."
        ),
        v(
            "Qaf", 50, 16,
            "وَنَحْنُ أَقْرَبُ إِلَيْهِ مِنْ حَبْلِ الْوَرِيدِ",
            "We are nearer to him than his jugular vein.",
            "He is closer than any worry you carry."
        ),
        v(
            "Adh-Dhariyat", 51, 56,
            "وَمَا خَلَقْتُ الْجِنَّ وَالْإِنْسَ إِلَّا لِيَعْبُدُونِ",
            "I did not create jinn and mankind except to worship Me.",
            "Worship is the purpose your days are built around."
        ),
        v(
            "At-Talaq", 65, 2,
            "وَمَنْ يَتَّقِ اللَّهَ يَجْعَلْ لَهُ مَخْرَجًا",
            "Whoever fears Allah, He makes for him a way out.",
            "A way out exists for every difficulty."
        ),
        v(
            "At-Talaq", 65, 3,
            "وَمَنْ يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ إِنَّ اللَّهَ بَالِغُ أَمْرِهِ",
            "Whoever relies on Allah, He is sufficient for him. Indeed, Allah accomplishes His purpose.",
            "Rely on Him and keep going."
        ),
        v(
            "Ash-Sharh", 94, 6,
            "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "Indeed, with hardship comes ease.",
            "Ease travels with the hardship, not after it."
        )
    )

    val hadith: List<Hadith> = listOf(
        h(1, "Abdullah ibn Amr", "Sahih al-Bukhari", "The best of you are those who have the best character."),
        h(2, "Umar ibn al-Khattab", "Sahih al-Bukhari and Sahih Muslim", "Actions are judged by intentions, and each person will have what they intended."),
        h(3, "Anas ibn Malik", "Sahih al-Bukhari and Sahih Muslim", "Whoever forgets a prayer or sleeps through it, let him pray it when he remembers it. There is no other expiation for it."),
        h(4, "Abu Hurairah", "Jami at-Tirmidhi", "The first thing a servant will be held to account for on the Day of Resurrection is his prayer. If it is sound, he will have succeeded and prospered."),
        h(5, "Abu Hurairah", "Sahih al-Bukhari and Sahih Muslim", "If there were a river at the door of one of you and he bathed in it five times every day, would any dirt remain on him? The five prayers are like that: by them Allah wipes away sins."),
        h(6, "Abu Hurairah", "Sahih al-Bukhari and Sahih Muslim", "Two phrases are light on the tongue, heavy on the scale, and beloved to the Most Merciful: SubhanAllahi wa bihamdihi, SubhanAllahil-Azim."),
        h(7, "Aisha", "Sahih al-Bukhari and Sahih Muslim", "The deeds most beloved to Allah are the most consistent, even if they are small."),
        h(8, "Suhayb ibn Sinan", "Sahih Muslim", "How wonderful is the affair of the believer: it is all good for him. If good times come, he is grateful, and that is good for him. If hardship comes, he is patient, and that is good for him."),
        h(9, "Abu Hurairah", "Sahih Muslim", "The strong believer is better and more beloved to Allah than the weak believer, though there is good in both. Strive for what benefits you, seek Allah's help, and do not give up."),
        h(10, "Jundub ibn Abdullah", "Sahih Muslim", "Whoever prays Fajr is under the protection of Allah."),
        h(11, "Abu Malik al-Ashari", "Sahih Muslim", "Cleanliness is half of faith, and prayer is light."),
        h(12, "Abu Hurairah", "Sahih Muslim", "Whoever glorifies Allah thirty-three times, praises Him thirty-three times and declares His greatness thirty-three times after every prayer, and completes a hundred with La ilaha illallah, his sins are forgiven even if they are like the foam of the sea."),
        h(13, "Anas ibn Malik", "Jami at-Tirmidhi", "Every son of Adam makes mistakes, and the best of those who make mistakes are those who repent."),
        h(14, "Abdullah ibn Umar", "Jami at-Tirmidhi", "Allah accepts the repentance of a servant until the final moment of death."),
        h(15, "Abu Dharr", "Sahih Muslim", "Do not belittle any good deed, even meeting your brother with a cheerful face."),
        h(16, "Abu Hurairah", "Sahih al-Bukhari and Sahih Muslim", "Whoever believes in Allah and the Last Day, let him speak good or keep silent."),
        h(17, "Anas ibn Malik", "Sahih al-Bukhari and Sahih Muslim", "None of you truly believes until he loves for his brother what he loves for himself."),
        h(18, "Abu Hurairah", "Sahih al-Bukhari and Sahih Muslim", "The strong person is not the one who overcomes others by force. The strong person is the one who controls himself when angry."),
        h(19, "Abu Hurairah", "Sahih Muslim", "Whoever follows a path to seek knowledge, Allah makes easy for him a path to Paradise."),
        h(20, "Samurah ibn Jundub", "Sahih Muslim", "The most beloved words to Allah are four: SubhanAllah, Alhamdulillah, La ilaha illallah and Allahu Akbar. It does not matter which you begin with."),
        h(21, "Abu Musa al-Ashari", "Sahih al-Bukhari", "The example of the one who remembers his Lord and the one who does not is like the living and the dead."),
        h(22, "Anas ibn Malik", "Sahih al-Bukhari and Sahih Muslim", "Patience is at the first strike of a calamity."),
        h(23, "Abu Hurairah", "Sahih al-Bukhari and Sahih Muslim", "No fatigue, illness, worry, sadness, harm or distress befalls a Muslim, even the prick of a thorn, except that Allah expiates some of his sins because of it."),
        h(24, "Abu Musa al-Ashari", "Sahih al-Bukhari and Sahih Muslim", "Whoever prays the two cool prayers, Fajr and Asr, will enter Paradise."),
        h(25, "Umm Habibah", "Sahih Muslim", "Whoever prays twelve rak'ahs of voluntary prayer in a day and night, a house will be built for him in Paradise."),
        h(26, "An-Nu'man ibn Bashir", "Sunan Abi Dawud and Jami at-Tirmidhi", "Supplication is worship."),
        h(27, "Uthman ibn Affan", "Sahih Muslim", "Whoever prays Isha in congregation, it is as if he stood in prayer for half the night. Whoever also prays Fajr in congregation, it is as if he prayed the whole night."),
        h(28, "Abu Hurairah", "Sahih Muslim", "The closest a servant is to his Lord is when he is in prostration, so make plenty of supplication in it."),
        h(29, "Abu Hurairah", "Sahih Muslim", "Allah does not look at your appearance or your wealth, but He looks at your hearts and your deeds."),
        h(30, "Anas ibn Malik", "Sahih al-Bukhari and Sahih Muslim", "Make things easy and do not make them difficult. Give good news and do not drive people away."),
        h(31, "Abu Dharr", "Jami at-Tirmidhi", "Your smile in the face of your brother is charity."),
        h(32, "Abu Umamah", "Sunan al-Kubra of an-Nasa'i", "Whoever recites Ayat al-Kursi after every obligatory prayer, nothing stands between him and Paradise except death.")
    )

    private val versesById = verses.associateBy { it.id }
    private val hadithById = hadith.associateBy { it.id }

    fun find(id: String): InspirationItem? =
        versesById[id]?.let { InspirationItem.Verse(it) } ?: hadithById[id]?.let { InspirationItem.Saying(it) }
}
