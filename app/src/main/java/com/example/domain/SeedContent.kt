package com.example.domain

data class MythFactItem(
    val id: String,
    val mythEn: String,
    val mythHi: String,
    val factEn: String,
    val factHi: String,
)

data class NutritionGuideItem(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val foodsEn: String,
    val foodsHi: String,
    val benefitEn: String,
    val benefitHi: String,
)

object SeedContent {

    val defaultArticles: List<EducationalArticle> = listOf(
        EducationalArticle(
            id = "art_what_is_period",
            titleEn = "What Is Menstruation & Why Periods Happen",
            titleHi = "मासिक धर्म क्या है और पीरियड्स क्यों आते हैं",
            summaryEn = "A gentle, clear explanation of how the monthly cycle works.",
            summaryHi = "मासिक चक्र कैसे काम करता है, इसकी सरल और स्पष्ट जानकारी।",
            bodyEn = "Menstruation (a period) is the monthly shedding of the uterus lining when pregnancy does not occur. Guided by natural hormones, the lining builds up gently each month and leaves the body over 2 to 7 days. It is a normal, healthy sign of growing and body rhythm—never dirty or shameful.",
            bodyHi = "मासिक धर्म (पीरियड) हर महीने गर्भाशय की परत के स्वाभाविक रूप से बाहर निकलने की प्रक्रिया है। हार्मोन के प्राकृतिक बदलावों के कारण यह परत हर महीने बनती है और 2 से 7 दिनों तक हल्के या मध्यम रक्तस्राव के रूप में निकलती है। यह शरीर के स्वस्थ होने का सामान्य संकेत है—इसमें कोई शर्म की बात नहीं है।",
            category = "hygiene",
            ageAppropriateFor = "all",
            reviewStatus = "draft_pending_doctor_review",
        ),
        EducationalArticle(
            id = "art_first_period",
            titleEn = "First Period Guide: What to Expect",
            titleHi = "पहला पीरियड गाइड: क्या उम्मीद करें",
            summaryEn = "Everything teens 13–17 need for a calm, prepared first period.",
            summaryHi = "13–17 वर्ष की किशोरियों के लिए पहले पीरियड की शांत और सुरक्षित तैयारी।",
            bodyEn = "Your first period can start with light brown or pink spotting before becoming red. Keep a small pouch in your school bag with 2 clean pads, spare underwear, and tissues. Irregular gaps between early cycles are very common in the first 1–2 years while hormones settle.",
            bodyHi = "पहला पीरियड हल्के भूरे या गुलाबी धब्बों से शुरू हो सकता है। अपने स्कूल बैग में एक छोटा पाउच रखें जिसमें 2 साफ पैड, अतिरिक्त अंडरवियर और टिश्यू हों। शुरुआती 1–2 वर्षों में पीरियड्स की तारीखों में अंतर होना बिल्कुल सामान्य है।",
            category = "first_period",
            ageAppropriateFor = "teen_13_17",
            reviewStatus = "draft_pending_doctor_review",
        ),
        EducationalArticle(
            id = "art_pads_cups_hygiene",
            titleEn = "Pads, Menstrual Cups & Safe Disposal",
            titleHi = "पैड, मेंस्ट्रुअल कप और सुरक्षित निपटान",
            summaryEn = "How often to change products and keep skin irritation-free.",
            summaryHi = "पैड या कप कब बदलें और संक्रमण से कैसे बचें।",
            bodyEn = "Change sanitary pads every 4 to 6 hours, even on lighter days, to prevent rashes and infection. Wash hands with soap before and after changing. If using a medical-grade silicone menstrual cup (with comfort and readiness), sanitize it by boiling before each cycle and rinse every 8–10 hours. Always wrap used pads in paper and place them in a covered dustbin—never flush pads down toilets.",
            bodyHi = "रैशेज और संक्रमण से बचने के लिए हल्के प्रवाह वाले दिनों में भी हर 4 से 6 घंटे में सैनिटरी पैड बदलें। बदलने से पहले और बाद में साबुन से हाथ धोएं। यदि मेंस्ट्रुअल कप उपयोग कर रहे हैं तो हर चक्र से पहले उबालकर साफ करें। इस्तेमाल किए गए पैड को कागज में लपेटकर ढक्कन वाले डस्टबिन में डालें—कभी भी टॉयलेट में न बहाएं।",
            category = "hygiene",
            ageAppropriateFor = "all",
            reviewStatus = "draft_pending_doctor_review",
        ),
        EducationalArticle(
            id = "art_puberty_irregular",
            titleEn = "Puberty & Understanding Irregular Cycles",
            titleHi = "किशोरावस्था और अनियमित चक्र को समझना",
            summaryEn = "Why cycles vary with stress, sleep, exams, or puberty.",
            summaryHi = "तनाव, नींद, परीक्षा या हार्मोनल बदलाव से चक्र में अंतर क्यों आता है।",
            bodyEn = "A typical cycle ranges from 21 to 35 days (and 21–45 days in teens). Exam stress, poor sleep, sudden weight shifts, or travel can delay ovulation and shift your period. Track your dates calmly without panic; if periods stay absent for 3+ months or arrive more often than every 21 days, speak with a gynecologist.",
            bodyHi = "सामान्य चक्र 21 से 35 दिनों का (किशोरियों में 21–45 दिन) होता है। परीक्षा का तनाव, कम नींद, यात्रा या खान-पान में बदलाव पीरियड को आगे-पीछे कर सकता है। शांत रहकर तारीखें दर्ज करें; यदि 3 महीने तक पीरियड न आएं तो डॉक्टर से सलाह लें।",
            category = "first_period",
            ageAppropriateFor = "all",
            reviewStatus = "draft_pending_doctor_review",
        ),
        EducationalArticle(
            id = "art_when_see_doctor",
            titleEn = "When to Seek Medical Help",
            titleHi = "डॉक्टर को कब दिखाएं",
            summaryEn = "Important red-flag symptoms you should never ignore.",
            summaryHi = "महत्वपूर्ण चेतावनी संकेत जिन्हें कभी नजरअंदाज न करें।",
            bodyEn = "Please consult a qualified doctor promptly if you soak through a pad or tampon every hour for 2+ hours, feel faint or dizzy, experience severe pain (8+/10) that stops daily life despite rest, pass blood clots larger than a coin repeatedly, or have fever with unusual discharge.",
            bodyHi = "यदि हर घंटे 2 घंटे तक लगातार पैड भीग जाए, चक्कर या बेहोशी आए, आराम के बाद भी असहनीय दर्द (8+/10) हो, बड़े थक्के लगातार आएं या बुखार के साथ असामान्य स्राव हो, तो तुरंत योग्य डॉक्टर से मिलें।",
            category = "when_to_see_doctor",
            ageAppropriateFor = "all",
            reviewStatus = "draft_pending_doctor_review",
        ),
        EducationalArticle(
            id = "art_self_care_comfort",
            titleEn = "Gentle Pain Self-Care & Rest",
            titleHi = "दर्द में आराम और स्व-देखभाल के उपाय",
            summaryEn = "Warmth, hydration, light stretching, and rest tips.",
            summaryHi = "गर्म सिकाई, पानी, हल्की स्ट्रेचिंग और आराम के तरीके।",
            bodyEn = "Place a warm water bag on your lower belly or lower back for 15–20 minutes to relax uterine muscles. Sip warm water or ginger-tulsi tea, eat light warm meals, and avoid skipping rest. These comfort tips support wellness and never replace medical advice when pain is severe.",
            bodyHi = "मांसपेशियों को आराम देने के लिए पेट के निचले हिस्से या कमर पर 15–20 मिनट गुनगुने पानी की बोतल से सिकाई करें। गुनगुना पानी या अदरक-तुलसी की चाय पिएं, हल्का ताजा भोजन लें और पर्याप्त आराम करें। तेज़ दर्द में डॉक्टर की सलाह जरूरी है।",
            category = "self_care",
            ageAppropriateFor = "all",
            reviewStatus = "draft_pending_doctor_review",
        ),
    )

