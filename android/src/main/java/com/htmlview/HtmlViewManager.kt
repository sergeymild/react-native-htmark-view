package com.htmlview

import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils.TruncateAt
import android.view.View.MeasureSpec
import android.view.ViewGroup.*
import android.view.ViewGroup.LayoutParams.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.HtmlCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.LayoutShadowNode
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.yoga.YogaMeasureFunction
import com.facebook.yoga.YogaMeasureMode
import com.facebook.yoga.YogaMeasureOutput
import com.facebook.yoga.YogaNode
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.lang.ref.WeakReference


fun toTruncate(params: ReadableMap): TruncateAt? {
  if (!params.hasKey("ellipsize")) return null
  return when(params.getString("ellipsize")) {
    "head" -> TruncateAt.START
    "middle" -> TruncateAt.MIDDLE
    "tail" -> TruncateAt.END
    "clip" -> TruncateAt.END
    else -> null
  }
}

fun getSpannableFromHtml(htmlString: String): Spannable {
  println("🗡️getSpannableFromHtml $htmlString")
  return SpannableString(Html.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_COMPACT))
}

fun getSpannableFromMarkdown(markdownString: String): Spannable {
  val parser: Parser = Parser.builder().build()
  val document = parser.parse(markdownString)
  val renderer = HtmlRenderer.builder().build()
  return getSpannableFromHtml(renderer.render(document))
}


fun calculateTextSize(
  spannable: Spannable,
  maxLines: Int,
  ellipsize: TruncateAt?,
  maxWidth: Float,
  maxHeight: Float
): IntArray {
  val r = AppCompatTextView(HtmlViewViewManager.context!!.get()!!)
  r.maxLines = maxLines
  r.isSingleLine = maxLines == 1
  r.ellipsize = ellipsize
  r.text = spannable
  r.layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
  r.measure(
    MeasureSpec.makeMeasureSpec(maxWidth.toInt(), MeasureSpec.AT_MOST),
    MeasureSpec.makeMeasureSpec(maxHeight.toInt(), MeasureSpec.AT_MOST),
  )

  return intArrayOf(r.measuredWidth, r.measuredHeight)
}

class SimpleShadowView: LayoutShadowNode(), YogaMeasureFunction {
  init {
    println("🗡️ SimpleShadowView.ini")
    setMeasureFunction(this);
  }

  private var text = ""
  private var maxLines = Int.MAX_VALUE
  private var isMark = false
  private var ellipsize: TruncateAt? = null

  @ReactProp(name = "params")
  fun setParams(params: ReadableMap) {
    if (params.hasKey("html")) {
      text = params.getString("html")!!
      isMark = false
    } else {
      text = params.getString("markdown")!!
      isMark = true
    }
    maxLines = if (params.hasKey("maxLines")) params.getInt("maxLines") else Int.MAX_VALUE
    ellipsize = toTruncate(params)
    markUpdated()
    dirty()
  }

  override fun measure(
    node: YogaNode?,
    width: Float,
    widthMode: YogaMeasureMode?,
    height: Float,
    heightMode: YogaMeasureMode?
  ): Long {

    val textSize = calculateTextSize(if (isMark) getSpannableFromMarkdown(text) else getSpannableFromHtml(text), maxLines, ellipsize, width, height)
    println("🗡️ SimpleShadowView.measure ${textSize}")
    return YogaMeasureOutput.make(textSize[0], textSize[1])
  }
}

class HtmlViewViewManager : SimpleViewManager<HtmlView>() {
  override fun getName() = "HtmlView"

  companion object {
    var context: WeakReference<ThemedReactContext>? = null
  }

  override fun createViewInstance(reactContext: ThemedReactContext): HtmlView {
    context = WeakReference(reactContext)
    return HtmlView(reactContext)
  }

  @ReactProp(name = "params")
  fun setParams(textView: HtmlView, params: ReadableMap) {
    if (params.hasKey("html")) {
      textView.setSpannableText(params.getString("html")!!)
    } else {
      textView.setMarkdownText(params.getString("markdown")!!)
    }
    textView.textView.maxLines = if (params.hasKey("maxLines")) params.getInt("maxLines") else Int.MAX_VALUE
    textView.textView.isSingleLine = textView.textView.maxLines == 1
    textView.textView.ellipsize = toTruncate(params)
  }

  override fun createShadowNodeInstance(context: ReactApplicationContext): LayoutShadowNode {
    return SimpleShadowView()
  }

  override fun createShadowNodeInstance(): LayoutShadowNode {
    return super.createShadowNodeInstance()
  }

  override fun getShadowNodeClass(): Class<LayoutShadowNode> {
    return SimpleShadowView::class.java as Class<LayoutShadowNode>
  }

  override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> {
    return MapBuilder.builder<String, Any>()
      .put("onPress", MapBuilder.of("registrationName", "onPress"))
      .build()
  }
}
