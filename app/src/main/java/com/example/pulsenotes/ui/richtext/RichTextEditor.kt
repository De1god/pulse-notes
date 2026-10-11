package com.example.pulsenotes.ui.richtext

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsenotes.data.richtext.RichTextSpan
import com.example.pulsenotes.data.richtext.TextFormat

/**
 * Rich text editor with formatting toolbar
 */
@Composable
fun RichTextEditor(
    text: String,
    spans: List<RichTextSpan>,
    onTextChange: (String, List<RichTextSpan>) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Start typing...",
    readOnly: Boolean = false
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                buildAnnotatedString {
                    append(text)
                    spans.forEach { span ->
                        addStyle(
                            style = TextFormat.toSpanStyle(span.format),
                            start = span.start.coerceIn(0, text.length),
                            end = span.end.coerceIn(0, text.length)
                        )
                    }
                },
                selection = TextRange(text.length)
            )
        )
    }

    var selectedSpan by remember { mutableStateOf<RichTextSpan?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Formatting Toolbar
        if (!readOnly) {
            FormattingToolbar(
                onFormatClick = { format ->
                    val selection = textFieldValue.selection
                    if (selection.start != selection.end) {
                        val newSpan = RichTextSpan(
                            start = selection.start,
                            end = selection.end,
                            format = format
                        )
                        // Remove overlapping spans and add new one
                        val updatedSpans = spans.filterNot { existing ->
                            existing.start < newSpan.end && existing.end > newSpan.start &&
                            existing.format::class == newSpan.format::class
                        } + newSpan
                        onTextChange(textFieldValue.text, updatedSpans)
                    }
                },
                onClearFormatting = {
                    val selection = textFieldValue.selection
                    val updatedSpans = spans.filterNot { span ->
                        span.start < selection.end && span.end > selection.start
                    }
                    onTextChange(textFieldValue.text, updatedSpans)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Text Field
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                // Adjust spans when text changes
                val updatedSpans = adjustSpansForTextChange(
                    spans,
                    textFieldValue.text.length,
                    newValue.text.length,
                    newValue.selection.start
                )
                onTextChange(newValue.text, updatedSpans)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (textFieldValue.text.isEmpty() && !readOnly) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    innerTextField()
                }
            },
            readOnly = readOnly
        )
    }
}

/**
 * Formatting toolbar with common text formatting options
 */
@Composable
private fun FormattingToolbar(
    onFormatClick: (TextFormat) -> Unit,
    onClearFormatting: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bold
        FormatButton(
            icon = Icons.Filled.FormatBold,
            contentDescription = "Bold",
            onClick = { onFormatClick(TextFormat.Bold) }
        )

        // Italic
        FormatButton(
            icon = Icons.Filled.FormatItalic,
            contentDescription = "Italic",
            onClick = { onFormatClick(TextFormat.Italic) }
        )

        // Underline
        FormatButton(
            icon = Icons.Filled.FormatUnderlined,
            contentDescription = "Underline",
            onClick = { onFormatClick(TextFormat.Underline) }
        )

        // Strikethrough
        FormatButton(
            icon = Icons.Filled.StrikethroughS,
            contentDescription = "Strikethrough",
            onClick = { onFormatClick(TextFormat.Strikethrough) }
        )

        Divider(
            modifier = Modifier
                .height(24.dp)
                .padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Heading 1
        FormatButton(
            icon = Icons.Filled.Title,
            contentDescription = "Heading 1",
            onClick = { onFormatClick(TextFormat.Heading1) }
        )

        // Heading 2
        FormatButton(
            icon = Icons.Default.Title, // Changed to Title for Heading 2 compatibility if FormatIndentIncrease fails
            contentDescription = "Heading 2",
            onClick = { onFormatClick(TextFormat.Heading2) }
        )

        Divider(
            modifier = Modifier
                .height(24.dp)
                .padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Bullet Point
        FormatButton(
            icon = Icons.Filled.FormatListBulleted,
            contentDescription = "Bullet Point",
            onClick = { onFormatClick(TextFormat.BulletPoint) }
        )

        // Code
        FormatButton(
            icon = Icons.Filled.Code,
            contentDescription = "Code",
            onClick = { onFormatClick(TextFormat.Code) }
        )

        Divider(
            modifier = Modifier
                .height(24.dp)
                .padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Clear Formatting
        FormatButton(
            icon = Icons.Filled.FormatClear,
            contentDescription = "Clear Formatting",
            onClick = onClearFormatting
        )
    }
}

/**
 * Individual format button
 */
@Composable
private fun FormatButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Adjust spans when text is edited
 */
private fun adjustSpansForTextChange(
    spans: List<RichTextSpan>,
    oldLength: Int,
    newLength: Int,
    changePosition: Int
): List<RichTextSpan> {
    val lengthDiff = newLength - oldLength
    
    return spans.mapNotNull { span ->
        when {
            // Span is completely before the change - keep as is
            span.end <= changePosition -> span
            
            // Span is completely after the change - shift positions
            span.start >= changePosition -> span.copy(
                start = span.start + lengthDiff,
                end = span.end + lengthDiff
            )
            
            // Span contains the change point - adjust end position
            else -> {
                val newEnd = (span.end + lengthDiff).coerceAtLeast(span.start)
                if (newEnd > span.start) {
                    span.copy(end = newEnd)
                } else {
                    null // Remove empty spans
                }
            }
        }
    }
}

/**
 * Convert rich text to displayable annotated string
 */
fun buildRichTextString(
    text: String,
    spans: List<RichTextSpan>
): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        append(text)
        spans.forEach { span ->
            addStyle(
                style = TextFormat.toSpanStyle(span.format),
                start = span.start.coerceIn(0, text.length),
                end = span.end.coerceIn(0, text.length)
            )
        }
    }
}