    val mythsVsFacts: List<MythFactItem> = listOf(
        MythFactItem(
            id = "mf1",
            mythEn = "Myth: Period blood is impure or dirty.",
            mythHi = "मिथक: पीरियड का खून अशुद्ध या गंदा होता है।",
            factEn = "Fact: It is simply normal blood and healthy uterine tissue—completely natural and not impure.",
            factHi = "तथ्य: यह सामान्य रक्त और गर्भाशय की परत का प्राकृतिक हिस्सा है—यह बिल्कुल भी अशुद्ध नहीं है।",
        ),
        MythFactItem(
            id = "mf2",
            mythEn = "Myth: You should not wash your hair or bathe during periods.",
            mythHi = "मिथक: पीरियड्स के दौरान नहाना या बाल धोना नहीं चाहिए।",
            factEn = "Fact: Warm showers actually improve hygiene, lift mood, and ease muscle cramps.",
            factHi = "तथ्य: गुनगुने पानी से नहाना स्वच्छता बनाए रखता है, मूड बेहतर करता है और ऐंठन कम करता है।",
        ),
        MythFactItem(
            id = "mf3",
            mythEn = "Myth: Exercise is dangerous during menstruation.",
            mythHi = "मिथक: मासिक धर्म के दौरान व्यायाम करना नुकसानदायक है।",
            factEn = "Fact: Gentle walking, stretching, and restorative yoga often reduce cramps and fatigue.",
            factHi = "तथ्य: हल्की सैर, स्ट्रेचिंग और सरल योग अक्सर दर्द और थकान को कम करते हैं।",
        ),
        MythFactItem(
            id = "mf4",
            mythEn = "Myth: Eating sour foods stops or ruins periods.",
            mythHi = "मिथक: खट्टा खाने से पीरियड रुक जाता है।",
            factEn = "Fact: Foods like lemon or curd do not stop menstruation; vitamin C even helps iron absorption.",
            factHi = "तथ्य: नींबू या दही से पीरियड नहीं रुकता; बल्कि विटामिन-C शरीर में आयरन सोखने में मदद करता है।",
        ),
    )

