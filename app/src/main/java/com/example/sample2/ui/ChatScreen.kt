package com.example.sample2.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.core.content.FileProvider
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sample2.data.DefaultJournalRepository
import com.example.sample2.data.ActionType
import com.example.sample2.data.JournalEntryType
import com.example.sample2.data.JournalBackupService
import com.example.sample2.data.JournalLocalDataSource
import com.example.sample2.data.MessageV2
import com.example.sample2.data.maxEmotionOrNull
import com.example.sample2.ui.analytics.AnalyticsDisplayMode
import com.example.sample2.ui.analytics.PersonalityAnalyticsScreen
import com.example.sample2.util.formatDate
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class JournalScreenMode {
    Journal,
    Analytics,
    AnalyticsDetail,
    DailyRecord,
    Reflection
}

@Composable
fun ChatScreen() {
    ChatRoute()
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoute() {
    val context = LocalContext.current
    val localDataSource = remember(context) { JournalLocalDataSource(context) }
    val backupService = remember(context, localDataSource) {
        JournalBackupService(context, localDataSource)
    }
    val repository = remember(localDataSource, backupService) {
        DefaultJournalRepository(localDataSource, backupService)
    }
    val state = remember(repository) {
        JournalViewModel(repository).apply {
            isSingleLineMode = true
        }
    }

    var showDailyRecordScreen by remember { mutableStateOf(false) }
    var showPersonalityAnalytics by remember { mutableStateOf(false) }
    var showPersonalityAnalyticsDetail by remember { mutableStateOf(false) }
    var showReflectionScreen by remember { mutableStateOf(false) }
    var reflectionEditorDate by remember { mutableStateOf<String?>(null) }
    var reflectionsVersion by remember { mutableIntStateOf(0) }
    var filterState by remember { mutableStateOf(JournalFilterState()) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var workModeEnabled by remember { mutableStateOf(false) }
    var dailyRecordsVersion by remember { mutableIntStateOf(0) }
    var addChildTarget by remember { mutableStateOf<MessageV2?>(null) }

    val dailyRecords = remember(dailyRecordsVersion) {
        state.loadDailyRecords()
    }

    val hasActiveFilter = remember(filterState) {
        filterState.weekday != WeekdayFilter.ALL ||
                filterState.emotions.isNotEmpty()
    }

    val currentMode = when {
        showReflectionScreen -> JournalScreenMode.Reflection
        showDailyRecordScreen -> JournalScreenMode.DailyRecord
        showPersonalityAnalyticsDetail -> JournalScreenMode.AnalyticsDetail
        showPersonalityAnalytics -> JournalScreenMode.Analytics
        else -> JournalScreenMode.Journal
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    fun switchToJournal() {
        showDailyRecordScreen = false
        showPersonalityAnalytics = false
        showPersonalityAnalyticsDetail = false
        showReflectionScreen = false
        showFilterSheet = false
    }

    fun openAnalytics() {
        showFilterSheet = false
        showDailyRecordScreen = false
        showReflectionScreen = false
        showPersonalityAnalyticsDetail = false
        showPersonalityAnalytics = true
    }

    fun openAnalyticsDetail() {
        showFilterSheet = false
        showDailyRecordScreen = false
        showReflectionScreen = false
        showPersonalityAnalytics = false
        showPersonalityAnalyticsDetail = true
    }

    fun openDailyRecord() {
        showFilterSheet = false
        showPersonalityAnalytics = false
        showPersonalityAnalyticsDetail = false
        showReflectionScreen = false
        showDailyRecordScreen = true
    }

    var reflectionInitialDate by remember { mutableStateOf(todayDateStringForRoute()) }

    fun openReflectionTimeline() {
        showFilterSheet = false
        showPersonalityAnalytics = false
        showPersonalityAnalyticsDetail = false
        showDailyRecordScreen = false
        reflectionEditorDate = null
        showReflectionScreen = true
    }

    fun openReflectionEditor(date: String = todayDateStringForRoute()) {
        showFilterSheet = false
        showPersonalityAnalytics = false
        showPersonalityAnalyticsDetail = false
        showDailyRecordScreen = false
        reflectionInitialDate = date
        reflectionEditorDate = date
        showReflectionScreen = true
    }

    BackHandler(enabled = currentMode != JournalScreenMode.Journal) {
        switchToJournal()
    }

    val parentEntries by remember {
        derivedStateOf {
            state.messages
                .filter {
                    it.parentId == null &&
                            it.entryType == JournalEntryType.MEMO &&
                            filterState.matches(it)
                }
                .sortedBy { it.timestamp }
        }
    }

    val childEntriesByParentId by remember {
        derivedStateOf {
            state.messages
                .asSequence()
                .filter {
                    it.parentId != null &&
                            it.entryType == JournalEntryType.EMOTION_RESPONSE
                }
                .groupBy { it.parentId!! }
                .mapValues { (_, children) ->
                    children.sortedBy { it.timestamp }
                }
        }
    }

    val workActionSummary by remember {
        derivedStateOf {
            WorkActionSummary.fromMessages(state.messages.filter { it.isToday() })
        }
    }

    val dateLabel = buildJournalDateLabel(
        timestamp = parentEntries.lastOrNull()?.timestamp ?: System.currentTimeMillis()
    )

    LaunchedEffect(parentEntries.size, currentMode) {
        if (
            currentMode == JournalScreenMode.Journal &&
            parentEntries.isNotEmpty()
        ) {
            listState.scrollToItem(parentEntries.lastIndex)
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
                state.exportBackupToUri(it)
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
                val result = state.restoreBackupFromUri(it)
                dailyRecordsVersion++

                Toast.makeText(
                    context,
                    "リストア完了: ${result.messageCount}件 / 日次データ${result.dailyRecordCount}件 / 振り返り${result.dailyReflectionCount}件",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "リストアに失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (state.showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { state.showRestoreDialog = false },
            title = { Text("リストア確認") },
            text = { Text("現在のデータは上書きされます。\nよろしいですか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.showRestoreDialog = false
                        restoreLauncher.launch(arrayOf("application/json"))
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { state.showRestoreDialog = false }
                ) {
                    Text("キャンセル")
                }
            }
        )
    }

    if (state.deleteTarget != null) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { state.deleteTarget = null },
            title = { Text("削除") },
            text = { Text("このメッセージを削除しますか？") },
            confirmButton = {
                Button(
                    onClick = {
                        state.deleteTarget?.let { state.deleteMessage(it) }
                        state.deleteTarget = null
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { state.deleteTarget = null }) {
                    Text("キャンセル")
                }
            }
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
                                shareJournalBackup(context, repository)
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
                    floatingActionButton = {
                        if (currentMode == JournalScreenMode.Journal) {
                            FloatingActionButton(
                                onClick = { state.addMessage() },
                                containerColor = androidx.compose.ui.graphics.Color.Black,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            ) {
                                Icon(imageVector = Icons.Default.EditNote, contentDescription = "記録する")
                            }
                        }
                    },
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
                                onOpenAnalyticsDetail = { openAnalyticsDetail() },
                                onOpenDailyRecord = { openDailyRecord() },
                                onOpenReflection = { openReflectionTimeline() }
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
                                    },
                                    onOpenReflection = { date -> openReflectionEditor(date) }
                                )
                            }
                        }

                        JournalScreenMode.Reflection -> {
                            if (reflectionEditorDate == null) {
                                val reflections = remember(reflectionsVersion) {
                                    state.loadDailyReflections()
                                }
                                ReflectionTimelineScreen(
                                    reflections = reflections,
                                    onOpenReflection = { date ->
                                        openReflectionEditor(date)
                                    },
                                    onCreateToday = { openReflectionEditor() }
                                )
                            } else {
                                DailyReflectionScreen(
                                    state = state,
                                    initialDate = reflectionInitialDate,
                                    onClose = { reflectionEditorDate = null },
                                    onSaved = { reflectionsVersion++ }
                                )
                            }
                        }

                        JournalScreenMode.Analytics -> {
                            PersonalityAnalyticsScreen(
                                messages = state.messages,
                                dailyRecords = dailyRecords,
                                initialDisplayMode = AnalyticsDisplayMode.CHARTS,
                                displayModes = listOf(
                                    AnalyticsDisplayMode.CHARTS,
                                    AnalyticsDisplayMode.MAP
                                ),
                                onUpdateDailyRecord = { updated ->
                                    state.upsertDailyRecord(updated)
                                    dailyRecordsVersion++
                                },
                                modifier = Modifier.padding(padding)
                            )
                        }

                        JournalScreenMode.AnalyticsDetail -> {
                            PersonalityAnalyticsScreen(
                                messages = state.messages,
                                dailyRecords = dailyRecords,
                                initialDisplayMode = AnalyticsDisplayMode.DETAIL,
                                displayModes = listOf(AnalyticsDisplayMode.DETAIL),
                                onUpdateDailyRecord = { updated ->
                                    state.upsertDailyRecord(updated)
                                    dailyRecordsVersion++
                                },
                                modifier = Modifier.padding(padding)
                            )
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
                                    isWorkMode = workModeEnabled,
                                    onMenuClick = {
                                        scope.launch { drawerState.open() }
                                    },
                                    onFilterClick = { showFilterSheet = true },
                                    onToggleWorkMode = {
                                        workModeEnabled = !workModeEnabled
                                    },
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
                                        resultCount = parentEntries.size,
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

                                AnimatedVisibility(
                                    visible = workModeEnabled
                                ) {
                                    WorkActionSummaryRow(
                                        summary = workActionSummary,
                                        modifier = Modifier.padding(
                                            start = 12.dp,
                                            end = 12.dp,
                                            top = 2.dp,
                                            bottom = 6.dp
                                        )
                                    )
                                }

                                JournalMessageListPane(
                                    messages = parentEntries,
                                    listState = listState,
                                    isSingleLineMode = state.isSingleLineMode,
                                    timestampOf = { it.timestamp },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                ) { msg ->
                                    val childEntries = childEntriesByParentId[msg.id].orEmpty()
                                    Column {
                                        MessageBubble(
                                            message = msg,
                                            state = state,
                                            onDelete = { state.deleteMessage(msg) },
                                            onUpdate = { updated ->
                                                state.updateMessage(updated)
                                            },
                                            onDoubleClick = { parent ->
                                                addChildTarget = parent
                                            }
                                        )

                                        if (childEntries.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            childEntries.forEach { child ->
                                                EmotionResponseChildBubble(
                                                    message = child,
                                                    onLongClick = { state.selectedMessage = it }
                                                )
                                            }
                                        }
                                    }
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

        addChildTarget?.let { parent ->
            AddChildMessageDialog(
                parent = parent,
                onDismiss = { addChildTarget = null },
                onAdd = { note ->
                    state.addEmotionResponse(
                        parent = parent,
                        targetEmotionKey = parent.emotions.maxEmotionOrNull()?.key ?: "",
                        actionKey = "",
                        effectScore = 0,
                        note = note
                    )
                    addChildTarget = null
                }
            )
        }
    }
}

@Composable
private fun AddChildMessageDialog(
    parent: MessageV2,
    onDismiss: () -> Unit,
    onAdd: (note: String) -> Unit
) {
    var note by remember(parent.id) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("子メッセージを追加") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("親: ${parent.text.take(40)}")

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("テキスト") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAdd(note)
                }
            ) {
                Text("追加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
private fun JournalBottomModeBar(
    currentMode: JournalScreenMode,
    onOpenJournal: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenAnalyticsDetail: () -> Unit,
    onOpenDailyRecord: () -> Unit,
    onOpenReflection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CompactActionChip(
            text = "記録",
            icon = Icons.Default.ViewAgenda,
            selected = currentMode == JournalScreenMode.Journal,
            onClick = onOpenJournal
        )

        CompactActionChip(
            text = "分析",
            icon = Icons.Default.ShowChart,
            selected = currentMode == JournalScreenMode.Analytics,
            onClick = onOpenAnalytics
        )

        CompactActionChip(
            text = "詳細",
            icon = Icons.Default.Psychology,
            selected = currentMode == JournalScreenMode.AnalyticsDetail,
            onClick = onOpenAnalyticsDetail
        )

        CompactActionChip(
            text = "日時",
            icon = Icons.Default.Today,
            selected = currentMode == JournalScreenMode.DailyRecord,
            onClick = onOpenDailyRecord
        )

        CompactActionChip(
            text = "振り返り",
            icon = Icons.Default.EditNote,
            selected = currentMode == JournalScreenMode.Reflection,
            onClick = onOpenReflection
        )
    }
}

@Composable
private fun JournalCompactMetaRow(
    dateLabel: String,
    hasActiveFilter: Boolean,
    isSingleLineMode: Boolean,
    isWorkMode: Boolean,
    onMenuClick: () -> Unit,
    onFilterClick: () -> Unit,
    onToggleWorkMode: () -> Unit,
    onToggleSingleLine: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveSingleLineMode = isSingleLineMode || isWorkMode

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactHeaderIconButton(
            selected = false,
            onClick = onMenuClick,
            icon = Icons.Default.Menu,
            contentDescription = "メニュー"
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactHeaderIconButton(
                selected = hasActiveFilter,
                onClick = onFilterClick,
                icon = Icons.Default.FilterList,
                contentDescription = "フィルタ"
            )

            CompactHeaderIconButton(
                selected = isWorkMode,
                onClick = onToggleWorkMode,
                icon = Icons.Default.Work,
                contentDescription = if (isWorkMode) "仕事表示をオフ" else "仕事表示をオン"
            )

            CompactHeaderIconButton(
                selected = effectiveSingleLineMode,
                onClick = onToggleSingleLine,
                icon = if (effectiveSingleLineMode) {
                    Icons.Default.ViewAgenda
                } else {
                    Icons.Default.ViewStream
                },
                contentDescription = if (effectiveSingleLineMode) {
                    "通常表示に切り替え"
                } else {
                    "1行表示に切り替え"
                }
            )
        }
    }
}

private data class WorkActionSummary(
    val delegate: Int,
    val challenge: Int,
    val breakdown: Int,
    val instruct: Int,
    val quickAction: Int
) {
    companion object {
        fun fromMessages(messages: List<MessageV2>): WorkActionSummary {
            return WorkActionSummary(
                delegate = messages.count { it.flags.delegate },
                challenge = messages.count { it.flags.challenge },
                breakdown = messages.count { it.flags.breakdown },
                instruct = messages.count { it.flags.instruct },
                quickAction = messages.count { it.flags.quickAction }
            )
        }
    }
}

private fun MessageV2.hasWorkAction(): Boolean {
    return flags.delegate ||
            flags.challenge ||
            flags.breakdown ||
            flags.instruct ||
            flags.quickAction
}

@Composable
private fun WorkActionSummaryRow(
    summary: WorkActionSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WorkActionSummaryItem(
            actionType = ActionType.DELEGATE,
            count = summary.delegate
        )
        WorkActionSummaryItem(
            actionType = ActionType.CHALLENGE,
            count = summary.challenge
        )
        WorkActionSummaryItem(
            actionType = ActionType.BREAKDOWN,
            count = summary.breakdown
        )
        WorkActionSummaryItem(
            actionType = ActionType.INSTRUCT,
            count = summary.instruct
        )
        WorkActionSummaryItem(
            actionType = ActionType.QUICK_ACTION,
            count = summary.quickAction
        )
    }
}

