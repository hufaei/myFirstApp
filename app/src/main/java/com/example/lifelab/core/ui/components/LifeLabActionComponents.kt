package com.example.lifelab.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LifeLabPrimaryActionRow(
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    primaryIcon: ImageVector? = null,
    secondaryIcon: ImageVector? = null,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val resolvedSecondaryLabel = secondaryLabel?.takeIf { it.isNotBlank() }
        val resolvedSecondaryClick = onSecondaryClick

        if (resolvedSecondaryLabel == null || resolvedSecondaryClick == null) {
            Button(
                onClick = onPrimaryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = ActionMinHeight),
            ) {
                ActionButtonContent(
                    label = primaryLabel,
                    icon = primaryIcon,
                )
            }
        } else {
            val shouldStackActions = maxWidth < 360.dp ||
                primaryLabel.isWideActionLabel() ||
                resolvedSecondaryLabel.isWideActionLabel()

            if (shouldStackActions) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onPrimaryClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = ActionMinHeight),
                    ) {
                        ActionButtonContent(
                            label = primaryLabel,
                            icon = primaryIcon,
                        )
                    }
                    OutlinedButton(
                        onClick = resolvedSecondaryClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = ActionMinHeight),
                    ) {
                        ActionButtonContent(
                            label = resolvedSecondaryLabel,
                            icon = secondaryIcon,
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onPrimaryClick,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = ActionMinHeight),
                    ) {
                        ActionButtonContent(
                            label = primaryLabel,
                            icon = primaryIcon,
                        )
                    }
                    OutlinedButton(
                        onClick = resolvedSecondaryClick,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = ActionMinHeight),
                    ) {
                        ActionButtonContent(
                            label = resolvedSecondaryLabel,
                            icon = secondaryIcon,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ActionButtonContent(
    label: String,
    icon: ImageVector?,
) {
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
        text = label,
        modifier = Modifier.weight(1f, fill = false),
        textAlign = TextAlign.Center,
        softWrap = true,
    )
}

private fun String?.isWideActionLabel(): Boolean {
    val label = this ?: return false
    var weightedLength = 0
    for (character in label) {
        weightedLength += if (character.code < 128) 1 else 2
    }
    return weightedLength > 22
}

private val ActionMinHeight = 48.dp
