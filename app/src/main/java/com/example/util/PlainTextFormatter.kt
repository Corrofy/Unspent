package com.example.util

/**
 * Strips markdown syntax from text, producing clean plain text.
 */
fun stripMarkdown(raw: String): String {
    if (raw.isBlank()) return ""

    var text = raw

    // Remove mermaid diagrams / callout blocks / code fences while keeping inner lines or code
    text = text.replace(Regex("```(?:mermaid|callout)?[\\w-]*\\n?([\\s\\S]*?)```", RegexOption.IGNORE_CASE), "$1")

    // Remove inline code
    text = text.replace(Regex("`([^`]+)`"), "$1")

    // Remove images: ![alt](url) -> alt
    text = text.replace(Regex("!\\[([^\\]]*)\\]\\([^\\)]*\\)"), "$1")

    // Remove links: [text](url) -> text
    text = text.replace(Regex("\\[([^\\]]+)\\]\\([^\\)]*\\)"), "$1")

    // Remove bold/italic combinations: ***text***, ___text___
    text = text.replace(Regex("(\\*\\*\\*|___)(.*?)\\1"), "$2")
    // Remove bold: **text**, __text__
    text = text.replace(Regex("(\\*\\*|__)(.*?)\\1"), "$2")
    // Remove italic / strikethrough: *text*, _text_, ~~text~~
    text = text.replace(Regex("(\\*|_|~~)(.*?)\\1"), "$2")

    // Line-by-line processing for block syntax
    val cleanedLines = text.lines().map { line ->
        var l = line.trimStart()
        // Headers: # Header -> Header
        l = l.replace(Regex("^#{1,6}\\s+"), "")
        // Blockquotes: > Quote -> Quote
        l = l.replace(Regex("^>+\\s*"), "")
        // Unordered lists: - item, * item, + item -> item
        l = l.replace(Regex("^[-*+]\\s+"), "")
        // Ordered lists: 1. item -> item
        l = l.replace(Regex("^\\d+\\.\\s+"), "")
        // Clean leading/trailing spaces on the stripped line
        l.trimEnd()
    }

    return cleanedLines.joinToString("\n").trim()
}
