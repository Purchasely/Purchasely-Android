package com.purchasely.samplev2.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CommonScreenHeader(title: String, onBackClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primary)
            .padding(vertical = 10.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Outlined.ArrowBackIos,
                contentDescription = "$title back",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}