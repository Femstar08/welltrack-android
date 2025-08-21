package com.beaconledger.welltrack.presentation.design

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Consistent spacing system
object WellTrackSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

// Consistent elevation system
object WellTrackElevation {
    val none = 0.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
    val xl = 16.dp
}

// Consistent corner radius system
object WellTrackCorners {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val round = 50.dp
}

// Enhanced Card Components
@Composable
fun WellTrackCard(
    modifier: Modifier = Modifier,
    elevation: androidx.compose.ui.unit.Dp = WellTrackElevation.md,
    cornerRadius: androidx.compose.ui.unit.Dp = WellTrackCorners.md,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }
    
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = border
    ) {
        Column(
            modifier = Modifier.padding(WellTrackSpacing.md),
            content = content
        )
    }
}

// Enhanced Button System
@Composable
fun WellTrackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    icon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    val buttonModifier = modifier.then(
        when (size) {
            ButtonSize.Small -> Modifier.height(32.dp)
            ButtonSize.Medium -> Modifier.height(40.dp)
            ButtonSize.Large -> Modifier.height(48.dp)
        }
    )
    
    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                ButtonContent(icon = icon, content = content)
            }
        }
        ButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                colors = OutlinedButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                ButtonContent(icon = icon, content = content)
            }
        }
        ButtonVariant.Tertiary -> {
            TextButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled
            ) {
                ButtonContent(icon = icon, content = content)
            }
        }
    }
}

@Composable
private fun RowScope.ButtonContent(
    icon: ImageVector?,
    content: @Composable RowScope.() -> Unit
) {
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(WellTrackSpacing.sm))
    }
    content()
}

// Enhanced Input Components
@Composable
fun WellTrackTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let { 
                { Icon(imageVector = it, contentDescription = null) }
            },
            trailingIcon = trailingIcon?.let { icon ->
                {
                    IconButton(
                        onClick = onTrailingIconClick ?: {}
                    ) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
            },
            isError = isError,
            enabled = enabled,
            singleLine = singleLine,
            shape = RoundedCornerShape(WellTrackCorners.sm),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = WellTrackSpacing.md, top = WellTrackSpacing.xs)
            )
        }
    }
}

// Enhanced Status Indicators
@Composable
fun StatusIndicator(
    status: Status,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val (color, icon, label) = when (status) {
        Status.Success -> Triple(
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle,
            "Success"
        )
        Status.Warning -> Triple(
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.Warning,
            "Warning"
        )
        Status.Error -> Triple(
            MaterialTheme.colorScheme.error,
            Icons.Default.Error,
            "Error"
        )
        Status.Info -> Triple(
            MaterialTheme.colorScheme.secondary,
            Icons.Default.Info,
            "Info"
        )
        Status.Loading -> Triple(
            MaterialTheme.colorScheme.primary,
            Icons.Default.Refresh,
            "Loading"
        )
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WellTrackSpacing.sm)
    ) {
        if (status == Status.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = color
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        
        if (showLabel) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Enhanced Progress Components
@Composable
fun WellTrackProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    showPercentage: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    animated: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) progress else progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "progress"
    )
    
    Column(modifier = modifier) {
        if (label != null || showPercentage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (showPercentage) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(WellTrackSpacing.sm))
        }
        
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = backgroundColor
        )
    }
}

// Enhanced Avatar Component
@Composable
fun WellTrackAvatar(
    name: String,
    imageUrl: String? = null,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showOnlineIndicator: Boolean = false,
    isOnline: Boolean = false
) {
    val avatarModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }
    
    Box(modifier = avatarModifier) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                // Image loading would go here
                // For now, show initials
                Text(
                    text = name.take(2).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = (size.value * 0.4).sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = name.take(2).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = (size.value * 0.4).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (showOnlineIndicator) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .clip(CircleShape)
                    .background(
                        if (isOnline) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.outline
                    )
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

// Enhanced Empty State Component
@Composable
fun WellTrackEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(WellTrackSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(WellTrackSpacing.md))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(WellTrackSpacing.sm))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(WellTrackSpacing.lg))
            
            WellTrackButton(
                onClick = onActionClick,
                variant = ButtonVariant.Primary
            ) {
                Text(actionLabel)
            }
        }
    }
}

// Enums for design system
enum class ButtonVariant {
    Primary, Secondary, Tertiary
}

enum class ButtonSize {
    Small, Medium, Large
}

enum class Status {
    Success, Warning, Error, Info, Loading
}