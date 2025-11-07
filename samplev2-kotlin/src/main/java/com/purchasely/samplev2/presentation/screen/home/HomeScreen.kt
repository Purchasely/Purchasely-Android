package com.purchasely.samplev2.presentation.screen.home

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purchasely.demo.R
import com.purchasely.samplev2.presentation.navigation.Screen
import com.purchasely.samplev2.presentation.screen.subscriptions.SubscriptionsActivity
import com.purchasely.samplev2.presentation.theme.Purple100
import com.purchasely.samplev2.presentation.theme.Purple500
import io.purchasely.ext.Purchasely
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = koinViewModel()) {
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val systemUiController = rememberSystemUiController()
    systemUiController.isSystemBarsVisible = true

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorScheme.primary)
            .padding(horizontal = 16.dp)
            .graphicsLayer { this.alpha = if (uiState.value.isRestoring) 0.2f else 1f },
    ) {
        item {
            HomeScreenHeader { navController.navigate(Screen.Settings.route) }
        }
        if (viewModel.displayPropertiesCard()) {
            item {
                PropertiesCard(uiState.value) { label, text ->
                    viewModel.saveToClipboard(context, label, text)
                }
            }
        }
        item {
            PurchaselyActions(
                uiState = uiState.value,
                onRestoreClick = { viewModel.restore() },
                onProductsClick = { navController.navigate(Screen.Products.route) },
                onAttributesClick = { navController.navigate(Screen.Attributes.route) },
                onViewPresentationClick = { navController.navigate(Screen.Paywall.route) },
                onSubscriptionsClick = { context.startActivity(Intent(context, SubscriptionsActivity::class.java)) },
            )
        }
    }

    if (uiState.value.isRestoring) {
        RestoreProgressIndicator()
    }
}

@Composable
fun HomeScreenHeader(onSettingsClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 37.dp, bottom = 16.dp)
    ) {
        IconButton(
            onClick = { onSettingsClick() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.Outlined.ManageAccounts,
                contentDescription = "settings",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "logo",
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 8.dp)
        )
        Text(
            text = "Purchasely Demo",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = "v${Purchasely.sdkVersion}",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
fun PurchaselyActions(
    uiState: HomeUiState,
    onViewPresentationClick: () -> Unit,
    onProductsClick: () -> Unit,
    onAttributesClick: () -> Unit,
    onSubscriptionsClick: () -> Unit,
    onRestoreClick: () -> Unit,

    ) {
    Column {
        Button(
            onClick = onViewPresentationClick,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(50.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    append("View presentation")
                    if (uiState.isAsync) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Black, color = Purple500)) {
                            append(" (async)")
                        }
                    }
                },
                fontWeight = FontWeight.Bold,
                fontStyle = typography.bodyLarge.fontStyle
            )
        }
        Button(
            onClick = onProductsClick,
            colors = ButtonDefaults.buttonColors(Color.White),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(50.dp)
        ) {
            Text(
                text = "Products",
                fontWeight = FontWeight.Bold,
                fontStyle = typography.bodyLarge.fontStyle
            )
        }

        Button(
            onClick = onAttributesClick,
            colors = ButtonDefaults.buttonColors(Color.White),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(50.dp)
        ) {
            Text(
                text = "Attributes",
                fontWeight = FontWeight.Bold,
                fontStyle = typography.bodyLarge.fontStyle
            )
        }

        Button(
            onClick = onSubscriptionsClick,
            colors = ButtonDefaults.buttonColors(Color.White),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(50.dp)
        ) {
            Text(
                text = "Subscriptions",
                fontWeight = FontWeight.Bold,
                fontStyle = typography.bodyLarge.fontStyle
            )
        }

        Button(
            onClick = onRestoreClick,
            colors = ButtonDefaults.buttonColors(Color.White),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(50.dp)
        ) {
            Text(
                text = "Restore",
                fontWeight = FontWeight.Bold,
                fontStyle = typography.bodyLarge.fontStyle
            )
        }
    }
}


@Composable
fun PropertiesCard(state: HomeUiState, onPropertyClick: (String, String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(shape = RoundedCornerShape(8.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
            ) {
                if (state.userId.isNotBlank()) {
                    PropertyData(label = "User ID", value = state.userId) {
                        onPropertyClick("User ID", state.userId)
                    }
                }
                if (state.placementId.isNotBlank()) {
                    PropertyData(label = "Placement ID", value = state.placementId) {
                        onPropertyClick("Placement ID", state.placementId)
                    }
                }
                if (state.presentationId.isNotBlank()) {
                    PropertyData(label = "Presentation ID", value = state.presentationId) {
                        onPropertyClick("Presentation ID", state.presentationId)
                    }
                }
                if (state.contentId.isNotBlank()) {
                    PropertyData(label = "Content ID", value = state.contentId) {
                        onPropertyClick("Content ID", state.contentId)
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyData(label: String, value: String, onPropertyClick: () -> Unit) {
    Column {
        Text(text = label, fontSize = 12.sp, color = Color.White)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.clickable { onPropertyClick() },
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Image(
                painter = painterResource(id = R.drawable.ic_copy),
                contentDescription = "save to clipboard",
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun RestoreProgressIndicator() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = Purple100,
            strokeWidth = 12.dp,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.size(20.dp))
        Text(
            text = "Restoring...",
            color = Purple100,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )
    }
}
