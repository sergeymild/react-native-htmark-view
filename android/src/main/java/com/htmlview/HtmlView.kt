package com.htmlview

import android.content.Context
import android.graphics.Color
import android.text.SpannedString
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.getSpans
import com.facebook.react.bridge.Promise
import com.facebook.react.uimanager.PixelUtil


class HtmlView(context: Context) : AppCompatTextView(context) {
//  val textView = AppCompatTextView(context)
  val textView
  get() = this

  init {
    textView.setEllipsize(TextUtils.TruncateAt.MARQUEE)
  }

  fun setSpannableText(html: String) {
    text = getSpannableFromHtml(html)
  }

  fun setMarkdownText(mark: String) {
    text = getSpannableFromMarkdown(mark)
  }

  fun findLink(locationX: Double, locationY: Double, promise: Promise) {
    var x = PixelUtil.toPixelFromDIP(locationX)
    var y = PixelUtil.toPixelFromDIP(locationY)
    x -= totalPaddingLeft;
    y -= totalPaddingTop;

    x += textView.scrollX;
    y += textView.scrollY;

    val layout = textView.layout
    val line = layout.getLineForVertical(y.toInt())
    val offset = layout.getOffsetForHorizontal(line, x)

    val spans = (text as SpannedString).getSpans<URLSpan>(offset, offset)
    promise.resolve(spans.firstOrNull()?.url)
  }
}
