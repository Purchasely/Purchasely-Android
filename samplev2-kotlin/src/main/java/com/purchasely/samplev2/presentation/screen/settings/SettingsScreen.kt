package com.purchasely.samplev2.presentation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIos
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purchasely.demo.R
import com.purchasely.samplev2.presentation.theme.Brand
import com.purchasely.samplev2.presentation.common.CommonDropdownMenu
import com.purchasely.samplev2.presentation.common.CommonTextField
import com.purchasely.samplev2.presentation.common.DisplayMethod
import com.purchasely.samplev2.presentation.common.DropDownCommonTextField
import com.purchasely.samplev2.presentation.common.drawTwoColors
import io.purchasely.views.presentation.models.PLYDimensionType
import io.purchasely.views.presentation.models.PLYTransitionType
import com.purchasely.samplev2.presentation.navigation.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = koinViewModel()) {
    val systemUiController = rememberSystemUiController()
    systemUiController.isSystemBarsVisible = true

    val uiState = viewModel.uiState
    Scaffold(
        bottomBar = {
            SaveButton{
                viewModel.saveSettings()
                navController.navigate(Screen.Home.route) { popUpTo(0) }
            }
        },
        content = {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = it,
                modifier = Modifier
                    .fillMaxSize()
                    .drawTwoColors(Brand.colors.hero, colorScheme.background)
            ) {
                item { SettingsHeader(onBackClick = { navController.navigateUp() }) }
                item { IdentifiersCard(uiState.value) }
                item { SwitchsCard(uiState.value) }
                item { URLsCard(uiState.value) }
                item { DropdownsCard(uiState.value) }
            }
        }
    )
}

@Composable
fun SettingsHeader(onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onBackClick, modifier = Modifier.padding(start = 4.dp)) {
            Icon(
                imageVector = Icons.Outlined.ArrowBackIos,
                contentDescription = "settings back",
                tint = Brand.colors.onHero,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 34.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_text),
                contentDescription = "logo",
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

@Composable
fun IdentifiersCard(uiState: SettingsUiState) {
    Card(
        colors = CardDefaults.cardColors(colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shapes.medium)
            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(all = 24.dp)
            ) {
                CommonTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = "User ID",
                    value = uiState.userId,
                    onValueChange = { uiState.userId = it }
                )
                DropDownCommonTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Presentation ID",
                    value = uiState.presentationId,
                    onValueChange = { uiState.presentationId = it },
                    historyKeywords = uiState.presentationHistory
                )
                DropDownCommonTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Placement ID",
                    value = uiState.placementId,
                    onValueChange = { uiState.placementId = it },
                    historyKeywords = uiState.placementHistory
                )
                CommonTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Content ID",
                    value = uiState.contentId,
                    onValueChange = { uiState.contentId = it }
                )
                val selectedDisplayMethod = remember { mutableStateOf(uiState.displayMethod) }
                val selectedTransitionType = remember { mutableStateOf(uiState.transitionType) }

                CommonDropdownMenu(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Display Method",
                    items = DisplayMethod.entries.map { it.name },
                    selected = selectedDisplayMethod.value.name,
                    onItemChange = {
                        val method = DisplayMethod.valueOf(it)
                        selectedDisplayMethod.value = method
                        uiState.displayMethod = method
                    }
                )

                if (selectedDisplayMethod.value == DisplayMethod.DISPLAY) {
                    CommonDropdownMenu(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Transition Type",
                        items = PLYTransitionType.entries
                            .filter { it != PLYTransitionType.INLINE_PAYWALL }
                            .map { it.name },
                        selected = selectedTransitionType.value.name,
                        onItemChange = {
                            val type = PLYTransitionType.valueOf(it)
                            selectedTransitionType.value = type
                            uiState.transitionType = type
                        }
                    )

                    if (selectedTransitionType.value == PLYTransitionType.DRAWER ||
                        selectedTransitionType.value == PLYTransitionType.POPIN
                    ) {
                        DimensionInputRow(
                            label = "Height",
                            type = uiState.transitionHeightType,
                            value = uiState.transitionHeightValue,
                            onTypeChange = { uiState.transitionHeightType = it },
                            onValueChange = { uiState.transitionHeightValue = it }
                        )
                    }

                    if (selectedTransitionType.value == PLYTransitionType.POPIN) {
                        DimensionInputRow(
                            label = "Width",
                            type = uiState.transitionWidthType,
                            value = uiState.transitionWidthValue,
                            onTypeChange = { uiState.transitionWidthType = it },
                            onValueChange = { uiState.transitionWidthValue = it }
                        )
                    }
                }
            }
        }
    }
}

