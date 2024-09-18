package com.htmark

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.View
import com.facebook.react.uimanager.PixelUtil
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

// Класс для хранения текущих стилей
data class StyleProperties(
  var isBold: Boolean = false,
  var isItalic: Boolean = false,
  var isUnderline: Boolean = false,
  var fontSize: Float? = null, // Размер шрифта в пикселях
  var textColor: Int? = null, // Цвет текста
  var url: String? = null, // Для ссылок
  var textAlign: Layout.Alignment? = null // Выравнивание текста
)

class HtmlParser {
  private var shouldSkipFirstNewLine = true
  fun parse(
    context: Context,
    html: String,
    treatPxAsDp: Boolean = false // Существующий параметр
  ): SpannableString {
    // Создаем пустой SpannableStringBuilder
    val spannableBuilder = SpannableStringBuilder()

    // Парсим HTML с помощью Jsoup
    val document = Jsoup.parse(html)

    // Рекурсивно проходим по узлам документа
    traverseNodes(context, document.body(), spannableBuilder, StyleProperties(), treatPxAsDp)

    // Преобразуем SpannableStringBuilder в SpannableString
    return SpannableString(spannableBuilder)
  }

  private fun traverseNodes(
    context: Context,
    node: Node?,
    builder: SpannableStringBuilder,
    parentStyle: StyleProperties,
    treatPxAsDp: Boolean
  ) {
    println("traverseNodes ${node}")
    if (node == null) return

    for (child in node.childNodes()) {
      when (child) {
        is TextNode -> {
          val start = builder.length
          val text = child.text()
          builder.append(text)
          val end = builder.length

          // Применяем стили из parentStyle
          applyStyles(builder, start, end, parentStyle)
        }

        is Element -> {
          val newStyle = parentStyle.copy()

          // Обработка атрибута style
          val styleAttr = child.attr("style")
          if (styleAttr.isNotEmpty()) {
            val stylesFromAttr = parseStyle(context, styleAttr, treatPxAsDp)
            newStyle.apply {
              if (stylesFromAttr.isBold) isBold = true
              if (stylesFromAttr.isItalic) isItalic = true
              if (stylesFromAttr.isUnderline) isUnderline = true
              if (stylesFromAttr.fontSize != null) fontSize = stylesFromAttr.fontSize
              if (stylesFromAttr.textColor != null) textColor = stylesFromAttr.textColor
              if (stylesFromAttr.textAlign != null) textAlign = stylesFromAttr.textAlign
            }
          }

          // Обработка стилей в зависимости от тега
          when (child.tagName()) {
            "b", "strong" -> {
              newStyle.isBold = true
            }

            "i", "em" -> {
              newStyle.isItalic = true
            }

            "u" -> {
              newStyle.isUnderline = true
            }

            "font" -> {
              val color = child.attr("color")
              if (color.isNotEmpty()) {
                newStyle.textColor = parseColor(color)
              }
              val sizeAttr = child.attr("size")
              if (sizeAttr.isNotEmpty()) {
                newStyle.fontSize = parseFontSize(context, sizeAttr, treatPxAsDp)
              }
            }

            "a" -> {
              val url = child.attr("href")
              newStyle.url = url
              // Можно добавить стили для ссылок
              newStyle.textColor = Color.BLUE
              newStyle.isUnderline = true
            }

            "br" -> {
              builder.append("\n")
            }
            "p" -> {
              if (shouldSkipFirstNewLine) {
                shouldSkipFirstNewLine = false
              } else {
                builder.append("\n")
              }
            }
          }
          traverseNodes(context, child, builder, newStyle, treatPxAsDp)
        }
      }
    }
  }

