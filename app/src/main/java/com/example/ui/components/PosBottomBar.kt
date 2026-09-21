package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Routes

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String,
    val isElevatedCenter: Boolean = false,
    val badgeCount: Int = 0
)

@Composable
fun PosBottomBar(
    currentRoute: String?,
    joinedNetworksCount: Int,
    ordersCount: Int,
    onNavigate: (String) -> Unit
) {
    // Exact requested order: الشبكات - البحث - الرئيسية (وسط مرتفع) - المبيعات - حسابي
    val items = listOf(
        BottomNavItem(
            route = Routes.NETWORKS,
            label = "الشبكات",
            icon = Icons.Outlined.Wifi,
            testTag = "nav_networks",
            badgeCount = joinedNetworksCount
        ),
        BottomNavItem(
            route = Routes.SEARCH_NETWORK,
            label = "البحث",
            icon = Icons.Outlined.Search,
            testTag = "nav_search"
        ),
        BottomNavItem(
            route = Routes.HOME,
            label = "الرئيسية",
            icon = Icons.Outlined.Home,
            testTag = "nav_home",
            isElevatedCenter = true
        ),
        BottomNavItem(
            route = Routes.SALES_HISTORY,
            label = "المبيعات",
            icon = Icons.Outlined.ReceiptLong,
            testTag = "nav_history",
            badgeCount = ordersCount
        ),
        BottomNavItem(
            route = Routes.PROFILE,
            label = "حسابي",
            icon = Icons.Outlined.Person,
            testTag = "nav_profile"
        )
    )

    val homeItem = items[2]
    val isHomeSelected = currentRoute == homeItem.route

    // Container with explicit height so the floating center button is completely contained inside the layout without any clipping or black bars
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bottom_navigation_bar"),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Base Navigation Surface Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                // 0. الشبكات
                StandardNavItem(
                    item = items[0],
                    isSelected = currentRoute == items[0].route,
                    onClick = { onNavigate(items[0].route) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                // 1. البحث
                StandardNavItem(
                    item = items[1],
                    isSelected = currentRoute == items[1].route,
                    onClick = { onNavigate(items[1].route) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                // Center placeholder space for Home label with click area
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigate(homeItem.route) }
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = homeItem.label,
                        fontSize = 11.5.sp,
                        fontWeight = if (isHomeSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isHomeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 7.dp)
                    )
                }

                // 3. المبيعات
                StandardNavItem(
                    item = items[3],
                    isSelected = currentRoute == items[3].route,
                    onClick = { onNavigate(items[3].route) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                // 4. حسابي
                StandardNavItem(
                    item = items[4],
                    isSelected = currentRoute == items[4].route,
                    onClick = { onNavigate(items[4].route) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                }
            }
        }

        // Prominent Floating Center Home Button protruding cleanly above the bottom bar
        ElevatedCenterNavItem(
            isSelected = isHomeSelected,
            item = homeItem,
            onClick = { onNavigate(homeItem.route) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, 0) {
                        placeable.placeRelative(0, -placeable.height / 2 - 2)
                    }
                }
        )
    }
}

@Composable
private fun StandardNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "iconColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "textColor"
    )

    Column(
        modifier = modifier
            .testTag(item.testTag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 28.dp),
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BadgedBox(
            badge = {
                if (item.badgeCount > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        else Color.Transparent
                    )
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun ElevatedCenterNavItem(
    isSelected: Boolean,
    item: BottomNavItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(stiffness = 300f),
        label = "scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .testTag(item.testTag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 10.dp,
            border = androidx.compose.foundation.BorderStroke(
                3.5.dp,
                MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.size(54.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
