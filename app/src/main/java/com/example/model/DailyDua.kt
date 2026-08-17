package com.example.model

data class DailyDua(
    val id: Int,
    val title: String,
    val category: String,
    val arabic: String,
    val transliteration: String,
    val turkishMeaning: String,
    val benefitOrSource: String
)

data class EsmaulHusna(
    val number: Int,
    val nameArabic: String,
    val nameTr: String,
    val meaning: String,
    val zikrCount: Int
)

object DuaDataRepository {
    val duas = listOf(
        DailyDua(
            1,
            "Sabah Duası (Hamd ve Şükür)",
            "Sabah & Akşam",
            "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            "Asbahnâ ve asbahal-mülkü lillâhi vel-hamdü lillâh, lâ ilâhe illallâhu vahdehû lâ şerîke leh.",
            "Biz de sabaha ulaştık, mülk de Allah'ın olarak sabaha erdi. Hamd Allah'a mahsustur. O'ndan başka hiçbir ilah yoktur, O tektir ve ortağı yoktur.",
            "Müslim, Zikir, 75"
        ),
        DailyDua(
            2,
            "Akşam Duası (Şerlerden Sığınma)",
            "Sabah & Akşam",
            "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ وَالْحَمْدُ لِلَّهِ",
            "Emseynâ ve emsel-mülkü lillâhi vel-hamdü lillâh.",
            "Akşama erdik, mülk de Allah'ın olarak akşama ulaştı. Hamd yalnızca Allah'a aittir.",
            "Tirmizî, Daavât, 13"
        ),
        DailyDua(
            3,
            "Seyyidül İstiğfar (Tevbe Duası)",
            "Tevbe & Bağışlanma",
            "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            "Allâhümme ente Rabbî lâ ilâhe illâ ente halaktenî ve ene 'abdüke ve ene 'alâ 'ahdike ve va'dike mesteta'tü, e'ûzü bike min şerri mâ sana'tü, ebû'u leke bini'metike 'aleyye ve ebû'u bizenbî fağfirlî feinnehu lâ yağfiruz-zünûbe illâ ente.",
            "Allah'ım! Sen benim Rabbimsin. Senden başka ilah yoktur. Beni sen yarattın, ben senin kulunum. Gücüm yettiğince sana verdiğim ahde ve vaade sadığım. İşlediğim kusurların şerrinden sana sığınırım. Bana verdiğin nimetleri itiraf eder, günahımı da ikrar ederim. Beni bağışla; çünkü günahları ancak Sen bağışlarsın.",
            "Buhârî, Deavât, 2 (Cennet müjdesi içeren istiğfar)"
        ),
        DailyDua(
            4,
            "Namaz Sonrası Tesbihat Duası",
            "Namaz Sonrası",
            "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالإِكْرَامِ",
            "Allâhümme ente's-selâmü ve minke's-selâm, tebârakte yâ ze'l-celâli ve'l-ikrâm.",
            "Allah'ım! Selâm sensin ve selâmet sendendir. Ey celâl ve ikram sahibi! Sen yücelerden yücesin.",
            "Müslim, Mesâcid, 135"
        ),
        DailyDua(
            5,
            "Ayetel Kürsi",
            "Kuran'dan Dualar",
            "اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ",
            "Allâhü lâ ilâhe illâ hüvel hayyül kayyûm...",
            "Allah, O'ndan başka ilah olmayan, diri (Hayy) ve her şeyi ayakta tutan (Kayyûm)dur. O'nu ne bir uyuklama ne de bir uyku tutar...",
            "Bakara Suresi, 255"
        ),
        DailyDua(
            6,
            "Rabbenâ Âtinâ (Dünya ve Ahiret İyiliği)",
            "Kuran'dan Dualar",
            "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            "Rabbenâ âtinâ fid-dünyâ haseneten ve fil-âhireti haseneten ve kınâ 'azâben-nâr.",
            "Ey Rabbimiz! Bize dünyada da iyilik ve güzellik ver, ahirette de iyilik ve güzellik ver ve bizi cehennem azabından koru.",
            "Bakara Suresi, 201"
        ),
        DailyDua(
            7,
            "Evden Çıkarken Okunacak Dua",
            "Günlük Yaşam",
            "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            "Bismillâhi tevekkeltü 'alallâhi ve lâ havle ve lâ kuvvete illâ billâh.",
            "Allah'ın adıyla. Allah'a tevekkül ettim. Güç ve kuvvet ancak Allah'ın yardımıyladır.",
            "Ebû Dâvûd, Edeb, 103"
        ),
        DailyDua(
            8,
            "Sıkıntı ve Keder Anında Dua",
            "Şifa & Sıkıntı",
            "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
            "Lâ ilâhe illâ ente sübhâneke innî küntü minez-zâlimîn.",
            "Senden başka ilah yoktur. Seni tenzih ederim. Şüphesiz ben (nefsime) zulmedenlerden oldum.",
            "Enbiyâ Suresi, 87 (Yunus A.S. Duası)"
        ),
        DailyDua(
            9,
            "Yemek Duası (Sonrasında)",
            "Günlük Yaşam",
            "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
            "Elhamdü lillâhillezî et'amenâ ve sekânâ ve ce'alenâ müslimîn.",
            "Bizi yediren, içiren ve bizi müslümanlardan kılan Allah'a hamdolsun.",
            "Ebû Dâvûd, Et'ime, 52"
        ),
        DailyDua(
            10,
            "Hacet ve Dilek Duası",
            "Tevbe & Bağışlanma",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ مُوجِبَاتِ رَحْمَتِكَ، وَعَزَائِمَ مَغْفِرَتِكَ",
            "Allâhümme innî es'elüke mûcibâti rahmetike ve 'azâime mağfiratike...",
            "Allah'ım! Rahmetini gerektiren vesileleri ve kesin bağışlanmanı senden niyaz ederim.",
            "Tirmizî, Vitir, 17"
        )
    )

