package com.example.sample2.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sample2.model.JournalJsonStorage
import com.example.sample2.ui.journal.DeleteMessageConfirmDialog
import com.example.sample2.ui.journal.JournalBottomModeBar
import com.example.sample2.ui.journal.JournalCompactMetaRow
import com.example.sample2.ui.journal.JournalScreenMode
import com.example.sample2.ui.journal.RestoreConfirmDialog
import com.example.sample2.ui.journal.buildJournalDateLabel
import com.example.sample2.ui.journal.shareJournalBackup
import com.example.sample2.ui.analytics.PersonalityAnalyticsScreen
import com.example.sample2.ui.theme.ChatGptTheme
import com.example.sample2.util.formatDate
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val state = remember {
        ChatState(context).apply {
            isSingleLineMode = true
        }
    }

    var showHeatmap by remember { mutableStateOf(false) }
    var showDailyRecordScreen by remember { mutableStateOf(false) }
    var showPersonalityAnalytics by remember { mutableStateOf(false) }
    var filterState by remember { mutableStateOf(JournalFilterState()) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var dailyRecordsVersion by remember { mutableIntStateOf(0) }

    val dailyRecords = remember(dailyRecordsVersion) {
        JournalJsonStorage.loadDailyRecords(context)
    }

    val hasActiveFilter = remember(filterState) {
        filterState.weekday != WeekdayFilter.ALL ||
                filterState.emotions.isNotEmpty()
    }

    val currentMode = when {
        showDailyRecordScreen -> JournalScreenMode.DailyRecord
        showPersonalityAnalytics -> JournalScreenMode.Analytics
        showHeatmap -> JournalScreenMode.Heatmap
        else -> JournalScreenMode.Journal
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    fun switchToJournal() {
        showHeatmap = false
        showDailyRecordScreen = false
        showPersonalityAnalytics = false
        showFilterSheet = false
    }

    fun openAnalytics() {
        showFilterSheet = false
        showDailyRecordScreen = false
        showHeatmap = false
        showPersonalityAnalytics = true
    }

    fun openHeatmap() {
        showFilterSheet = false
        showDailyRecordScreen = false
        showPersonalityAnalytics = false
        showHeatmap = true
    }

    fun openDailyRecord() {
        showFilterSheet = false
        showHeatmap = false
        showPersonalityAnalytics = false
        showDailyRecordScreen = true
    }

    BackHandler(enabled = currentMode != JournalScreenMode.Journal) {
        switchToJournal()
    }

    val displayMessages by remember {
        derivedStateOf {
            state.messages
                .filter { filterState.matches(it) }
                .sortedBy { it.timestamp }
        }
    }

    val dateLabel = buildJournalDateLabel(
        timestamp = displayMessages.lastOrNull()?.timestamp ?: System.currentTimeMillis()
    )

    LaunchedEffect(displayMessages.size, currentMode) {
        if (
            currentMode == JournalScreenMode.Journal &&
            displayMessages.isNotEmpty()
        ) {
            listState.scrollToItem(displayMessages.lastIndex)
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )
            val spokenText = results?.firstOrNull()
            if (spokenText != null) {
                state.inputText = spokenText
            }
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                JournalJsonStorage.exportBackupToUri(context, it)
                Toast.makeText(context, "バックアップを保存しました", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "バックアップ保存に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val result = JournalJsonStorage.restoreBackupFromUri(context, it)
                state.messages.clear()
                state.messages.addAll(JournalJsonStorage.loadMessages(context))
                dailyRecordsVersion++

                Toast.makeText(
                    context,
                    "リストア完了: ${result.messageCount}件 / 日次データ${result.dailyRecordCount}件",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "リストアに失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (state.showRestoreDialog) {
        RestoreConfirmDialog(
            onConfirm = {
                state.showRestoreDialog = false
                restoreLauncher.launch(arrayOf("application/json"))
            },
            onDismiss = { state.showRestoreDialog = false }
        )
    }

    if (state.deleteTarget != null) {
        DeleteMessageConfirmDialog(
            onConfirm = {
                state.deleteTarget?.let { state.deleteMessage(it) }
                state.deleteTarget = null
            },
            onDismiss = { state.deleteTarget = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.selectedMessage == null) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    JournalDrawerContent(
                        onClose = {
                            scope.launch { drawerState.close() }
                        },
                        onCopy = {
                            val text = state.messages.joinToString("\n") {
                                "${formatDate(it.timestamp)} ${it.text}"
                            }

                            if (text.isNotBlank()) {
                                val clipboard =
                                    context.getSystemService(ClipboardManager::class.java)
                                val clip = ClipData.newPlainText("chat.txt", text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "コピー", Toast.LENGTH_SHORT).show()
                            }

                            scope.launch { drawerState.close() }
                        },
                        onShare = {
                            try {
                                shareJournalBackup(context)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "共有に失敗しました", Toast.LENGTH_SHORT).show()
                            }
                            scope.launch { drawerState.close() }
                        },
                        onBackup = {
                            backupLauncher.launch("journal_backup.json")
                            scope.launch { drawerState.close() }
                        },
                        onRestore = {
                            state.showRestoreDialog = true
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            ) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .navigationBarsPadding()
                        ) {
                            JournalBottomModeBar(
                                currentMode = currentMode,
                                onOpenJournal = { switchToJournal() },
                                onOpenAnalytics = { openAnalytics() },
                                onOpenHeatmap = { openHeatmap() },
                                onOpenDailyRecord = { openDailyRecord() }
                            )

                            if (currentMode == JournalScreenMode.Journal) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .imePadding()
                                ) {
                                    SmartInputBar(
                                        inputText = state.inputText,
                                        onInputChange = { state.inputText = it },
                                        onMicClick = {
                                            val intent = Intent(
                                                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                                            ).apply {
                                                putExtra(
                                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                                )
                                                putExtra(
                                                    RecognizerIntent.EXTRA_LANGUAGE,
                                                    "ja-JP"
                                                )
                                            }
                                            speechLauncher.launch(intent)
                                        },
                                        onSendClick = { state.addMessage() }
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    when (currentMode) {
                        JournalScreenMode.DailyRecord -> {
                            Box(modifier = Modifier.padding(padding)) {
                                DailyRecordScreen(
                                    onClose = {
                                        switchToJournal()
                                        dailyRecordsVersion++
                                    }
                                )
                            }
                        }

                        JournalScreenMode.Analytics -> {
                            PersonalityAnalyticsScreen(
                                messages = state.messages,
                                dailyRecords = dailyRecords,
                                onUpdateDailyRecord = { updated ->
                                    JournalJsonStorage.upsertDailyRecord(context, updated)
                                    dailyRecordsVersion++
                                },
                                modifier = Modifier.padding(padding)
                            )
                        }

                        JournalScreenMode.Heatmap -> {
                            ChatGptTheme {
                                HeatmapScreen(
                                    state = state,
                                    onBack = { switchToJournal() },
                                    modifier = Modifier.padding(padding)
                                )
                            }
                        }

                        JournalScreenMode.Journal -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding)
                            ) {
                                JournalCompactMetaRow(
                                    dateLabel = dateLabel,
                                    hasActiveFilter = hasActiveFilter,
                                    isSingleLineMode = state.isSingleLineMode,
                                    onMenuClick = {
                                        scope.launch { drawerState.open() }
                                    },
                                    onFilterClick = { showFilterSheet = true },
                                    onToggleSingleLine = {
                                        state.isSingleLineMode = !state.isSingleLineMode
                                    },
                                    modifier = Modifier.padding(
                                        start = 12.dp,
                                        end = 12.dp,
                                        top = 8.dp,
                                        bottom = 2.dp
                                    )
                                )

                                AnimatedVisibility(
                                    visible = hasActiveFilter
                                ) {
                                    JournalFilterHeader(
                                        filterState = filterState,
                                        resultCount = displayMessages.size,
                                        onOpenSheet = { showFilterSheet = true },
                                        onClearWeekday = {
                                            filterState = filterState.copy(
                                                weekday = WeekdayFilter.ALL
                                            )
                                        },
                                        onRemoveEmotion = { emotion ->
                                            filterState = filterState.copy(
                                                emotions = filterState.emotions - emotion
                                            )
                                        },
                                        onClearAll = {
                                            filterState = JournalFilterState()
                                        },
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }

                                JournalMessageListPane(
                                    messages = displayMessages,
                                    listState = listState,
                                    isSingleLineMode = state.isSingleLineMode,
                                    timestampOf = { it.timestamp },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                ) { msg ->
                                    MessageBubble(
                                        message = msg,
                                        state = state,
                                        onDelete = { state.deleteMessage(msg) },
                                        onUpdate = { updated ->
                                            state.updateMessage(updated)
                                        }
                                    )
                                }
                            }

                            if (showFilterSheet) {
                                JournalFilterBottomSheet(
                                    current = filterState,
                                    onDismiss = { showFilterSheet = false },
                                    onApply = { applied ->
                                        filterState = applied
                                        showFilterSheet = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        state.selectedMessage?.let { msg ->
            MessageActionOverlay(
                message = msg,
                state = state,
                onDismiss = { state.selectedMessage = null },
                onDelete = { state.deleteMessage(msg) },
                onUpdate = { updated -> state.updateMessage(updated) }
            )
        }
    }
}

