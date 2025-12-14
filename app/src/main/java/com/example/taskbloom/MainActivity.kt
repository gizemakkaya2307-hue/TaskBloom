package com.example.taskbloom   // <-- BURAYI kendi paket adına göre düzelt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

enum class TaskCategory(val label: String, val color: Color) {
    SCHOOL("Okul", Color(0xFF7C8CFF)),
    HEALTH("Sağlık", Color(0xFF4FC3A1)),
    SELF("Kendim için", Color(0xFFFF9BB0))
}

data class TaskItem(
    val id: Int,
    val title: String,
    val category: TaskCategory,
    val focusMinutes: Int,
    val isDone: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF7C8CFF),
                    secondary = Color(0xFFFF9BB0)
                )
            ) {
                TaskBloomApp()
            }
        }
    }
}

@Composable
fun TaskBloomApp() {
    // örnek birkaç görevle başlat
    var tasks by remember {
        mutableStateOf(
            listOf(
                TaskItem(1, "AYT Matematik 2 test", TaskCategory.SCHOOL, 40, false),
                TaskItem(2, "30 dk yürüyüş", TaskCategory.HEALTH, 30, false),
                TaskItem(3, "Günlük 10 dk yazı yaz", TaskCategory.SELF, 10, true)
            )
        )
    }

    var filter by remember { mutableStateOf<TaskCategory?>(null) }
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf(TaskCategory.SCHOOL) }
    var newMinutes by remember { mutableStateOf("25") }

    val filteredTasks =
        if (filter == null) tasks else tasks.filter { it.category == filter }

    val totalCount = filteredTasks.size
    val doneCount = filteredTasks.count { it.isDone }
    val progress = if (totalCount == 0) 0f else doneCount.toFloat() / totalCount

    val today = remember {
        SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")).format(Date())
    }

    // arka plan: pastel gradient
    val background = Brush.verticalGradient(
        listOf(
            Color(0xFFFDF5FF),
            Color(0xFFF5FBFF),
            Color(0xFFFFF9F4)
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // HEADER
            Text(
                text = "TaskBloom",
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF222546)
            )
            Text(
                text = today,
                fontSize = 13.sp,
                color = Color(0xFF7B7FA8),
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // ÜST KART: bugünkü ilerleme
            ProgressCard(
                total = totalCount,
                done = doneCount,
                progress = progress
            )

            Spacer(modifier = Modifier.height(14.dp))

            // FİLTRE CHIPLERİ
            FilterRow(
                currentFilter = filter,
                onFilterChange = { filter = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // GÖREV LİSTESİ
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bu filtrede hiç görev yok. Yeni bir tane ekle ✨",
                        fontSize = 13.sp,
                        color = Color(0xFF9A9EC5)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggle = { toggled ->
                                tasks = tasks.map {
                                    if (it.id == toggled.id) it.copy(isDone = !it.isDone) else it
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // YENİ GÖREV EKLEME KARTI
            AddTaskCard(
                title = newTitle,
                onTitleChange = { newTitle = it },
                category = newCategory,
                onCategoryChange = { newCategory = it },
                minutesText = newMinutes,
                onMinutesChange = { newMinutes = it.filter { ch -> ch.isDigit() } },
                onAdd = {
                    val title = newTitle.trim()
                    val mins = newMinutes.toIntOrNull() ?: 0
                    if (title.isNotEmpty() && mins > 0) {
                        val nextId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                        val newTask = TaskItem(
                            id = nextId,
                            title = title,
                            category = newCategory,
                            focusMinutes = mins,
                            isDone = false
                        )
                        tasks = tasks + newTask
                        newTitle = ""
                        newMinutes = "25"
                        filter = null
                    }
                }
            )
        }
    }
}

// ------------- Progress Card -----------------

@Composable
fun ProgressCard(total: Int, done: Int, progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFEEF0FF),
                        Color(0xFFFFF0F5)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = "Bugünkü akışın",
            fontSize = 14.sp,
            color = Color(0xFF46486B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when {
                total == 0 -> "Henüz planlanmış görev yok."
                progress == 1f -> "Hepsi tamam! Dinlenmeyi hak ettin. 🌈"
                progress > 0.5f -> "Yolun büyük kısmını hallettin, devam! ⚡"
                else -> "Başlangıç iyi, küçük adımlar büyük etki bırakır. 🌱"
            },
            fontSize = 13.sp,
            color = Color(0xFF6A6E99)
        )
        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = Color(0xFF7C8CFF),
            trackColor = Color(0xFFD9DDFB)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$done / $total görev tamamlandı",
                fontSize = 12.sp,
                color = Color(0xFF6A6E99)
            )
        }
    }
}

