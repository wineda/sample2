package com.example.sample2.ui.analytics.components
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
@Composable fun ViewToggle(){ Row { FilterChip(selected=true,onClick={},label={Text("グラフ")}); FilterChip(selected=false,onClick={},label={Text("ヒートマップ")}) } }