/**
 * A single transition dimension input: a unit dropdown (% or px) and a value
 * field laid out in the same row with equal weight. Percentage values are
 * entered as whole numbers (e.g. 60 for 60%).
 */
@Composable
fun DimensionInputRow(
    label: String,
    type: PLYDimensionType,
    value: String,
    onTypeChange: (PLYDimensionType) -> Unit,
    onValueChange: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            CommonDropdownMenu(
                modifier = Modifier.fillMaxWidth(),
                label = "$label unit",
                items = dimensionTypeLabels,
                selected = type.toDimensionLabel(),
                onItemChange = { onTypeChange(it.toDimensionType()) }
            )
        }
        CommonTextField(
            modifier = Modifier.weight(1f),
            label = "$label value",
            value = value,
            keyboardType = KeyboardType.Number,
            onValueChange = onValueChange
        )
    }
}

private val dimensionTypeLabels = listOf("%", "px")

private fun PLYDimensionType.toDimensionLabel(): String = when (this) {
    PLYDimensionType.PIXEL -> "px"
    PLYDimensionType.PERCENTAGE -> "%"
}

private fun String.toDimensionType(): PLYDimensionType =
    if (this == "px") PLYDimensionType.PIXEL else PLYDimensionType.PERCENTAGE

@Composable
fun SwitchsCard(uiState: SettingsUiState) {
    Card(
        colors = CardDefaults.cardColors(colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shapes.medium)
            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
    ) {
        SwitchOption(
            label = "SDK DEBUG MODE",
            isEnabled = uiState.debugMode,
            onCheckedChange = { uiState.debugMode = it }
        )
        SwitchOption(
            label = "OBSERVER MODE",
            isEnabled = uiState.isObserverMode,
            onCheckedChange = {
                uiState.isObserverMode = it
                uiState.needRestart = true
            }
        )
        SwitchOption(
            label = "ALLOW DEEPLINK",
            isEnabled = uiState.allowDeeplink,
            onCheckedChange = {
                uiState.allowDeeplink = it
                uiState.needRestart = true
            }
        )
        SwitchOption(
            label = "ALLOW CAMPAIGNS",
            isEnabled = uiState.allowCampaigns,
            onCheckedChange = {
                uiState.allowCampaigns = it
                uiState.needRestart = true
            }
        )
        // Only changes what Home renders, so it needs no SDK restart.
        SwitchOption(
            label = "SHOW INLINE SCREEN",
            isEnabled = uiState.showInlineScreen,
            onCheckedChange = { uiState.showInlineScreen = it }
        )
    }
}

@Composable
fun SwitchOption(label: String, isEnabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val switchState = remember { mutableStateOf(isEnabled) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = SemiBold, color = colorScheme.onSurfaceVariant)

        Switch(
            checked = switchState.value,
            onCheckedChange = {
                switchState.value = it
                onCheckedChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorScheme.primary,
                checkedTrackColor = colorScheme.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedTrackColor = colorScheme.onSurface.copy(alpha = 0.12f)
            ),
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
fun URLsCard(uiState: SettingsUiState) {
    Card(
        colors = CardDefaults.cardColors(colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shapes.small)
            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(all = 24.dp)
            ) {
                CommonTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Api Key",
                    value = uiState.apiKey,
                    onValueChange = {
                        uiState.apiKey = it
                        uiState.needRestart = true
                    }
                )
            }
        }
    }
}

@Composable
fun DropdownsCard(uiState: SettingsUiState) {
    Card(
        colors = CardDefaults.cardColors(colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shapes.medium)
            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            CommonDropdownMenu(
                modifier = Modifier.fillMaxWidth(),
                label = "Theme Color",
                items = uiState.themesList,
                selected = uiState.theme,
                onItemChange = { uiState.theme = it }
            )
            CommonDropdownMenu(
                modifier = Modifier.fillMaxWidth(),
                label = "SDK language",
                items = uiState.languagesList,
                selected = uiState.language,
                onItemChange = { uiState.language = it }
            )
            CommonDropdownMenu(
                modifier = Modifier.fillMaxWidth(),
                label = "Store",
                items = uiState.storesList,
                selected = uiState.store,
                onItemChange = { uiState.store = it }
            )
        }
    }
}

/** Opaque, since the form scrolls underneath it. */
@Composable
fun SaveButton(onSaveClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.background)
    ) {
        HorizontalDivider(color = colorScheme.outline)
        Button(
            onClick = onSaveClick,
            shape = shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(50.dp)
        ) {
            Text(text = "Save", fontWeight = Bold)
        }
    }
}

