package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.AiCareRepository
import com.example.data.PinkoraRepository
import com.example.domain.AgeGroup
import com.example.domain.EducationalArticle
import com.example.domain.UiState
import com.example.firebase.authStateFlow
import com.example.firebase.signOut
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.security.SecurityManager
import com.example.ui.screens.AboutPinkoraSubScreen
import com.example.ui.screens.AdminPanelSubScreen
import com.example.ui.screens.ArticleDetailDialog
import com.example.ui.screens.AuthWelcomeScreen
import com.example.ui.screens.CareAssistantScreen
import com.example.ui.screens.CareHubScreen
import com.example.ui.screens.CycleHubScreen
import com.example.ui.screens.HelpSupportSubScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.LearnHubScreen
import com.example.ui.screens.NotificationsCenterSubScreen
import com.example.ui.screens.OnboardingFlowScreen
import com.example.ui.screens.PrivacySecuritySubScreen
import com.example.ui.screens.PrivateDiaryScreen
import com.example.ui.screens.ProfileHubScreen
import com.example.ui.screens.ProfileSubPage
import com.example.ui.screens.SettingsSubScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.PinkLight
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkoraTheme
import com.example.ui.theme.PureWhite
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BottomTab { HOME, CYCLE, CARE, LEARN, PROFILE }
enum class CareOverlay { NONE, DIARY, AI_ASSISTANT }

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val securityManager = remember { SecurityManager(context.applicationContext) }
    val preAuthLanguage by securityManager.languageFlow.collectAsStateWithLifecycle(initialValue = AppLanguage.EN)
    val scope = rememberCoroutineScope()
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(800)
        showSplash = false
    }

    if (showSplash) {
        PinkoraTheme(language = preAuthLanguage) {
            SplashScreen(language = preAuthLanguage)
        }
        return
    }

    val user = currentUser
    if (user == null) {
        PinkoraTheme(language = preAuthLanguage) {
            AuthWelcomeScreen(
                language = preAuthLanguage,
                onLanguageChange = { lang -> scope.launch { securityManager.setLanguage(lang) } },
                onAuthSuccess = {},
            )
        }
    } else {
        key(user.uid) {
            val viewModel: PinkoraViewModel = viewModel(
                key = user.uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY])
                        PinkoraViewModel(
                            repository = PinkoraRepository(app.applicationContext),
                            aiRepository = AiCareRepository(),
                            securityManager = SecurityManager(app.applicationContext),
                            appContext = app.applicationContext,
                            currentUserId = user.uid,
                            userEmail = user.email,
                            displayName = user.displayName,
                        )
                    }
                },
            )
            AuthenticatedPinkoraShell(
                viewModel = viewModel,
                securityManager = securityManager,
                onSignedOut = {},
            )
        }
    }
}

