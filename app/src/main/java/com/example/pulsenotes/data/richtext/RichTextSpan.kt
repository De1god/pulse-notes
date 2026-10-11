package com.example.pulsenotes.data.richtext

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable

/**
 * Represents a rich text formatting span with its position in the text
 */
@Serializable
data class RichTextSpan(
    val start: Int,
    val end: Int,
    val format: TextFormat
) {
    companion object {
        fun bold(start: Int, end: Int) = RichTextSpan(start, end, TextFormat.Bold)
        fun italic(start: Int, end: Int) = RichTextSpan(start, end, TextFormat.Italic)
        fun underline(start: Int, end: Int) = RichTextSpan(start, end, TextFormat.Underline)
        fun strikethrough(start: Int, end: Int) = RichTextSpan(start, end, TextFormat.Strikethrough)
        fun heading1(start: Int, end: Int) = RichTextSpan(start, end, TextFormat.Heading1)
        fun heading2(start: Int, end: Int) = RichTextSpan(start, end, TextFormat.Heading2)
        fun bulletPoint(start: Int, end: Int) = RichTextSpan(start, end, TextFormat.BulletPoint)
        fun code(start: Int, end: Int) = RichTextSpan(start, end, TextFormat.Code)
    }
}

/**
 * Text formatting options
 */
@Serializable
sealed class TextFormat {
    @Serializable
    data object Bold : TextFormat()
    
    @Serializable
    data object Italic : TextFormat()
    
    @Serializable
    data object Underline : TextFormat()
    
    @Serializable
    data object Strikethrough : TextFormat()
    
    @Serializable
    data object Heading1 : TextFormat()
    
    @Serializable
    data object Heading2 : TextFormat()
    
    @Serializable
    data object BulletPoint : TextFormat()
    
    @Serializable
    data object Code : TextFormat()
    
    companion object {
        fun toSpanStyle(format: TextFormat): SpanStyle = when (format) {
            is Bold -> SpanStyle(fontWeight = FontWeight.Bold)
            is Italic -> SpanStyle(fontStyle = FontStyle.Italic)
            is Underline -> SpanStyle(textDecoration = TextDecoration.Underline)
            is Strikethrough -> SpanStyle(textDecoration = TextDecoration.LineThrough)
            is Heading1 -> SpanStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            is Heading2 -> SpanStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            is BulletPoint -> SpanStyle()
            is Code -> SpanStyle(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                background = androidx.compose.ui.graphics.Color.LightGray
            )
        }
    }
}
