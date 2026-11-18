// app/src/main/java/com/julesmeister/sangfroid/feature/feedback/components/RevisionDetailSheet.kt

package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.domain.history.RevisionSource
import com.julesmeister.sangfroid.domain.history.SavedRevision
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private val DetailSheetBackground = Color(0xFFFAFBFD)
private val DetailSheetHeading = Color(0xFF19283B)
private val DetailSheetBody = Color(0xFF35465A)
private val DetailSheetMuted = Color(0xFF536276)
private val DetailSheetAccent = Color(0xFF28528F)
private val DetailSheetHandle = Color(0xFFC4CEDA)

private val DetailContentBackground = Color(0xFFF1F4F8)
private val DetailContentBorder = Color(0xFFE3E9F1)
private val DetailSourceBackground = Color(0xFFEDF3FD)

private val DetailErrorBackground = Color(0xFFFFF0ED)
private val DetailErrorContent = Color(0xFF942F26)
private val DetailErrorBorder = Color(0xFFF1D5D0)

private val DetailDisabledBackground = Color(0xFFE3E9F1)
private val DetailDisabledContent = Color(0xFF586779)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionDetailSheet(
    revision: SavedRevision,
    isDeleting: Boolean,
    errorMessage: String?,
    onUseRevision: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = {
            if (!isDeleting) {
                onDismiss()
            }
        },
        containerColor = DetailSheetBackground,
        contentColor = DetailSheetHeading,
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
                color = DetailSheetHandle,
                shape = RoundedCornerShape(50)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(
                    rememberScrollState()
                )
                .navigationBarsPadding()
                .padding(
                    horizontal = SangfroidSpacing.CardContent
                ),
            verticalArrangement = Arrangement.spacedBy(
                SangfroidSpacing.Md
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SAVED ANSWER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = DetailSheetAccent
                )

                Text(
                    text = "Version ${revision.versionNumber}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DetailSheetHeading
                )

                Surface(
                    color = DetailSourceBackground,
                    contentColor = DetailSheetAccent,
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = revision.source.displayLabel(),
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = DetailSheetAccent
                    )
                }

                Text(
                    text = formatRevisionDetailDate(
                        revision.createdAt
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = DetailSheetMuted
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DetailContentBackground,
                contentColor = DetailSheetBody,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = BorderStroke(
                    width = 0.5.dp,
                    color = DetailContentBorder
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        SangfroidSpacing.CardContent
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "YOUR SAVED REVISION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = DetailSheetMuted
                    )

                    SelectionContainer {
                        Text(
                            text = revision.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = DetailSheetBody
                        )
                    }
                }
            }

            errorMessage
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?.let { message ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = DetailErrorBackground,
                        contentColor = DetailErrorContent,
                        tonalElevation = 0.dp,
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = DetailErrorBorder
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(
                                SangfroidSpacing.Md
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DetailErrorContent
                        )
                    }
                }

            Button(
                onClick = onUseRevision,
                enabled = !isDeleting,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailSheetAccent,
                    contentColor = Color.White,
                    disabledContainerColor =
                        DetailDisabledBackground,
                    disabledContentColor =
                        DetailDisabledContent
                )
            ) {
                Text(
                    text = "Use as next answer",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onDelete,
                enabled = !isDeleting,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = if (isDeleting) {
                        DetailContentBorder
                    } else {
                        DetailErrorBorder
                    }
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = DetailErrorContent,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = DetailDisabledContent
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = DetailDisabledContent,
                            strokeWidth = 1.5.dp
                        )
                    }

                    Text(
                        text = if (isDeleting) {
                            "Deleting…"
                        } else {
                            "Delete revision"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = DetailSheetMuted,
                    disabledContentColor = DetailDisabledContent
                )
            ) {
                Text(
                    text = "Back to revision history",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(
                modifier = Modifier.height(
                    SangfroidSpacing.Section
                )
            )
        }
    }
}

private fun RevisionSource.displayLabel(): String {
    return when (this) {
        RevisionSource.QWEN_EXAMPLE -> "Qwen example"
        RevisionSource.USER_EDITED -> "User edited"
        RevisionSource.REGENERATED -> "Regenerated"
    }
}

private fun formatRevisionDetailDate(
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