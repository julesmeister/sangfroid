package com.julesmeister.sangfroid.feature.saved.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SavedQuestionsFilteredEmptyState(
    selectedCategoryCount: Int,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    horizontal = 24.dp
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        color =
                            MaterialTheme
                                .colorScheme
                                .surfaceVariant
                                .copy(
                                    alpha = 0.46f
                                ),
                        shape =
                            RoundedCornerShape(
                                22.dp
                            )
                    )
                    .padding(
                        horizontal = 22.dp,
                        vertical = 20.dp
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            Text(
                text =
                    "No matching saved questions",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.ExtraBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface,
                textAlign =
                    TextAlign.Center
            )

            Text(
                text =
                    if (
                        selectedCategoryCount == 1
                    ) {
                        "None of your saved questions match the selected category."
                    } else {
                        "None of your saved questions match the " +
                                "$selectedCategoryCount selected categories."
                    },
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                textAlign =
                    TextAlign.Center
            )

            Text(
                text =
                    "Clear filters",
                modifier =
                    Modifier
                        .clickable(
                            role =
                                Role.Button,
                            onClick =
                                onClearFilters
                        )
                        .padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        ),
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}