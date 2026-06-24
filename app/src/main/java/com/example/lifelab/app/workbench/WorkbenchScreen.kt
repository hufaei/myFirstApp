package com.example.lifelab.app.workbench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lifelab.core.ui.component.SectionHeader
import com.example.lifelab.core.ui.component.SummaryMetric

enum class WorkbenchTab {
    Tasks,
    Habits,
}

@Composable
fun WorkbenchScreen(
    selectedTab: WorkbenchTab,
    summary: WorkbenchSummary,
    contentPadding: PaddingValues,
    onSelectTab: (WorkbenchTab) -> Unit,
    tasksContent: @Composable () -> Unit,
    habitsContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(
            title = "Workbench",
            subtitle = "Plan work and keep streaks alive",
        )
        WorkbenchSummaryRow(summary = summary)
        WorkbenchSegments(
            selectedTab = selectedTab,
            onSelectTab = onSelectTab,
        )
        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == WorkbenchTab.Tasks) {
                tasksContent()
            } else {
                habitsContent()
            }
        }
    }
}

@Composable
private fun WorkbenchSummaryRow(summary: WorkbenchSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryMetric(
                value = summary.activeTasks.toString(),
                label = "Active tasks",
                helper = summary.completedLabel,
                modifier = Modifier.weight(1f),
            )
            SummaryMetric(
                value = "${summary.checkedInToday}/${summary.totalHabits}",
                label = "Habits today",
                helper = summary.streakLabel,
                modifier = Modifier.weight(1f),
            )
        }
        SummaryMetric(
            value = summary.activeReminders.toString(),
            label = "Reminder signals",
            helper = summary.reminderLabel,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkbenchSegments(
    selectedTab: WorkbenchTab,
    onSelectTab: (WorkbenchTab) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        WorkbenchTab.entries.forEachIndexed { index, tab ->
            SegmentedButton(
                selected = selectedTab == tab,
                onClick = { onSelectTab(tab) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = WorkbenchTab.entries.size,
                ),
            ) {
                Text(
                    text = tab.name,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
