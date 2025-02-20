package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.ui.theme.ToDolistTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab

// Data Class for Task
data class Task(
    val id: Int,
    var title: String,
    var completed: Boolean = false,
    var category: String
)

// ViewModel to Manage Tasks
class TaskViewModel : ViewModel() {
    var tasks by mutableStateOf(listOf<Task>())
        private set

    private var nextId = 0

    fun addTask(title: String, category: String) {
        if (title.isNotEmpty()) {
            tasks = tasks + Task(nextId++, title, false, category)
        }
    }

    fun deleteTask(task: Task) {
        tasks = tasks.filter { it.id != task.id }
    }

    fun toggleTaskCompletion(task: Task) {
        tasks = tasks.map {
            if (it.id == task.id) it.copy(completed = !it.completed) else it
        }
    }

    fun editTask(task: Task, newTitle: String) {
        tasks = tasks.map {
            if (it.id == task.id) it.copy(title = newTitle) else it
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToDolistTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TaskScreen()
                }
            }
        }
    }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel = viewModel()) {
    var taskTitle by remember { mutableStateOf(TextFieldValue()) }
    var selectedCategory by remember { mutableStateOf("Work") }
    var selectedTab by remember { mutableStateOf(0) }
    val categories = listOf("Work", "Personal", "Urgent")
    val filteredTasks = viewModel.tasks.filter { it.category == categories[selectedTab] }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = taskTitle,
            onValueChange = { taskTitle = it },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenuCategory(selectedCategory) { selectedCategory = it }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.addTask(taskTitle.text, selectedCategory)
                taskTitle = TextFieldValue()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab) {
            categories.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        LazyColumn {
            items(filteredTasks) { task ->
                TaskItem(task, viewModel)
            }
        }
    }
}

@Composable
fun DropdownMenuCategory(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Work", "Personal", "Urgent")

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(text = selectedCategory)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, viewModel: TaskViewModel) {
    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(TextFieldValue(task.title)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(if (task.completed) Color.LightGray else Color.White, RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = { viewModel.toggleTaskCompletion(task) }
                )

                if (isEditing) {
                    TextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = task.title,
                        modifier = Modifier.weight(1f)
                    )
                }

                IconButton(onClick = { isEditing = !isEditing }) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Edit, contentDescription = "Edit")
                }

                IconButton(onClick = { viewModel.deleteTask(task) }) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            if (isEditing) {
                Button(onClick = {
                    viewModel.editTask(task, editedTitle.text)
                    isEditing = false
                }) {
                    Text("Save")
                }
            }

            Text("Category: ${task.category}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskScreenPreview() {
    ToDolistTheme {
        TaskScreen()
    }
}