  private fun applyStyles(
    builder: SpannableStringBuilder,
    start: Int,
    end: Int,
    style: StyleProperties,
  ) {
    if (style.isBold) {
      builder.setSpan(
        StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    }
    if (style.isItalic) {
      builder.setSpan(
        StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    }
    if (style.isUnderline) {
      builder.setSpan(
        UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    }
    style.fontSize?.let { fontSizePx ->
      builder.setSpan(
        AbsoluteSizeSpan(fontSizePx.toInt()),
        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    }
    style.textColor?.let { textColor ->
      builder.setSpan(
        ForegroundColorSpan(textColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    }
    style.url?.let { url ->
      builder.setSpan(URLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    style.textAlign?.let { alignment ->
      builder.setSpan(
        AlignmentSpan.Standard(alignment), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    }
  }

  private fun parseStyle(
    context: Context,
    styleString: String,
    treatPxAsDp: Boolean
  ): StyleProperties {
    val styles = StyleProperties()

    // Разбиваем строку стилей на пары "ключ: значение"
    val declarations = styleString.split(";").map { it.trim() }
    for (declaration in declarations) {
      if (declaration.isEmpty()) continue
      val parts = declaration.split(":").map { it.trim() }
      if (parts.size != 2) continue
      val property = parts[0].lowercase()
      val value = parts[1].lowercase()

      when (property) {
        "font-weight" -> {
          if (value == "bold") styles.isBold = true
        }

        "font-style" -> {
          if (value == "italic") styles.isItalic = true
        }

        "text-decoration" -> {
          if (value.contains("underline")) styles.isUnderline = true
        }

        "font-size" -> {
          val fontSize = parseCssFontSize(context, value, treatPxAsDp)
          if (fontSize != null) styles.fontSize = fontSize
        }

        "color" -> {
          val color = parseColor(value)
          if (color != null) styles.textColor = color
        }

        "text-align" -> {
          val alignment = parseTextAlign(value)
          if (alignment != null) styles.textAlign = alignment
        }
      }
    }

    return styles
  }

  private fun parseCssFontSize(
    context: Context,
    value: String,
    treatPxAsDp: Boolean
  ): Float? {
    return when {
      value.endsWith("px") -> {
        val sizeValue = value.removeSuffix("px").toFloatOrNull()
        if (sizeValue != null) {
          PixelUtil.toPixelFromDIP(sizeValue)
        } else null
      }

      value.endsWith("dp") || value.endsWith("dip") -> {
        val dpValue = value.removeSuffix("dp").removeSuffix("dip").toFloatOrNull()
        if (dpValue != null) {
          PixelUtil.toPixelFromDIP(dpValue)
        } else null
      }

      value.endsWith("sp") -> {
        val spValue = value.removeSuffix("sp").toFloatOrNull()
        if (spValue != null) {
          PixelUtil.toPixelFromSP(spValue)
        } else null
      }

      value.endsWith("em") -> {
        val emValue = value.removeSuffix("em").toFloatOrNull()
        if (emValue != null) {
          PixelUtil.toPixelFromSP(emValue)
        } else null
      }

      value.endsWith("%") -> {
        val percentValue = value.removeSuffix("%").toFloatOrNull()
        if (percentValue != null) {
          // Предположим, что 100% соответствует 16sp
          val spValue = 16f * (percentValue / 100f)
          TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spValue,
            context.resources.displayMetrics
          )
        } else null
      }

      else -> {
        // Обработка числовых значений без единиц (считаем, что это sp)
        val spValue = value.toFloatOrNull()
        if (spValue != null) {
          PixelUtil.toPixelFromSP(spValue)
        } else null
      }
    }
  }

  private fun parseFontSize(
    context: Context,
    sizeAttr: String,
    treatPxAsDp: Boolean
  ): Float? {
    // Стандартный размер шрифта в sp
    val defaultFontSizeSp = 16f

    return when {
      // Относительный размер, например, "+1", "-2"
      sizeAttr.startsWith("+") || sizeAttr.startsWith("-") -> {
        val relativeSize = sizeAttr.toFloatOrNull()
        if (relativeSize != null) {
          // Каждый шаг ~20% от стандартного размера
          val spValue = defaultFontSizeSp * (1 + (relativeSize * 0.2f))
          TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spValue,
            context.resources.displayMetrics
          )
        } else null
      }
      // Абсолютный размер от 1 до 7
      else -> {
        val absoluteSize = sizeAttr.toIntOrNull()
        if (absoluteSize != null) {
          val sizeScale = when (absoluteSize) {
            1 -> 0.6f
            2 -> 0.8f
            3 -> 1.0f // Стандартный размер
            4 -> 1.2f
            5 -> 1.4f
            6 -> 1.6f
            7 -> 1.8f
            else -> 1.0f
          }
          val spValue = defaultFontSizeSp * sizeScale
          TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spValue,
            context.resources.displayMetrics
          )
        } else null
      }
    }
  }

  private fun parseColor(colorString: String): Int? {
    return try {
      Color.parseColor(colorString)
    } catch (e: IllegalArgumentException) {
      // Некорректный цвет
      null
    }
  }

  private fun parseTextAlign(value: String): Layout.Alignment? {
    return when (value.lowercase()) {
      "left" -> Layout.Alignment.ALIGN_NORMAL
      "center", "centre" -> Layout.Alignment.ALIGN_CENTER
      "right" -> Layout.Alignment.ALIGN_OPPOSITE
      "justify" -> Layout.Alignment.ALIGN_NORMAL // В Android нет прямой поддержки justify
      else -> null
    }
  }
}