    val nutritionGuides: List<NutritionGuideItem> = listOf(
        NutritionGuideItem(
            id = "nut_iron",
            titleEn = "Iron-Rich Everyday Foods",
            titleHi = "आयरन से भरपूर दैनिक आहार",
            foodsEn = "Spinach (palak), beetroot, pomegranate (anar), dates (khajoor), roasted chana, jaggery (gud), lentils (dal).",
            foodsHi = "पालक, चुकंदर, अनार, खजूर, भुना चना, गुड़ और दालें।",
            benefitEn = "Helps replenish iron lost during bleeding and supports steady energy. Pair with lemon/amla for better absorption.",
            benefitHi = "रक्तस्राव के दौरान कम हुए आयरन की भरपाई और ऊर्जा बनाए रखने में सहायक। बेहतर अवशोषण के लिए नींबू या आंवला साथ लें।",
        ),
        NutritionGuideItem(
            id = "nut_indian_meals",
            titleEn = "Warm Comforting Indian Meal Ideas",
            titleHi = "पीरियड्स के लिए हल्का और पौष्टिक भारतीय भोजन",
            foodsEn = "Moong dal khichdi with ghee, ragi/bajra roti with palak paneer, vegetable poha with peanuts, soaked almonds & walnuts.",
            foodsHi = "घी के साथ मूंग दाल खिचड़ी, रागी/बाजरा रोटी व पालक पनीर, मूंगफली वाला वेज पोहा, भीगे बादाम और अखरोट।",
            benefitEn = "Easy to digest during bloating while delivering protein, magnesium, and slow-release energy.",
            benefitHi = "पेट फूलने के समय पचने में आसान और प्रोटीन व मैग्नीशियम से भरपूर।",
        ),
        NutritionGuideItem(
            id = "nut_hydration",
            titleEn = "Hydration & Soothing Drinks",
            titleHi = "हाइड्रेशन और आरामदायक पेय",
            foodsEn = "Plain warm water, coconut water, buttermilk (chaas), ginger-cinnamon herbal infusion, fresh fruit slices.",
            foodsHi = "गुनगुना पानी, नारियल पानी, छाछ, अदरक-दालचीनी का हर्बल पेय और ताजे फल।",
            benefitEn = "Reduces water-retention bloating, eases headaches, and keeps digestion comfortable.",
            benefitHi = "सिरदर्द और पेट फूलने की समस्या कम करने और शरीर को हाइड्रेट रखने में मदद करता है।",
        ),
    )

