// app/src/main/java/com/julesmeister/sangfroid/feature/feedback/components/RevisionHistorySheet.kt

package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.domain.history.SavedRevision

private val HistorySheetBackground = Color(0xFFFAFBFD)
private val HistorySheetHeading = Color(0xFF19283B)
private val HistorySheetBody = Color(0xFF35465A)
private val HistorySheetMuted = Color(0xFF536276)
private val HistorySheetAccent = Color(0xFF28528F)
private val HistorySheetDivider = Color(0xFFE8EDF3)
private val HistorySheetHandle = Color(0xFFC4CEDA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionHistorySheet(
    revisions: List<SavedRevision>,
    onRevisionSelected: (SavedRevision) -> Unit,
    onDismiss: () -> Unit
) {
    val sortedRevisions = remember(revisions) {
        revisions.sortedWith(
            compareByDescending<SavedRevision> {
                it.versionNumber
            }.thenByDescending {
                it.createdAt
            }
        )
    }

    val latestRevisionId =
        sortedRevisions.firstOrNull()?.id

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = HistorySheetBackground,
        contentColor = HistorySheetHeading,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(
                        top = 12.dp,
                        bottom = 20.dp
                    )
                    .width(36.dp)
                    .height(4.dp),
                color = HistorySheetHandle,
                shape = RoundedCornerShape(50)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = SangfroidSpacing.CardContent
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SAVED VERSIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = HistorySheetAccent
                )

                Text(
                    text = "Revision history",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = HistorySheetHeading
                )

                Text(
                    text = buildRevisionCountLabel(
                        sortedRevisions.size
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HistorySheetMuted
                )
            }

            Spacer(
                modifier = Modifier.height(
                    SangfroidSpacing.Md
                )
            )

            HorizontalDivider(
                modifier = Modifier.padding(
                    horizontal = SangfroidSpacing.CardContent
                ),
                thickness = 0.5.dp,
                color = HistorySheetDivider
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                contentPadding = PaddingValues(
                    start = SangfroidSpacing.Md,
                    end = SangfroidSpacing.Md,
                    top = SangfroidSpacing.Md,
                    bottom = SangfroidSpacing.Section
                ),
                verticalArrangement = Arrangement.spacedBy(
                    SangfroidSpacing.Md
                )
            ) {
                if (sortedRevisions.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.padding(
                                SangfroidSpacing.Md
                            ),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No saved versions yet",
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HistorySheetHeading
                            )

                            Text(
                                text = "Save an improved answer from " +
                                        "feedback to find it here.",
                                style =
                                    MaterialTheme.typography.bodyMedium,
                                color = HistorySheetBody
                            )
                        }
                    }
                }

                items(
                    items = sortedRevisions,
                    key = { revision ->
                        revision.id
                    }
                ) { revision ->
                    RevisionHistoryItem(
                        revision = revision,
                        isLatest =
                            revision.id == latestRevisionId,
                        onClick = {
                            onRevisionSelected(revision)
                        }
                    )
                }
            }
        }
    }
}

private fun buildRevisionCountLabel(
    count: Int
): String {
    return if (count == 1) {
        "1 saved version"
    } else {
        "$count saved versions"
    }
}