@Composable
private fun AuthenticatedPinkoraShell(
    viewModel: PinkoraViewModel,
    securityManager: SecurityManager,
    onSignedOut: () -> Unit,
) {
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()

    val language by viewModel.language.collectAsStateWithLifecycle()
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val periodsState by viewModel.periodsState.collectAsStateWithLifecycle()
    val symptomsState by viewModel.symptomsState.collectAsStateWithLifecycle()
    val moodsState by viewModel.moodsState.collectAsStateWithLifecycle()
    val diaryState by viewModel.diaryState.collectAsStateWithLifecycle()
    val waterState by viewModel.waterState.collectAsStateWithLifecycle()
    val remindersState by viewModel.remindersState.collectAsStateWithLifecycle()
    val articles by viewModel.articlesState.collectAsStateWithLifecycle()
    val tips by viewModel.tipsState.collectAsStateWithLifecycle()
    val faqs by viewModel.faqsState.collectAsStateWithLifecycle()
    val supportState by viewModel.supportRequestsState.collectAsStateWithLifecycle()
    val adminSupportState by viewModel.adminSupportRequestsState.collectAsStateWithLifecycle()
    val adminLogs by viewModel.adminLogsState.collectAsStateWithLifecycle()
    val cycleSummary by viewModel.cycleSummary.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val hasPin by viewModel.hasPin.collectAsStateWithLifecycle()
    val diaryUnlocked by viewModel.diaryUnlocked.collectAsStateWithLifecycle()
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val aiStreamState by viewModel.aiStreamState.collectAsStateWithLifecycle()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    PinkoraTheme(language = language) {
        when (val pState = profileState) {
            is UiState.Loading -> {
                SplashScreen(language = language)
            }
            is UiState.Error -> {
                OnboardingFlowScreen(
                    initialName = viewModel.displayName ?: "",
                    language = language,
                    onLanguageChange = viewModel::switchLanguage,
                    onComplete = viewModel::completeOnboarding,
                )
            }
            is UiState.Success -> {
                val profile = pState.data
                if (profile == null || !profile.onboardingCompleted) {
                    OnboardingFlowScreen(
                        initialName = profile?.preferredName ?: (viewModel.displayName ?: ""),
                        language = language,
                        onLanguageChange = viewModel::switchLanguage,
                        onComplete = viewModel::completeOnboarding,
                    )
                } else {
                    val strings = AppStrings.get(language)
                    var activeTab by remember { mutableStateOf(BottomTab.HOME) }
                    var cycleSubTab by remember { mutableIntStateOf(0) }
                    var careSubTab by remember { mutableIntStateOf(0) }
                    var careOverlay by remember { mutableStateOf(CareOverlay.NONE) }
                    var profileSubPage by remember { mutableStateOf(ProfileSubPage.MENU) }
                    var dialogArticle by remember { mutableStateOf<EducationalArticle?>(null) }

                    val today = LocalDate.now().toEpochDay()
                    val waterLogs = (waterState as? UiState.Success)?.data ?: emptyList()
                    val todayWater = waterLogs.firstOrNull { it.dateEpochDay == today }
                    val waterGlassesToday = (todayWater?.glassesCount ?: 0L).toInt()
                    val waterTarget = (todayWater?.targetGlasses ?: 8L).toInt()

                    // Handle system Back button for nested sub-screens
                    BackHandler(enabled = careOverlay != CareOverlay.NONE || profileSubPage != ProfileSubPage.MENU || activeTab != BottomTab.HOME) {
                        when {
                            careOverlay != CareOverlay.NONE -> careOverlay = CareOverlay.NONE
                            activeTab == BottomTab.PROFILE && profileSubPage != ProfileSubPage.MENU -> profileSubPage = ProfileSubPage.MENU
                            else -> activeTab = BottomTab.HOME
                        }
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            NavigationBar(
                                containerColor = PureWhite,
                                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                            ) {
                                val navItems = listOf(
                                    Triple(BottomTab.HOME, strings.navHome, Pair(Icons.Filled.Home, Icons.Outlined.Home)),
                                    Triple(BottomTab.CYCLE, strings.navCycle, Pair(Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)),
                                    Triple(BottomTab.CARE, strings.navCare, Pair(Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)),
                                    Triple(BottomTab.LEARN, strings.navLearn, Pair(Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook)),
                                    Triple(BottomTab.PROFILE, strings.navProfile, Pair(Icons.Filled.Person, Icons.Outlined.Person)),
                                )
                                navItems.forEach { (tab, label, icons) ->
                                    val selected = activeTab == tab
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            activeTab = tab
                                            careOverlay = CareOverlay.NONE
                                            if (tab != BottomTab.PROFILE) profileSubPage = ProfileSubPage.MENU
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) icons.first else icons.second,
                                                contentDescription = label,
                                            )
                                        },
                                        label = { Text(label, maxLines = 1) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = PinkPrimary,
                                            selectedTextColor = PinkPrimary,
                                            indicatorColor = PinkLight,
                                        ),
                                        modifier = Modifier.testTag("bottom_nav_${tab.name.lowercase()}"),
                                    )
                                }
                            }
                        },
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                            if (careOverlay == CareOverlay.DIARY) {
                                PrivateDiaryScreen(
                                    diaryState = diaryState,
                                    hasPin = hasPin,
                                    isUnlocked = diaryUnlocked,
                                    biometricEnabled = biometricEnabled,
                                    securityManager = securityManager,
                                    language = language,
                                    onSetPin = viewModel::setPin,
                                    onVerifyPin = viewModel::unlockDiaryWithPin,
                                    onBiometricSuccess = viewModel::unlockDiaryWithBiometric,
                                    onLockNow = viewModel::lockDiaryNow,
                                    onSaveEntry = viewModel::saveDiary,
                                    onDeleteEntry = viewModel::deleteDiary,
                                    onBack = { careOverlay = CareOverlay.NONE },
                                )
                            } else if (careOverlay == CareOverlay.AI_ASSISTANT) {
                                CareAssistantScreen(
                                    profile = profile,
                                    chatMessages = chatMessages,
                                    streamState = aiStreamState,
                                    faqs = faqs,
                                    language = language,
                                    onGrantAiConsent = {
                                        viewModel.updateProfileSettings(
                                            preferredName = profile.preferredName,
                                            ageGroup = profile.resolvedAgeGroup(),
                                            aiConsent = true,
                                            cycleLength = profile.averageCycleLength.toInt(),
                                            periodLength = profile.averagePeriodLength.toInt(),
                                            ovulationEnabled = profile.ovulationTrackingEnabled,
                                        )
                                    },
                                    onSendMessage = viewModel::sendCareMessage,
                                    onBack = { careOverlay = CareOverlay.NONE },
                                )
                            } else {
                                when (activeTab) {
                                    BottomTab.HOME -> HomeDashboardScreen(
                                        profile = profile,
                                        cycleSummary = cycleSummary,
                                        waterGlassesToday = waterGlassesToday,
                                        waterTarget = waterTarget,
                                        tips = tips,
                                        articles = articles,
                                        language = language,
                                        onLanguageChange = viewModel::switchLanguage,
                                        onIncrementWater = { viewModel.updateWater(1, waterTarget) },
                                        onOpenCalendar = {
                                            cycleSubTab = 0
                                            activeTab = BottomTab.CYCLE
                                        },
                                        onOpenDailyCheckin = {
                                            careSubTab = 0
                                            activeTab = BottomTab.CARE
                                        },
                                        onOpenSymptoms = {
                                            careSubTab = 1
                                            activeTab = BottomTab.CARE
                                        },
                                        onOpenMood = {
                                            careSubTab = 2
                                            activeTab = BottomTab.CARE
                                        },
                                        onOpenCareAssistant = { careOverlay = CareOverlay.AI_ASSISTANT },
                                        onOpenDiary = { careOverlay = CareOverlay.DIARY },
                                        onOpenNotifications = {
                                            activeTab = BottomTab.PROFILE
                                            profileSubPage = ProfileSubPage.NOTIFICATIONS
                                        },
                                        onOpenArticle = { dialogArticle = it },
                                    )

                                    BottomTab.CYCLE -> CycleHubScreen(
                                        initialSubTab = cycleSubTab,
                                        profile = profile,
                                        periodsState = periodsState,
                                        cycleSummary = cycleSummary,
                                        language = language,
                                        onSavePeriod = viewModel::savePeriod,
                                        onDeletePeriod = viewModel::deletePeriod,
                                    )

                                    BottomTab.CARE -> CareHubScreen(
                                        initialSubTab = careSubTab,
                                        symptomsState = symptomsState,
                                        moodsState = moodsState,
                                        language = language,
                                        onLogSymptoms = viewModel::logSymptoms,
                                        onDeleteSymptom = viewModel::deleteSymptom,
                                        onLogMood = viewModel::logMood,
                                        onDeleteMood = viewModel::deleteMood,
                                        onOpenDiary = { careOverlay = CareOverlay.DIARY },
                                        onOpenAiAssistant = { careOverlay = CareOverlay.AI_ASSISTANT },
                                    )

                                    BottomTab.LEARN -> LearnHubScreen(
                                        articles = articles,
                                        profile = profile,
                                        waterGlassesToday = waterGlassesToday,
                                        waterTarget = waterTarget,
                                        language = language,
                                        onUpdateWater = { delta -> viewModel.updateWater(delta, waterTarget) },
                                        onUpdateWaterTarget = viewModel::updateWaterTarget,
                                        onOpenNotifications = {
                                            activeTab = BottomTab.PROFILE
                                            profileSubPage = ProfileSubPage.NOTIFICATIONS
                                        },
                                    )

                                    BottomTab.PROFILE -> when (profileSubPage) {
                                        ProfileSubPage.MENU -> ProfileHubScreen(
                                            profile = profile,
                                            userEmail = viewModel.userEmail,
                                            isAdmin = isAdmin,
                                            language = language,
                                            onLanguageChange = viewModel::switchLanguage,
                                            onNavigateSubPage = { profileSubPage = it },
                                            onDeleteAccount = {
                                                viewModel.deleteAccountData {
                                                    signOut(credentialManager, scope, onSignedOut)
                                                }
                                            },
                                            onLogout = { signOut(credentialManager, scope, onSignedOut) },
                                        )
                                        ProfileSubPage.SETTINGS -> SettingsSubScreen(
                                            profile = profile,
                                            language = language,
                                            onLanguageChange = viewModel::switchLanguage,
                                            onSaveSettings = viewModel::updateProfileSettings,
                                            onBack = { profileSubPage = ProfileSubPage.MENU },
                                        )
                                        ProfileSubPage.NOTIFICATIONS -> NotificationsCenterSubScreen(
                                            remindersState = remindersState,
                                            language = language,
                                            onSaveReminder = viewModel::saveReminderSetting,
                                            onBack = { profileSubPage = ProfileSubPage.MENU },
                                        )
                                        ProfileSubPage.PRIVACY -> PrivacySecuritySubScreen(
                                            profile = profile,
                                            hasPin = hasPin,
                                            biometricEnabled = biometricEnabled,
                                            securityManager = securityManager,
                                            language = language,
                                            onSetPin = viewModel::setPin,
                                            onRemovePin = viewModel::removePin,
                                            onToggleBiometric = viewModel::setBiometricEnabled,
                                            onToggleAiConsent = { enabled ->
                                                viewModel.updateProfileSettings(
                                                    preferredName = profile.preferredName,
                                                    ageGroup = profile.resolvedAgeGroup(),
                                                    aiConsent = enabled,
                                                    cycleLength = profile.averageCycleLength.toInt(),
                                                    periodLength = profile.averagePeriodLength.toInt(),
                                                    ovulationEnabled = profile.ovulationTrackingEnabled,
                                                )
                                            },
                                            onExportJson = viewModel::exportDataJson,
                                            onBack = { profileSubPage = ProfileSubPage.MENU },
                                        )
                                        ProfileSubPage.ABOUT -> AboutPinkoraSubScreen(
                                            language = language,
                                            onBack = { profileSubPage = ProfileSubPage.MENU },
                                        )
                                        ProfileSubPage.SUPPORT -> HelpSupportSubScreen(
                                            supportState = supportState,
                                            faqs = faqs,
                                            language = language,
                                            onSubmitTicket = viewModel::submitSupportTicket,
                                            onBack = { profileSubPage = ProfileSubPage.MENU },
                                        )
                                        ProfileSubPage.ADMIN -> AdminPanelSubScreen(
                                            articles = articles,
                                            tips = tips,
                                            faqs = faqs,
                                            supportState = adminSupportState,
                                            adminLogs = adminLogs,
                                            language = language,
                                            onCreateArticle = viewModel::adminPublishArticle,
                                            onCreateTip = viewModel::adminPublishTip,
                                            onCreateFaq = viewModel::adminPublishFaq,
                                            onReplyTicket = viewModel::adminReplyTicket,
                                            onBack = { profileSubPage = ProfileSubPage.MENU },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    dialogArticle?.let { art ->
                        ArticleDetailDialog(
                            article = art,
                            language = language,
                            onDismiss = { dialogArticle = null },
                        )
                    }
                }
            }
        }
    }
}