    val defaultTips: List<WellnessTip> = listOf(
        WellnessTip(
            id = "tip_1",
            textEn = "Sip a warm glass of water and stretch your shoulders gently—hydration eases fatigue.",
            textHi = "एक गिलास गुनगुना पानी पिएं और कंधों को हल्के से स्ट्रेच करें—हाइड्रेशन थकान कम करता है।",
            category = "hydration",
        ),
        WellnessTip(
            id = "tip_2",
            textEn = "Be gentle with yourself today. Rest is productive, especially during heavy flow days.",
            textHi = "आज अपने प्रति नरम रहें। आराम भी जरूरी देखभाल है, खासकर भारी प्रवाह वाले दिनों में।",
            category = "mood",
        ),
        WellnessTip(
            id = "tip_3",
            textEn = "Pair iron-rich palak or chana with a squeeze of lemon to boost absorption naturally.",
            textHi = "आयरन से भरपूर पालक या चने के साथ थोड़ा नींबू मिलाएं ताकि शरीर आयरन बेहतर सोख सके।",
            category = "nutrition",
        ),
    )

    val defaultFaqs: List<FaqItem> = listOf(
        FaqItem(
            id = "faq_1",
            questionEn = "How often should I change a sanitary pad?",
            questionHi = "सैनिटरी पैड कितनी देर में बदलना चाहिए?",
            answerEn = "Change your pad every 4 to 6 hours (or sooner if soaked) to stay fresh and prevent skin irritation or infection.",
            answerHi = "ताजगी बनाए रखने और संक्रमण से बचने के लिए हर 4 से 6 घंटे में (या भरने पर जल्दी) पैड बदलें।",
            category = "hygiene",
        ),
        FaqItem(
            id = "faq_2",
            questionEn = "Why is my cycle date shifting by a few days?",
            questionHi = "मेरी पीरियड की तारीख कुछ दिन आगे-पीछे क्यों हो जाती है?",
            answerEn = "Small shifts of 2–5 days are very common due to sleep changes, exam or work stress, travel, or teen hormone adjustments. Predictions are estimates only.",
            answerHi = "नींद, तनाव, यात्रा या किशोरावस्था में हार्मोन बदलाव के कारण 2–5 दिन का अंतर आम है। सभी अनुमान केवल मार्गदर्शन के लिए हैं।",
            category = "cycle",
        ),
        FaqItem(
            id = "faq_3",
            questionEn = "What helps mild period cramps naturally?",
            questionHi = "हल्के पीरियड क्रैम्प्स में प्राकृतिक आराम कैसे पाएं?",
            answerEn = "Try a warm water compress on your lower belly for 15–20 minutes, gentle breathing, warm water, and light rest. If pain is severe (8+/10) or accompanied by fainting, seek medical help.",
            answerHi = "पेट के निचले हिस्से पर 15–20 मिनट गुनगुनी सिकाई करें, शांत श्वास लें और आराम करें। तेज़ दर्द (8+/10) या बेहोशी होने पर तुरंत डॉक्टर से मिलें।",
            category = "pain",
        ),
        FaqItem(
            id = "faq_4",
            questionEn = "Who can read my Private Diary or Period logs?",
            questionHi = "मेरी निजी डायरी या पीरियड रिकॉर्ड कौन देख सकता है?",
            answerEn = "Only you. Your diary, period logs, symptoms, and mood entries are isolated to your account and blocked from admins.",
            answerHi = "केवल आप। आपकी डायरी, पीरियड, लक्षण और मूड रिकॉर्ड केवल आपके खाते तक सीमित हैं और एडमिन उन्हें कभी नहीं देख सकते।",
            category = "privacy",
        ),
    )

    val positiveMessagesEn = listOf(
        "Your body is resilient and deserves kindness today.",
        "Slow down, breathe deeply, and take one step at a time.",
        "Listening to your energy is a form of strength.",
        "You are cared for—hydrate, rest, and honour your comfort.",
    )

    val positiveMessagesHi = listOf(
        "आपका शरीर संवेदनशील और मजबूत है—आज खुद के प्रति दयालु रहें।",
        "धीमे चलें, गहरी सांस लें और एक समय में एक कदम बढ़ाएं।",
        "अपनी ऊर्जा को सुनना और आराम करना भी एक शक्ति है।",
        "अपनी देखभाल करें—पानी पिएं, आराम करें और सहज रहें।",
    )
}
