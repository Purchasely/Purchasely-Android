package com.purchasely.samplev2.presentation.screen.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purchasely.samplev2.R
import com.purchasely.samplev2.presentation.common.CommonDropdownMenu
import com.purchasely.samplev2.presentation.common.CommonSwitchOption
import com.purchasely.samplev2.presentation.common.CommonTextField
import com.purchasely.samplev2.presentation.common.DropDownCommonTextField
import com.purchasely.samplev2.presentation.common.drawTwoColors
import com.purchasely.samplev2.presentation.navigation.Screen
import com.purchasely.samplev2.presentation.theme.Gray100

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
        CommonSwitchOption(
            label = "OBSERVER MODE",
            isEnabled = uiState.isObserverMode,
            onCheckedChange = {
                uiState.isObserverMode = it
                uiState.needRestart = true
            }
        )
        CommonSwitchOption(
            label = "ASYNC LOADING",
            isEnabled = uiState.isAsyncLoading,
            onCheckedChange = { uiState.isAsyncLoading = it }
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
                label = "Theme Color",
                items = uiState.themesList,
                selected = uiState.theme,
                onItemChange = { uiState.theme = it }
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

