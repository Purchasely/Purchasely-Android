package com.purchasely.samplev2.presentation.screen.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purchasely.demo.R
import com.purchasely.samplev2.presentation.common.CommonDropdownMenu
import com.purchasely.samplev2.presentation.common.CommonTextField
import com.purchasely.samplev2.presentation.common.DropDownCommonTextField
import com.purchasely.samplev2.presentation.common.drawTwoColors
import com.purchasely.samplev2.presentation.navigation.Screen
import com.purchasely.samplev2.presentation.theme.Gray100
import com.purchasely.samplev2.presentation.theme.Gray500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = hiltViewModel()) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .drawTwoColors(colorScheme.primary, Gray100)
                    .padding(it)
            ) {
                item { SettingsHeader() }
                item { IdentifiersCard(uiState.value) }
                item { SwitchsCard(uiState.value) }
                item { URLsCard(uiState.value) }
                item { DropdownsCard(uiState.value) }
            }
        }
    )
}

@Composable
fun SettingsHeader() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_text),
            contentDescription = "logo",
            modifier = Modifier.size(180.dp)
        )
    }
}

@Composable
fun IdentifiersCard(uiState: SettingsUiState) {
    Card(
        colors = CardDefaults.cardColors(Color.White),
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
            }
        }
    }
}

@Composable
fun SwitchsCard(uiState: SettingsUiState) {
    Card(
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shapes.medium)
            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
    ) {
        SwitchOption(
            label = "PRODUCTION",
            isEnabled = uiState.isProductionMode,
            onCheckedChange = {
                uiState.isProductionMode = it
                uiState.needRestart = true
            }
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
            label = "ASYNC LOADING",
            isEnabled = uiState.isAsyncLoading,
            onCheckedChange = { uiState.isAsyncLoading = it }
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
        Text(text = label, fontSize = 12.sp, fontWeight = SemiBold, color = Gray500)

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
        colors = CardDefaults.cardColors(Color.White),
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
                CommonTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Api Url",
                    value = uiState.apiUrl,
                    keyboardType = KeyboardType.Uri,
                    onValueChange = {
                        uiState.apiUrl = it
                        uiState.needRestart = true
                    }
                )
                CommonTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Paywall Url",
                    value = uiState.paywallUrl,
                    keyboardType = KeyboardType.Uri,
                    onValueChange = {
                        uiState.paywallUrl = it
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
        colors = CardDefaults.cardColors(Color.White),
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
                label = "Template",
                items = uiState.templatesList,
                selected = uiState.template,
                onItemChange = { uiState.template = it }
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

@Composable
fun SaveButton(onSaveClick: () -> Unit) {
    Button(
        onClick = onSaveClick,
        shape = shapes.medium,
        colors = ButtonDefaults.buttonColors(colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
            .height(50.dp)
    ) {
        Text(
            text = "Save",
            fontWeight = Bold,
            fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
            color = Color.White
        )
    }
}