@Composable
private fun WorkActionSummaryItem(
    actionType: ActionType,
    count: Int
) {
    val uiSpec = actionType.toUiSpec()
    val iconColor = if (count > 0) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val valueColor = if (count > 0) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = uiSpec.iconRes),
            contentDescription = "${actionType.label}: $count",
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = count.toString(),
            color = valueColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun MessageV2.isToday(): Boolean {
    val target = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    val today = Calendar.getInstance()

    return target.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            target.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

@Composable
private fun CompactHeaderIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp)
            )

            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun CompactActionChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(androidx.compose.ui.graphics.Color(0xFFB91C1C))
            )
        }
    }
}


private fun todayDateStringForRoute(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
}

private fun shareJournalBackup(
    context: android.content.Context,
    repository: DefaultJournalRepository
) {
    val backupFile = File(
        context.cacheDir,
        "journal-share-${System.currentTimeMillis()}.json"
    )

    backupFile.outputStream().use { output ->
        repository.exportBackup(output)
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        backupFile
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "journal_backup.json")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "バックアップを共有"))
}

private fun buildJournalDateLabel(timestamp: Long): String {
    val dateText = SimpleDateFormat("M月d日(E)", Locale.JAPAN).format(Date(timestamp))

    val target = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    val today = Calendar.getInstance()

    val isToday =
        target.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                target.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

    return if (isToday) {
        "$dateText・今日"
    } else {
        dateText
    }
}
