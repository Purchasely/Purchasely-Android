package com.purchasely.samplev2.presentation.screen.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.purchasely.samplev2.presentation.common.CommonScreenHeader
import io.purchasely.ext.PLYDataProcessingPurpose
import org.koin.androidx.compose.koinViewModel

@Composable
fun PrivacyScreen(navController: NavController, viewModel: PrivacyViewModel = koinViewModel()) {
    val dataProcessing = viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .background(color = colorScheme.background)
            .fillMaxSize() // ensure we can apply inset padding
            .navigationBarsPadding() // add bottom padding for nav bar
    ) {
        CommonScreenHeader(
            title = "Privacy",
            onBackClick = { navController.navigateUp() },
            //modifier = Modifier.statusBarsPadding()
        )

        Text(
            text = buildAnnotatedString {
                append("Select the purposes you want to enable,\n")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                    append("unchecking")
                }
                append(" a purpose will ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                    append(" revoke the consent ")
                }
                append("for it and delete all associated data.")
            },
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            fontWeight = SemiBold,
            color = colorScheme.onSurfaceVariant
        )

        Column(modifier = Modifier.background(color = colorScheme.background)) {
            PrivacySwitch(
                label = "All Non Essentials",
                isChecked = !dataProcessing.value.all,
            ) { checked ->
                viewModel.setPurposes(if (checked) emptySet() else setOf(PLYDataProcessingPurpose.AllNonEssentials))
            }
            PrivacySwitch(
                label = "Analytics",
                isChecked = !dataProcessing.value.purposes.contains(PLYDataProcessingPurpose.Analytics)
            ) { checked ->
                trigger(checked, PLYDataProcessingPurpose.Analytics, viewModel)
            }
            PrivacySwitch(
                label = "Personalization",
                isChecked = !dataProcessing.value.purposes.contains(PLYDataProcessingPurpose.Personalization)
            ) { checked ->
                trigger(checked, PLYDataProcessingPurpose.Personalization, viewModel)
            }
            PrivacySwitch(
                label = "Identified Analytics",
                isChecked = !dataProcessing.value.purposes.contains(PLYDataProcessingPurpose.IdentifiedAnalytics)
            ) { checked ->
                trigger(checked, PLYDataProcessingPurpose.IdentifiedAnalytics, viewModel)
            }
            PrivacySwitch(
                label = "Campaigns",
                isChecked = !dataProcessing.value.purposes.contains(PLYDataProcessingPurpose.Campaigns)
            ) { checked ->
                trigger(checked, PLYDataProcessingPurpose.Campaigns, viewModel)
            }
            PrivacySwitch(
                label = "Third party integrations",
                isChecked = !dataProcessing.value.purposes.contains(PLYDataProcessingPurpose.ThirdPartyIntegrations)
            ) { checked ->
                trigger(checked, PLYDataProcessingPurpose.ThirdPartyIntegrations, viewModel)
            }
        }
    }

}

private fun trigger(
    checked: Boolean,
    purpose: PLYDataProcessingPurpose,
    viewModel: PrivacyViewModel
) {
    val newPurposes = viewModel.uiState.value.purposes.toMutableSet()
    if (checked) {
        newPurposes.remove(purpose)
    } else {
        newPurposes.add(purpose)
    }
    viewModel.setPurposes(newPurposes)
}

@Composable
fun PrivacySwitch(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = SemiBold, color = colorScheme.onSurfaceVariant)

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
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
