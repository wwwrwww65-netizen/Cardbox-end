package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PosAmberWarning
import com.example.ui.theme.PosEmeraldSuccess

data class OnboardingBadge(
    val label: String,
    val icon: ImageVector
)

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val badges: List<OnboardingBadge>,
    val highlightColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val pages = listOf(
        OnboardingPageData(
            title = "طباعة وتوزيع كروت الشبكات",
            subtitle = "إدارة باقات شبكات الـ Wi-Fi والطباعة الحرارية السريعة",
            description = "أداة متكاملة لموزعي نقاط البيع لاستخراج وطباعة كروت الإنترنت لشبكات المايكروتك المحلية عبر البلوتوث بضغطة زر وبدون تعقيد.",
            icon = Icons.Outlined.Print,
            badges = listOf(
                OnboardingBadge("طباعة فورية", Icons.Outlined.FlashOn),
                OnboardingBadge("المايكروتك", Icons.Outlined.Wifi),
                OnboardingBadge("فواتير معتمدة", Icons.Outlined.Receipt)
            ),
            highlightColor = MaterialTheme.colorScheme.primary
        ),
        OnboardingPageData(
            title = "تغذية المحفظة وتسديد الحسابات",
            subtitle = "شحن الرصيد وسداد مستحقات الشبكات بأعلى أمان",
            description = "إمكانية إيداع وتغذية محفظتك الإلكترونية وسداد كروت الشبكات عبر البنوك والمحافظ اليمنية (جيب، الكريمي، ون كاش، جوالي) بسرعة فائقة.",
            icon = Icons.Outlined.AccountBalanceWallet,
            badges = listOf(
                OnboardingBadge("محفظة كارد بوكس", Icons.Outlined.CreditCard),
                OnboardingBadge("التحويل المباشر", Icons.Outlined.SyncAlt),
                OnboardingBadge("حماية مشفرة", Icons.Outlined.Shield)
            ),
            highlightColor = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPageData(
            title = "إدارة المبيعات والإشعارات الحية",
            subtitle = "تقارير يومية متكاملة وتنبيهات مستمرة لعملياتك",
            description = "متابعة دقيقة لكل فواتير المبيعات، إشعارات حية بقبول طلبات الانضمام للشبكات وتعبئة الرصيد، وسجل أرشيفي كامل بجميع المبيعات.",
            icon = Icons.Outlined.Analytics,
            badges = listOf(
                OnboardingBadge("تقارير تفصيلية", Icons.Outlined.BarChart),
                OnboardingBadge("تنبيهات فورية", Icons.Outlined.NotificationsActive),
                OnboardingBadge("أرشيف دائم", Icons.Outlined.Folder)
            ),
            highlightColor = PosEmeraldSuccess
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "كارد بوكس POS",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    if (currentPage < pages.size - 1) {
                        TextButton(
                            onClick = onSkip,
                            modifier = Modifier.testTag("btn_onboarding_skip")
                        ) {
                            Text(
                                text = "تخطي",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Animated Slide Content
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(animationSpec = tween(400)) { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally(animationSpec = tween(400)) { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally(animationSpec = tween(400)) { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally(animationSpec = tween(400)) { width -> width } + fadeOut()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 500.dp),
                label = "onboardingSlide"
            ) { pageIdx ->
                val page = pages[pageIdx]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Illustration Card Banner with responsive layout
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 250.dp),
                        shape = RoundedCornerShape(32.dp),
                        color = page.highlightColor.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.2.dp,
                            page.highlightColor.copy(alpha = 0.28f)
                        ),
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Background decorative glow
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(CircleShape)
                                    .background(page.highlightColor.copy(alpha = 0.16f))
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 8.dp,
                                    border = androidx.compose.foundation.BorderStroke(
                                        2.dp,
                                        page.highlightColor.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.size(88.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = page.icon,
                                            contentDescription = null,
                                            tint = page.highlightColor,
                                            modifier = Modifier.size(46.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Logo Brand Label
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_app_logo),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "CardBox POS",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title
                    Text(
                        text = page.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle
                    Text(
                        text = page.subtitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = page.highlightColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description Body
                    Text(
                        text = page.description,
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Badges List
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        page.badges.forEach { badge ->
                            Surface(
                                color = page.highlightColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    page.highlightColor.copy(alpha = 0.25f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = badge.icon,
                                        contentDescription = null,
                                        tint = page.highlightColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = badge.label,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = page.highlightColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation & Dots Indicator
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.indices.forEach { index ->
                        val isSelected = index == currentPage
                        val targetWidth: Dp = if (isSelected) 32.dp else 8.dp
                        val widthAnim by animateDpAsState(
                            targetValue = targetWidth,
                            animationSpec = tween(300),
                            label = "dotWidth"
                        )
                        val color = if (isSelected) pages[currentPage].highlightColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(widthAnim)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Next / Previous / Finish Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    if (currentPage > 0) {
                        OutlinedButton(
                            onClick = { currentPage-- },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("btn_onboarding_prev")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = "السابق",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("السابق", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Next / Finish Button
                    Button(
                        onClick = {
                            if (currentPage < pages.size - 1) {
                                currentPage++
                            } else {
                                onFinish()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = pages[currentPage].highlightColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("btn_onboarding_next")
                    ) {
                        Text(
                            text = if (currentPage == pages.size - 1) "متابعة وتفعيل الأذونات" else "التالي",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (currentPage == pages.size - 1) Icons.Outlined.CheckCircle else Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Next",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
