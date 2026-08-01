package com.example.zenmoney.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenmoney.data.CategorySum

val ChartColors = listOf(
    Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFFE53935),
    Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFF00BCD4),
    Color(0xFFFFEB3B), Color(0xFF795548)
)

@Composable
fun CategoryChart(data: List<CategorySum>) {
    if (data.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("Нет данных", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
        return
    }

    val total = data.sumOf { it.total }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        data.forEachIndexed { index, item ->
            val percentage = (item.total / total).toFloat()
            val color = ChartColors[index % ChartColors.size]

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.category,
                    modifier = Modifier.width(100.dp),
                    fontSize = 13.sp
                )
                Box(modifier = Modifier.weight(1f).height(20.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            topLeft = Offset(0f, 0f),
                            size = Size(size.width, size.height)
                        )
                        drawRect(
                            color = color,
                            topLeft = Offset(0f, 0f),
                            size = Size(size.width * percentage, size.height)
                        )
                    }
                }
                Text(
                    text = "%.0f%%".format(percentage * 100),
                    modifier = Modifier.width(50.dp).padding(start = 8.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
