package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DekotubeRepository
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun MonetizationScreen(
    repository: DekotubeRepository,
    modifier: Modifier = Modifier
) {
    var longViews by remember { mutableFloatStateOf(250000f) }
    var longRpm by remember { mutableFloatStateOf(3.80f) }
    var shortsViews by remember { mutableFloatStateOf(1500000f) }
    var shortsRpm by remember { mutableFloatStateOf(0.12f) }
    var subscribers by remember { mutableFloatStateOf(45000f) }

    val calcResult = remember(longViews, longRpm, shortsViews, shortsRpm, subscribers) {
        repository.calculateMonetization(
            longViews = longViews.toLong(),
            longRpm = longRpm.toDouble(),
            shortsViews = shortsViews.toLong(),
            shortsRpm = shortsRpm.toDouble(),
            subscribers = subscribers.toInt()
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZincBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 80.dp)
            .testTag("monetization_calculator_screen")
    ) {
        Row(
            modifier = Modifier.padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = null,
                tint = SleekGreen,
                modifier = Modifier.size(26.dp)
            )
            Column {
                Text(
                    text = "Monetization Calculator",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZincTextPrimary
                )
                Text(
                    text = "Live revenue modeling • 55/45 split & Shorts creator pool",
                    fontSize = 12.sp,
                    color = ZincTextSecondary
                )
            }
        }

        // BIG EARNINGS RESULT CARD (rounded-3xl bg-zinc-900 border border-zinc-800)
        Surface(
            color = ZincSurface,
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "ESTIMATED MONTHLY CREATOR PAYOUT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ZincTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format(Locale.US, "%,.2f", calcResult.monthlyEarnings)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekGreen
                )
                Text(
                    text = "Projected Annual: $${String.format(Locale.US, "%,.2f", calcResult.yearlyEarnings)}",
                    fontSize = 13.sp,
                    color = ZincTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Threshold progress bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Monthly Payout ($100 Min)",
                        fontSize = 12.sp,
                        color = ZincTextSecondary
                    )
                    Text(
                        text = if (calcResult.payoutEligible) "Ready for Deposit" else "${calcResult.progressToPayout.toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (calcResult.payoutEligible) SleekGreen else SleekRedAccent
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { calcResult.progressToPayout / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SleekGreen,
                    trackColor = ZincElevated
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ZincBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Revenue Breakdown Rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Long-Form Video Ads (55%):", fontSize = 13.sp, color = ZincTextSecondary)
                    Text("$${String.format(Locale.US, "%,.2f", calcResult.longFormEarnings)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ZincTextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Shorts Creator Pool (45%):", fontSize = 13.sp, color = ZincTextSecondary)
                    Text("$${String.format(Locale.US, "%,.2f", calcResult.shortsEarnings)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ZincTextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Channel Memberships (70%):", fontSize = 13.sp, color = ZincTextSecondary)
                    Text("$${String.format(Locale.US, "%,.2f", calcResult.membershipEarnings)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ZincTextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // INTERACTIVE SLIDERS SECTION
        Text(
            text = "Adjust Channel Metrics",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = ZincTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 1. Long-form Views
        SliderCard(
            title = "Monthly Long-Form Video Views",
            valueText = formatViews(longViews.toLong()),
            value = longViews,
            onValueChange = { longViews = it },
            range = 10000f..5000000f,
            steps = 49
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Long-form RPM
        SliderCard(
            title = "Long-Form RPM (Revenue per 1,000 Views)",
            valueText = "$${String.format(Locale.US, "%.2f", longRpm)}",
            value = longRpm,
            onValueChange = { longRpm = it },
            range = 1.0f..15.0f,
            steps = 28
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Shorts Views
        SliderCard(
            title = "Monthly Shorts Views",
            valueText = formatViews(shortsViews.toLong()),
            value = shortsViews,
            onValueChange = { shortsViews = it },
            range = 50000f..25000000f,
            steps = 49
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Shorts RPM
        SliderCard(
            title = "Shorts Creator Pool RPM",
            valueText = "$${String.format(Locale.US, "%.2f", shortsRpm)}",
            value = shortsRpm,
            onValueChange = { shortsRpm = it },
            range = 0.05f..0.30f,
            steps = 25
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Active Subscribers
        SliderCard(
            title = "Channel Subscribers",
            valueText = formatViews(subscribers.toLong()),
            value = subscribers,
            onValueChange = { subscribers = it },
            range = 1000f..500000f,
            steps = 49
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Partner Program Requirements
        Surface(
            color = ZincSurface,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dekotube Partner Program Eligibility",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZincTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                EligibilityItem("1,000 Subscribers", subscribers >= 1000)
                EligibilityItem("4,000 Watch Hours or 10M Shorts Views", longViews >= 50000 || shortsViews >= 10000000)
                EligibilityItem("Compliant with Platform Rules & Guidelines", true)
            }
        }
    }
}

@Composable
fun SliderCard(
    title: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    steps: Int
) {
    Surface(
        color = ZincSurface,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, fontSize = 12.sp, color = ZincTextSecondary)
                Text(text = valueText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ZincTextPrimary)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = SleekRed,
                    activeTrackColor = SleekRed,
                    inactiveTrackColor = ZincElevated
                )
            )
        }
    }
}

@Composable
fun EligibilityItem(label: String, passed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (passed) SleekGreen else ZincBorderSubtle,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (passed) ZincTextPrimary else ZincTextSecondary
        )
    }
}

