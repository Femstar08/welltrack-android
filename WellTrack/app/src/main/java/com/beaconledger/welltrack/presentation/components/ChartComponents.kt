package com.beaconledger.welltrack.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

// Animated Donut Chart for nutrition breakdown
@Composable
fun AnimatedDonutChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    centerText: String = "",
    centerSubtext: String = "",
    strokeWidth: Float = 40f,
    animationDuration: Int = 1500
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    val density = LocalDensity.current
    
    val animatedValues = data.map { chartData ->
        val animatedValue by animateFloatAsState(
            targetValue = chartData.value,
            animationSpec = tween(
                durationMillis = animationDuration,
                easing = EaseInOutCubic
            ),
            label = "chart_value"
        )
        animatedValue
    }
    
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.width - strokeWidth) / 2
            var startAngle = -90f
            
            animatedValues.forEachIndexed { index, value ->
                val sweepAngle = (value / total) * 360f
                
                drawArc(
                    color = data[index].color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    ),
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(center.x - radius, center.y - radius)
                )
                
                startAngle += sweepAngle
            }
        }
        
        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (centerText.isNotEmpty()) {
                Text(
                    text = centerText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (centerSubtext.isNotEmpty()) {
                Text(
                    text = centerSubtext,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Animated Bar Chart
@Composable
fun AnimatedBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    maxValue: Float? = null,
    showValues: Boolean = true,
    animationDuration: Int = 1000
) {
    val maxVal = maxValue ?: data.maxOfOrNull { it.value } ?: 1f
    
    Column(modifier = modifier) {
        data.forEach { barData ->
            val animatedHeight by animateFloatAsState(
                targetValue = barData.value / maxVal,
                animationSpec = tween(
                    durationMillis = animationDuration,
                    easing = EaseOutCubic
                ),
                label = "bar_height"
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = barData.label,
                    modifier = Modifier.width(80.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedHeight)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        barData.color,
                                        barData.color.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                }
                
                if (showValues) {
                    Text(
                        text = "${barData.value.toInt()}",
                        modifier = Modifier.width(40.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

// Line Chart for trends
@Composable
fun AnimatedLineChart(
    data: List<LineChartData>,
    modifier: Modifier = Modifier,
    showPoints: Boolean = true,
    showGrid: Boolean = true,
    animationDuration: Int = 2000
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = EaseInOutCubic
        ),
        label = "line_progress"
    )
    
    if (data.isEmpty()) return
    
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    val minValue = data.minOfOrNull { it.value } ?: 0f
    val valueRange = maxValue - minValue
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1).coerceAtLeast(1)
        
        // Draw grid
        if (showGrid) {
            val gridColor = Color.Gray.copy(alpha = 0.3f)
            
            // Horizontal grid lines
            for (i in 0..4) {
                val y = height * i / 4
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Vertical grid lines
            for (i in data.indices step (data.size / 5).coerceAtLeast(1)) {
                val x = i * stepX
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        
        // Draw line
        val path = Path()
        val animatedDataSize = (data.size * animatedProgress).toInt().coerceAtLeast(1)
        
        data.take(animatedDataSize).forEachIndexed { index, point ->
            val x = index * stepX
            val y = height - ((point.value - minValue) / valueRange) * height
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        // Draw the line with gradient
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF4CAF50),
                    Color(0xFF2196F3)
                )
            ),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        
        // Draw points
        if (showPoints) {
            data.take(animatedDataSize).forEachIndexed { index, point ->
                val x = index * stepX
                val y = height - ((point.value - minValue) / valueRange) * height
                
                drawCircle(
                    color = Color(0xFF4CAF50),
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}

// Weekly Progress Chart
@Composable
fun WeeklyProgressChart(
    weeklyData: List<DayProgress>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Weekly Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyData.forEach { dayData ->
                    WeeklyProgressBar(
                        dayData = dayData,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyProgressBar(
    dayData: DayProgress,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = dayData.progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "day_progress"
    )
    
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedProgress)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .align(Alignment.BottomCenter)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = dayData.dayName,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Data classes for charts
data class ChartData(
    val label: String,
    val value: Float,
    val color: Color
)

data class BarChartData(
    val label: String,
    val value: Float,
    val color: Color
)

data class LineChartData(
    val label: String,
    val value: Float
)

data class DayProgress(
    val dayName: String,
    val progress: Float // 0.0 to 1.0
)