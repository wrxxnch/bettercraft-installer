package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Minecraft Faithful 32x32 Palette
object MinecraftPalette {
    val BackgroundDark = Color(0xFF141517)
    val PanelBackground = Color(0xFF26282B)
    val PanelInner = Color(0xFF1B1C1E)

    // Button Stone
    val ButtonStone = Color(0xFF53565A)
    val ButtonStoneHover = Color(0xFF6B6E73)
    val ButtonStoneHighlight = Color(0xFF8C9096)
    val ButtonStoneShadow = Color(0xFF282A2D)
    val ButtonBorderBlack = Color(0xFF0C0D0E)

    // Button Green (Play / Action)
    val ButtonGreen = Color(0xFF2E7D32)
    val ButtonGreenHover = Color(0xFF388E3C)
    val ButtonGreenHighlight = Color(0xFF66BB6A)
    val ButtonGreenShadow = Color(0xFF1B5E20)

    // Gold / Diamond / Emerald / Redstone
    val DiamondBlue = Color(0xFF4AEDD9)
    val EmeraldGreen = Color(0xFF55FF55)
    val GoldYellow = Color(0xFFFFAA00)
    val RedstoneRed = Color(0xFFFF5555)
    val IronGray = Color(0xFFD8D8D8)

    // Minecraft Text Shadow
    val TextShadow = Color(0xFF222222)
    val TextWhite = Color(0xFFFFFFFF)
}

/**
 * Text rendered with Minecraft GUI style drop shadow
 */
@Composable
fun MinecraftText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MinecraftPalette.TextWhite,
    fontSize: Int = 14,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign? = null
) {
    Box(modifier = modifier) {
        // Drop shadow
        Text(
            text = text,
            color = MinecraftPalette.TextShadow,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp,
            textAlign = textAlign,
            modifier = Modifier.padding(start = 1.5.dp, top = 1.5.dp)
        )
        // Foreground
        Text(
            text = text,
            color = color,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp,
            textAlign = textAlign
        )
    }
}

/**
 * Minecraft Faithful 32x32 Beveled Button
 */
@Composable
fun MinecraftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isActionGreen: Boolean = false,
    icon: ImageVector? = null,
    testTag: String = "mc_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val baseColor = when {
        !enabled -> Color(0xFF383838)
        isActionGreen && isPressed -> MinecraftPalette.ButtonGreen
        isActionGreen -> MinecraftPalette.ButtonGreenHover
        isPressed -> MinecraftPalette.ButtonStone
        else -> MinecraftPalette.ButtonStoneHover
    }

    val highlightColor = when {
        !enabled -> Color(0xFF4A4A4A)
        isActionGreen -> MinecraftPalette.ButtonGreenHighlight
        else -> MinecraftPalette.ButtonStoneHighlight
    }

    val shadowColor = when {
        !enabled -> Color(0xFF222222)
        isActionGreen -> MinecraftPalette.ButtonGreenShadow
        else -> MinecraftPalette.ButtonStoneShadow
    }

    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1.0f, label = "btn_press")

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MinecraftPalette.ButtonBorderBlack)
            .padding(2.dp)
            .drawBehind {
                // Outer highlight (Top & Left)
                drawRect(
                    color = if (isPressed) shadowColor else highlightColor,
                    topLeft = Offset.Zero,
                    size = Size(size.width, 3.dp.toPx())
                )
                drawRect(
                    color = if (isPressed) shadowColor else highlightColor,
                    topLeft = Offset.Zero,
                    size = Size(3.dp.toPx(), size.height)
                )
                // Outer shadow (Bottom & Right)
                drawRect(
                    color = if (isPressed) highlightColor else shadowColor,
                    topLeft = Offset(0f, size.height - 3.dp.toPx()),
                    size = Size(size.width, 3.dp.toPx())
                )
                drawRect(
                    color = if (isPressed) highlightColor else shadowColor,
                    topLeft = Offset(size.width - 3.dp.toPx(), 0f),
                    size = Size(3.dp.toPx(), size.height)
                )
            }
            .background(baseColor)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) Color.White else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            MinecraftText(
                text = text.uppercase(),
                color = if (enabled) Color.White else Color(0xFFA0A0A0),
                fontSize = 13,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Minecraft Faithful 32x32 Container Panel (Double Inset Border)
 */
@Composable
fun MinecraftPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MinecraftPalette.PanelBackground,
    innerPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(MinecraftPalette.ButtonBorderBlack)
            .padding(2.dp)
            .drawBehind {
                val borderHighlight = Color(0xFF4A4E54)
                val borderShadow = Color(0xFF17181A)
                // Inset border
                drawRect(borderHighlight, Offset.Zero, Size(size.width, 2.dp.toPx()))
                drawRect(borderHighlight, Offset.Zero, Size(2.dp.toPx(), size.height))
                drawRect(borderShadow, Offset(0f, size.height - 2.dp.toPx()), Size(size.width, 2.dp.toPx()))
                drawRect(borderShadow, Offset(size.width - 2.dp.toPx(), 0f), Size(2.dp.toPx(), size.height))
            }
            .background(backgroundColor)
            .padding(innerPadding)
    ) {
        content()
    }
}

/**
 * Minecraft XP Bar Progress Indicator
 */
@Composable
fun MinecraftXpBar(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    activeColor: Color = MinecraftPalette.EmeraldGreen
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MinecraftText(
                text = label,
                fontSize = 12,
                color = MinecraftPalette.IronGray
            )
            Spacer(modifier = Modifier.weight(1f))
            MinecraftText(
                text = "${(progress * 100).toInt().coerceIn(0, 100)}%",
                fontSize = 12,
                color = MinecraftPalette.EmeraldGreen
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(Color(0xFF0F1012))
                .border(1.5.dp, Color(0xFF33353A))
                .padding(2.dp)
        ) {
            // Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0.01f, 1f))
                    .height(10.dp)
                    .background(activeColor)
            )
        }
    }
}

/**
 * Minecraft Category / Status Chip Badge
 */
@Composable
fun MinecraftBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF141416))
            .border(1.dp, color)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        MinecraftText(
            text = text,
            color = color,
            fontSize = 11,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Minecraft Faithful Input Field
 */
@Composable
fun MinecraftInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF0C0D0E))
            .border(1.5.dp, Color(0xFF33353A))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    MinecraftText(
                        text = placeholder,
                        fontSize = 11,
                        color = Color.DarkGray
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    cursorBrush = SolidColor(MinecraftPalette.EmeraldGreen),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(6.dp))
                trailingIcon()
            }
        }
    }
}
