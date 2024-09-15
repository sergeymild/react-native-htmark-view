package com.htmlview

import android.graphics.Color
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import org.xml.sax.XMLReader

class CustomTagHandler : Html.TagHandler {
  override fun handleTag(
    opening: Boolean,
    tag: String,
    output: Editable,
    xmlReader: XMLReader
  ) {
    if (opening) {
      startSpan(tag, output)
    } else {
      endSpan(tag, output)
    }
  }

  private fun startSpan(tag: String, output: Editable) {
    val start = output.length
    output.setSpan(CustomTag(tag), start, start, Spannable.SPAN_MARK_MARK)
  }

  private fun endSpan(tag: String, output: Editable) {
    val end = output.length
    val customTag = getLast(output, CustomTag::class.java)

    val start = output.getSpanStart(customTag)
    output.removeSpan(customTag)

    if (start != end) {
      val spanContent = output.subSequence(start, end).toString()

      if (spanContent.contains("color")) {
        // Extract color from inline style
        val colorValue = "#FF5733" // Example: extract from spanContent
        output.setSpan(ForegroundColorSpan(Color.parseColor(colorValue)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
      }

      if (spanContent.contains("font-size")) {
        // Extract font size from inline style
        val sizeValue = 1.5f // Example: extract and convert to float
        output.setSpan(RelativeSizeSpan(sizeValue), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }
  }

  private fun <T> getLast(text: Editable, kind: Class<T>): T? {
    val spans = text.getSpans(0, text.length, kind)
    return if (spans.isEmpty()) null else spans[spans.size - 1]
  }

  class CustomTag(val tag: String)
}
