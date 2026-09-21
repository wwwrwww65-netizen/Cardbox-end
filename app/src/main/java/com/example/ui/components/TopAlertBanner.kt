package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class AlertType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

data class InAppAlert(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val title: String? = null,
    val type: AlertType = AlertType.INFO,
    val durationMs: Long = 3500L
)

@Composable
fun TopAlertHost(
    alert: InAppAlert?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto dismiss after specified duration
    LaunchedEffect(alert?.id) {
        if (alert != null) {
            delay(alert.durationMs)
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .zIndex(100f),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = alert != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(animationSpec = tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(220, easing = FastOutLinearInEasing)
            ) + fadeOut(animationSpec = tween(200))
        ) {
            if (alert != null) {
                TopAlertCard(
                    alert = alert,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun TopAlertCard(
    alert: InAppAlert,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == PosDarkBackground || 
                 MaterialTheme.colorScheme.surface == PosDarkSurface

    val (accentColor, containerColor, icon) = when (alert.type) {
        AlertType.SUCCESS -> Triple(
            PosEmeraldSuccess,
            if (isDark) Color(0xFF064E3B).copy(alpha = 0.92f) else Color(0xFFECFDF5),
            Icons.Outlined.CheckCircle
        )
        AlertType.ERROR -> Triple(
            PosRedError,
            if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.92f) else Color(0xFFFEF2F2),
            Icons.Outlined.ErrorOutline
        )
        AlertType.WARNING -> Triple(
            PosAmberWarning,
            if (isDark) Color(0xFF78350F).copy(alpha = 0.92f) else Color(0xFFFFFBEB),
            Icons.Outlined.WarningAmber
        )
        AlertType.INFO -> Triple(
            PosIndigoPrimaryLight,
            if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.92f) else Color(0xFFEEF2FF),
            Icons.Outlined.Info
        )
    }

    val defaultTitle = when (alert.type) {
        AlertType.SUCCESS -> "تمت العملية بنجاح"
        AlertType.ERROR -> "تنبيه خطأ"
        AlertType.WARNING -> "ملاحظة هامة"
        AlertType.INFO -> "إشعار"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(
            1.2.dp,
            accentColor.copy(alpha = if (isDark) 0.65f else 0.45f)
        ),
        shadowElevation = if (isDark) 8.dp else 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onDismiss() }
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -15) {
                        onDismiss()
                    }
                }
            }
            .testTag("top_in_app_alert")
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon Badge
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = if (isDark) 0.35f else 0.18f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Text Content
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = alert.title ?: defaultTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = alert.message,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color.White.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Close Button
                Surface(
                    shape = CircleShape,
                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.08f),
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onDismiss() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق التنبيه",
                            tint = if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Subtle colored progress line at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(accentColor.copy(alpha = 0.7f))
            )
        }
    }
}
