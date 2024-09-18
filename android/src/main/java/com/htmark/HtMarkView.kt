package com.htmark

import android.content.Context
import android.text.SpannedString
import android.text.TextUtils
import android.text.style.URLSpan
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.getSpans
import com.facebook.react.bridge.Promise
import com.facebook.react.uimanager.PixelUtil


class HtMarkView(context: Context) : AppCompatTextView(context) {
//  val textView = AppCompatTextView(context)
  val textView
  get() = this

  init {

  }

  fun setSpannableText(html: String) {
    setText(getSpannableFromHtml(html), BufferType.SPANNABLE)
  }

  fun setMarkdownText(mark: String) {
    setText(getSpannableFromMarkdown(mark), BufferType.SPANNABLE)
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
