package com.example.sample2.ui.journal

import androidx.compose.runtime.saveable.Saver

fun stringSetSaver(): Saver<Set<String>, List<String>> = Saver(
    save = { it.toList() },
    restore = { it.toSet() }
)
