// app/src/main/java/com/julesmeister/sangfroid/feature/feedback/components/RevisionHistoryItem.kt

package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.domain.history.SavedRevision
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private val RevisionHeading = Color(0xFF19283B)
private val RevisionBody = Color(0xFF35465A)
private val RevisionMuted = Color(0xFF536276)
private val RevisionAccent = Color(0xFF28528F)
private val RevisionLatestAccent = Color(0xFF1B6254)

private val RevisionBackground = Color(0xFFFAFBFD)
private val RevisionLatestBackground = Color(0xFFF2F7FD)

private val RevisionBorder = Color(0xFFE6EBF2)
private val RevisionLatestBorder = Color(0xFFD8E5F5)
private val RevisionLatestBadgeBackground = Color(0xFFE7F4ED)

@Composable
fun RevisionHistoryItem(
    revision: SavedRevision,
    isLatest: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = if (isLatest) {
            RevisionLatestBackground
        } else {
            RevisionBackground
        },
        contentColor = RevisionHeading,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 0.5.dp,
            color = if (isLatest) {
                RevisionLatestBorder
            } else {
                RevisionBorder
            }
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(
                SangfroidSpacing.Md
            ),
            verticalArrangement = Arrangement.spacedBy(
                SangfroidSpacing.Sm
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Version ${revision.versionNumber}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevisionHeading
                )

                if (isLatest) {
                    LatestRevisionBadge()
                }
            }

            Text(
                text = formatRevisionDate(
                    revision.createdAt
                ),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = RevisionMuted
            )

            Text(
                text = revision.content,
                style = MaterialTheme.typography.bodyMedium,
                color = RevisionBody,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Open version",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevisionAccent
                )

                Text(
                    text = "›",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = RevisionAccent
                )
            }
        }
    }
}

@Composable
private fun LatestRevisionBadge() {
    Surface(
        color = RevisionLatestBadgeBackground,
        contentColor = RevisionLatestAccent,
        shape = RoundedCornerShape(50),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(RevisionLatestAccent)
            )

            Text(
                text = "LATEST",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = RevisionLatestAccent
            )
        }
    }
}

private fun formatRevisionDate(
    timestamp: Long
): String {
    return DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT,
        Locale.getDefault()
    ).format(
        Date(timestamp)
    )
}