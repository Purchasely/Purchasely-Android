package com.purchasely.samplev2.presentation.screen.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purchasely.samplev2.domain.log.LogMessage
import com.purchasely.samplev2.presentation.common.CommonScreenHeader
import com.purchasely.samplev2.presentation.common.navigationBarsContentPadding
import com.purchasely.samplev2.presentation.common.CommonTextField
import com.purchasely.samplev2.presentation.theme.Cream
import com.purchasely.samplev2.presentation.theme.DangerSoft
import com.purchasely.samplev2.presentation.theme.LavenderInk
import com.purchasely.samplev2.presentation.theme.LavenderSoft
import com.purchasely.samplev2.presentation.theme.SurfaceInset
import com.purchasely.samplev2.presentation.theme.Warning
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun LogsScreen(navController: NavController, viewModel: LogsViewModel = koinViewModel()) {
    val uiState = viewModel.uiState.collectAsState()

    val systemUiController = rememberSystemUiController()
    systemUiController.isSystemBarsVisible = true

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            Column {
                CommonScreenHeader(
                    title = "Logs",
                    onBackClick = { navController.navigateUp() }
                )
            }

        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            if (uiState.value.logs.isNotEmpty())
                                listState.animateScrollToItem(0)
                        }
                    },
                    content = { Icon(Icons.Default.ArrowUpward, "Scroll to top") }
                )

                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            if (uiState.value.logs.isNotEmpty())
                                listState.animateScrollToItem(uiState.value.logs.size - 1)
                        }
                    },
                    content = { Icon(Icons.Default.ArrowDownward, "Scroll to bottom") }
                )
            }
        },
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures {
                focusManager.clearFocus()
            }
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorScheme.background)
                // Only the top: the log list takes the bottom inset as content padding so it
                // scrolls under the navigation bar.
                .padding(top = it.calculateTopPadding())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            LogFilter(
                uiState = uiState.value,
                onFilterTextChanged = { newKeyword -> viewModel.updateKeyword(newKeyword) },
                onClear = { viewModel.clearLogs() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                state = listState,
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = navigationBarsContentPadding,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(uiState.value.logs.size) { index ->
                    Log(uiState.value.logs[uiState.value.logs.size - 1 - index])
                }
            }
        }
    }

}

@Composable
fun LogFilter(
    uiState: LogsUiState,
    onFilterTextChanged: (String) -> Unit,
    onClear: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        CommonTextField(
            label = "Filter logs",
            value = uiState.keyword,
            onValueChange = { onFilterTextChanged(it) },
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = { onClear() },
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            elevation = ButtonDefaults.buttonElevation(6.dp),
            modifier = Modifier.wrapContentSize()
        ) {
            Text(text = "Clear")
        }
    }
}

@Composable
fun Log(log: LogMessage) {
    val (badgeBackground, badgeContent) = logLevelColors(log.level)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = log.level,
                style = MaterialTheme.typography.labelSmall,
                color = badgeContent,
                modifier = Modifier
                    .background(badgeBackground, MaterialTheme.shapes.extraSmall)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
            Text(
                text = log.date,
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = log.message.trim(),
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = colorScheme.onSurface
        )
    }
}

/** Background and foreground of a level badge, keyed on [io.purchasely.ext.LogLevel]. */
@Composable
@ReadOnlyComposable
private fun logLevelColors(level: String): Pair<Color, Color> = with(MaterialTheme.colorScheme) {
    when (level) {
        "ERROR" -> errorContainer to error
        "WARN" -> tertiaryContainer to onTertiaryContainer
        "DEBUG" -> secondaryContainer to onSecondaryContainer
        else -> surfaceVariant to onSurfaceVariant
    }
}