package com.purchasely.samplev2.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIos
import com.purchasely.samplev2.presentation.theme.Brand
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CommonScreenHeader(title: String, onBackClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Brand.colors.hero)
            .padding(top = 10.dp, bottom = 10.dp)
            .statusBarsPadding()
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Outlined.ArrowBackIos,
                contentDescription = "$title back",
                tint = Brand.colors.onHero,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Brand.colors.onHero,
            modifier = Modifier.weight(1f)
        )
    }
}