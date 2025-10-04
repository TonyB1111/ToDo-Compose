package com.example.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todo.ui.theme.TodoTheme
import kotlin.random.Random

// Data for each row
data class TodoItem(
    val id: Long = Random.nextLong(),
    val label: String,
    val isDone: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodoApp(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TodoApp(modifier: Modifier = Modifier) {
    // Persist list across rotation + start with 4 sample items
    val items = rememberSaveable(
        saver = listSaver(
            save = { list -> list.map { listOf(it.id, it.label, it.isDone) } },
            restore = { saved ->
                mutableStateListOf<TodoItem>().apply {
                    saved.forEach { row ->
                        add(
                            TodoItem(
                                id = row[0] as Long,
                                label = row[1] as String,
                                isDone = row[2] as Boolean
                            )
                        )
                    }
                }
            }
        )
    ) {
        mutableStateListOf(
            // Active items (unchecked)
            TodoItem(label = "Learn Java", isDone = false),
            TodoItem(label = "Complete Math homework", isDone = false),
            // Completed items (checked)
            TodoItem(label = "Complete Mini Project", isDone = true),
            TodoItem(label = "Buy groceries", isDone = true)
        )
    }

    // Persist input text across rotation
    var newLabel by rememberSaveable { mutableStateOf("") }

    val trimmed = newLabel.trim()
    val tooLong = trimmed.length > 80
    val isValid = trimmed.isNotEmpty() && !tooLong

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "TODO List",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        // Input row (state hoisted)
        TodoInputRow(
            text = newLabel,
            onTextChange = { newLabel = it },
            onAdd = {
                if (isValid) {
                    items.add(TodoItem(label = trimmed))
                    newLabel = ""
                }
            },
            isValid = isValid,
            validationMsg = when {
                trimmed.isEmpty() -> "Please enter something."
                tooLong -> "Keep it under 80 characters."
                else -> null
            }
        )

        Divider()

        // Active section (header only when non-empty)
        val activeItems = items.filter { !it.isDone }
        if (activeItems.isNotEmpty()) {
            SectionHeader("Items")
            TodoList(
                items = activeItems,
                onToggle = { id, checked ->
                    val i = items.indexOfFirst { it.id == id }
                    if (i != -1) items[i] = items[i].copy(isDone = checked)
                },
                onDelete = { id -> items.removeAll { it.id == id } }
            )
        } else {
            EmptyHint("No items yet.")
        }

        Divider()

        // Completed section (header only when non-empty)
        val doneItems = items.filter { it.isDone }
        if (doneItems.isNotEmpty()) {
            SectionHeader("Completed Items")
            TodoList(
                items = doneItems,
                onToggle = { id, checked ->
                    val i = items.indexOfFirst { it.id == id }
                    if (i != -1) items[i] = items[i].copy(isDone = checked)
                },
                onDelete = { id -> items.removeAll { it.id == id } }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { items.removeAll { it.isDone } }) {
                    Text("Clear Completed")
                }
            }
        } else {
            EmptyHint("No completed items yet.")
        }
    }
}

@Composable
fun TodoInputRow(
    text: String,
    onTextChange: (String) -> Unit,
    onAdd: () -> Unit,
    isValid: Boolean,
    validationMsg: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Add a task…") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = onAdd, enabled = isValid) { Text("Add") }
        }
        if (validationMsg != null) {
            Text(
                text = validationMsg,
                color = Color(0xFFB00020),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun TodoList(
    items: List<TodoItem>,
    onToggle: (id: Long, checked: Boolean) -> Unit,
    onDelete: (id: Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 360.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            TodoRow(item = item, onToggle = onToggle, onDelete = onDelete)
        }
    }
}

@Composable
fun TodoRow(
    item: TodoItem,
    onToggle: (id: Long, checked: Boolean) -> Unit,
    onDelete: (id: Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = item.isDone,
            onCheckedChange = { checked -> onToggle(item.id, checked) }
        )
        val style = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
        Text(
            text = item.label,
            modifier = Modifier.weight(1f),
            textDecoration = style
        )
        IconButton(onClick = { onDelete(item.id) }) {
            Text("✕")
        }    }
}

@Preview
@Composable
fun TodoPreview() {
    TodoTheme {
        TodoApp()
    }
}
