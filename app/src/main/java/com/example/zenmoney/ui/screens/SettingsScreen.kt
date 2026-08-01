package com.example.zenmoney.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.zenmoney.utils.RequestSmsPermission
import com.example.zenmoney.utils.hasSmsPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var smsEnabled by remember { mutableStateOf(false) }
    var showPermissionRequest by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Переключатель SMS ──
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text("Авто-учёт по SMS", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                            Text(
                                "Сбербанк, Райффайзен и др.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Switch(
                        checked = smsEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (hasSmsPermission(context)) {
                                    smsEnabled = true
                                    Toast.makeText(context, "SMS-учёт включён", Toast.LENGTH_SHORT).show()
                                } else {
                                    showPermissionRequest = true
                                }
                            } else {
                                smsEnabled = false
                                Toast.makeText(context, "SMS-учёт выключен", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // ── О приложении ──
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("О приложении", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ZenMoney Clone v1.0")
                    Text("Учёт расходов и доходов")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Приложение автоматически распознаёт SMS от Сбербанка и Райффайзен " +
                        "и добавляет операции в учёт. Данные хранятся локально.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // ── Функции ──
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Функции", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Ручной учёт доходов и расходов")
                    Text("• Авто-учёт из SMS (Сбербанк, Райффайзен)")
                    Text("• Категории операций")
                    Text("• Аналитика расходов")
                    Text("• Бюджетирование")
                    Text("• Локальное хранение данных (Room)")
                }
            }
        }
    }

    // Запрос разрешений
    if (showPermissionRequest) {
        RequestSmsPermission(
            onGranted = {
                smsEnabled = true
                showPermissionRequest = false
                Toast.makeText(context, "Разрешение получено! SMS-учёт включён", Toast.LENGTH_SHORT).show()
            },
            onDenied = {
                smsEnabled = false
                showPermissionRequest = false
                Toast.makeText(context, "Без разрешения SMS не работает", Toast.LENGTH_LONG).show()
            }
        )
    }
}