// ------------- Filter Row -----------------

@Composable
fun FilterRow(
    currentFilter: TaskCategory?,
    onFilterChange: (TaskCategory?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            label = "Tümü",
            selected = currentFilter == null,
            color = Color(0xFF6063A8),
            onClick = { onFilterChange(null) },
            modifier = Modifier.weight(1f)
        )

        TaskCategory.values().forEach { category ->
            FilterChip(
                label = category.label,
                selected = currentFilter == category,
                color = category.color,
                onClick = { onFilterChange(category) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) {
        Brush.horizontalGradient(
            listOf(
                color,
                color.copy(alpha = 0.8f)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color(0xFFE6E8FF),
                Color(0xFFF5F3FF)
            )
        )
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else Color(0xFF585A8C)
        )
    }
}

// ------------- Task Card -----------------

@Composable
fun TaskCard(
    task: TaskItem,
    onToggle: (TaskItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFFFFFF))
            .clickable { onToggle(task) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // check bubble
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    if (task.isDone) task.category.color.copy(alpha = 0.85f)
                    else Color(0xFFF0F1FF)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (task.isDone) {
                Text("✓", color = Color.White, fontSize = 16.sp)
            } else {
                Text(
                    text = task.category.label.first().toString(),
                    color = task.category.color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.SemiBold,
                color = if (task.isDone) Color(0xFF9A9EC0) else Color(0xFF262845),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${task.focusMinutes} dk • ${task.category.label}",
                fontSize = 11.sp,
                color = Color(0xFF9A9EC0),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // küçük renk etiketi
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(task.category.color.copy(alpha = 0.8f))
        )
    }
}

// ------------- Add Task Card -----------------

@Composable
fun AddTaskCard(
    title: String,
    onTitleChange: (String) -> Unit,
    category: TaskCategory,
    onCategoryChange: (TaskCategory) -> Unit,
    minutesText: String,
    onMinutesChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF272A4D))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Yeni görev ekle",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFF7F6FF)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text("Görev başlığı (örn. 2 paragraf esey)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9FA6FF),
                unfocusedBorderColor = Color(0xFF4B4F79),
                cursorColor = Color(0xFF9FA6FF),
                focusedContainerColor = Color(0xFF25284A),
                unfocusedContainerColor = Color(0xFF25284A),
                focusedPlaceholderColor = Color(0xFF7C80B3),
                unfocusedPlaceholderColor = Color(0xFF7C80B3),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // kategori seçimi basit pill butonlar
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TaskCategory.values().forEach { cat ->
                    val selected = cat == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (selected) cat.color
                                else Color(0xFF363A63)
                            )
                            .clickable { onCategoryChange(cat) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat.label,
                            fontSize = 11.sp,
                            color = if (selected) Color.White else Color(0xFFC3C6F0)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = minutesText,
                onValueChange = onMinutesChange,
                modifier = Modifier.width(80.dp),
                singleLine = true,
                label = { Text("dk") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF9FA6FF),
                    unfocusedBorderColor = Color(0xFF4B4F79),
                    cursorColor = Color(0xFF9FA6FF),
                    focusedContainerColor = Color(0xFF25284A),
                    unfocusedContainerColor = Color(0xFF25284A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onAdd,
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9BB0)
            )
        ) {
            Text(
                text = "Görevi ekle",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
