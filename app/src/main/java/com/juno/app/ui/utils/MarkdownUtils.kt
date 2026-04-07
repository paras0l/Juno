package com.juno.app.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

object MarkdownUtils {

    /**
     * Split Markdown content into sections by heading
     * Returns List<Pair<sectionTitle, sectionContent>>
     */
    fun parseSections(content: String): List<Pair<String, String>> {
        val sections = mutableListOf<Pair<String, String>>()
        val lines = content.split("\n")
        
        var currentTitle = ""
        var currentContent = StringBuilder()
        
        for (line in lines) {
            val trimmed = line.trim()
            
            if (trimmed.startsWith("### ")) {
                if (currentTitle.isNotEmpty()) {
                    sections.add(currentTitle to currentContent.toString().trim())
                }
                currentTitle = trimmed.removePrefix("### ").trim()
                currentContent = StringBuilder()
            } else if (trimmed.startsWith("## ") || trimmed.startsWith("# ")) {
                if (currentTitle.isNotEmpty()) {
                    sections.add(currentTitle to currentContent.toString().trim())
                }
                currentTitle = trimmed.replace(Regex("^#+ "), "").trim()
                currentContent = StringBuilder()
            } else {
                if (trimmed.isNotEmpty()) {
                    currentContent.appendLine(trimmed)
                }
            }
        }
        
        if (currentTitle.isNotEmpty()) {
            sections.add(currentTitle to currentContent.toString().trim())
        }
        
        return sections
    }

    /**
     * Convert a single line of Markdown to AnnotatedString
     * Supports **bold** syntax
     */
    fun parseLine(text: String): AnnotatedString {
        return buildAnnotatedString {
            val regex = Regex("\\*\\*(.+?)\\*\\*")
            var lastIndex = 0
            
            for (match in regex.findAll(text)) {
                if (match.range.first > lastIndex) {
                    append(text.substring(lastIndex, match.range.first))
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
                lastIndex = match.range.last + 1
            }
            
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }
}