    val esmaulHusna = listOf(
        EsmaulHusna(1, "الرَّحْمَنُ", "Er-Rahmân", "Dünyada bütün mahlûkata merhamet eden, şefkat gösteren.", 298),
        EsmaulHusna(2, "الرَّحِيمُ", "Er-Rahîm", "Ahirette sadece mü'minlere sonsuz merhamet ve lütuf eden.", 258),
        EsmaulHusna(3, "الْمَلِكُ", "El-Melik", "Bütün kainatın mutlak sahibi ve hükümdarı.", 90),
        EsmaulHusna(4, "الْقُدُّوسُ", "El-Kuddûs", "Her türlü eksiklik ve ayıptan münezzeh ve pâk olan.", 170),
        EsmaulHusna(5, "السَّلَامُ", "Es-Selâm", "Kullarını her türlü tehlikelerden selâmete çıkaran.", 131),
        EsmaulHusna(6, "الْمُؤْمِنُ", "El-Mü'min", "Gönüllerde iman ışığı uyandıran, güven veren.", 136),
        EsmaulHusna(7, "الْمُهَيْمِنُ", "El-Müheymin", "Her şeyi gözetip koruyan ve yöneten.", 145),
        EsmaulHusna(8, "الْعَزِيزُ", "El-Azîz", "İzzet ve kudret sahibi, mağlup edilmesi imkânsız olan.", 94),
        EsmaulHusna(9, "الْجَبَّارُ", "El-Cebbâr", "Kırılanları onaran, dilediğini zorla da olsa yaptıran.", 206),
        EsmaulHusna(10, "الْمُتَكَبِّرُ", "El-Mütekebbir", "Büyüklükte eşi ve benzeri olmayan, azamet sahibi.", 662),
        EsmaulHusna(11, "الْخَالِقُ", "El-Hâlık", "Her şeyi yoktan var eden, yaratan.", 731),
        EsmaulHusna(12, "الْبَارِئُ", "El-Bâri", "Her şeyi kusursuz ve ahenk içinde yaratan.", 213),
        EsmaulHusna(13, "الْمُصَوِّرُ", "El-Musavvir", "Varlıklara en güzel suret ve şekli veren.", 336),
        EsmaulHusna(14, "الْغَفَّارُ", "El-Gaffâr", "Kullarının günahlarını tekrar tekrar bağışlayan.", 1281),
        EsmaulHusna(15, "الْقَهَّارُ", "El-Kahhâr", "Her şeye galip gelen ve mutlak hakim olan.", 306),
        EsmaulHusna(16, "الْوَهَّابُ", "El-Vehhâb", "Karşılıksız bol bol nimet ve hibe veren.", 14),
        EsmaulHusna(17, "الرَّزَّاقُ", "Er-Rezzâk", "Bütün canlıların rızkını veren ve temin eden.", 308),
        EsmaulHusna(18, "الْفَتَّاحُ", "El-Fettâh", "Bütün hayır kapılarını ve zorlukları açan.", 489),
        EsmaulHusna(19, "الْعَلِيمُ", "El-Alîm", "Gizli ve açık her şeyi hakkıyla bilen.", 150),
        EsmaulHusna(20, "الْحَكِيمُ", "El-Hakîm", "Her işi hikmetli, yerli yerinde olan.", 78),
        EsmaulHusna(21, "الْوَدُودُ", "El-Vedûd", "Kullarını çok seven ve sevilmeye en layık olan.", 20),
        EsmaulHusna(22, "النُّورُ", "En-Nûr", "Alemleri nurlandıran, doğru yolu aydınlatan.", 256)
    )

    val dailyAyahHadith = listOf(
        Pair(
            "“Namazı dosdoğru kılın, zekâtı verin ve rükû edenlerle birlikte rükû edin.”",
            "Bakara Suresi, 43. Ayet"
        ),
        Pair(
            "“Beni anın ki ben de sizi anayım. Bana şükredin, nankörlük etmeyin.”",
            "Bakara Suresi, 152. Ayet"
        ),
        Pair(
            "“Şüphesiz namaz, mü'minler üzerine vakitleri belirlenmiş bir farzdır.”",
            "Nisâ Suresi, 103. Ayet"
        ),
        Pair(
            "“İslam beş esas üzerine kurulmuştur: Kelime-i Şehadet getirmek, namaz kılmak, zekat vermek, hacca gitmek ve Ramazan orucunu tutmak.”",
            "Hadis-i Şerif (Buhârî & Müslim)"
        ),
        Pair(
            "“Kulun Rabbine en yakın olduğu an, secde anıdır. Öyleyse secdede çokça dua edin.”",
            "Hadis-i Şerif (Müslim)"
        ),
        Pair(
            "“Sizin en hayırlınız, Kur'an'ı öğrenen ve öğretendir.”",
            "Hadis-i Şerif (Buhârî)"
        )
    )
}
