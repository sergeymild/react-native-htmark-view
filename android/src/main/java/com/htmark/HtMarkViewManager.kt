package com.htmark

import android.text.Spannable
import android.text.TextUtils.TruncateAt
import android.view.View.MeasureSpec
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.widget.AppCompatTextView
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.LayoutShadowNode
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.facebook.yoga.YogaMeasureFunction
import com.facebook.yoga.YogaMeasureMode
import com.facebook.yoga.YogaMeasureOutput
import com.facebook.yoga.YogaNode
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.lang.ref.WeakReference


data class TextLayoutParams(val width: Int, val height: Int, val linesCount: Int)

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
  return HtmlParser().parse(HtMarkViewViewManager.context!!.get()!!, "<div>${htmlString}</div>")
}

fun getSpannableFromMarkdown(markdownString: String): Spannable {
  val parser: Parser = Parser.builder().build()
  val document = parser.parse(markdownString)
  val renderer = HtmlRenderer.builder().build()
  return getSpannableFromHtml(renderer.render(document))
}


val wrapContent = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
fun calculateTextSize(
  spannable: Spannable,
  maxLines: Int,
  ellipsize: TruncateAt?,
  maxWidth: Float,
  maxHeight: Float
): TextLayoutParams {
  val r = AppCompatTextView(HtMarkViewViewManager.context!!.get()!!)
  r.maxLines = maxLines
  //r.isSingleLine = maxLines == 1
  r.ellipsize = ellipsize
  r.text = spannable
  r.layoutParams = wrapContent
  r.measure(
    MeasureSpec.makeMeasureSpec(maxWidth.toInt(), MeasureSpec.AT_MOST),
    MeasureSpec.makeMeasureSpec(maxHeight.toInt(), MeasureSpec.AT_MOST),
  )

  var measuredHeight = r.measuredHeight
  measuredHeight = measuredHeight.coerceAtMost(maxHeight.toInt());

  return TextLayoutParams(
    width = r.measuredWidth,
    height = measuredHeight,
    linesCount = r.lineCount
  )
}

class SimpleShadowView: LayoutShadowNode(), YogaMeasureFunction {
  init {
    println("🗡️ SimpleShadowView.ini")
    setMeasureFunction(this);
  }

  private var text = ""
  private var maxLines = Int.MAX_VALUE
  private var isMark = false
  private var shouldNotifyOnSizeChanged = false
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

  @ReactProp(name = "onSizeCalculated")
  fun onSizeCalculated(shouldNotifyOnSizeChanged: Boolean) {
    this.shouldNotifyOnSizeChanged = shouldNotifyOnSizeChanged
  }

  override fun measure(
    node: YogaNode?,
    width: Float,
    widthMode: YogaMeasureMode?,
    height: Float,
    heightMode: YogaMeasureMode?
  ): Long {

    val h = if (height.isNaN()) Float.MAX_VALUE else height
    val textSize = calculateTextSize(if (isMark) getSpannableFromMarkdown(text) else getSpannableFromHtml(text), maxLines, ellipsize, width, h)
    println("🗡️ SimpleShadowView.measure ${textSize}")
    if (shouldNotifyOnSizeChanged) {
      themedContext?.getJSModule(RCTEventEmitter::class.java)?.receiveEvent(reactTag, "onSizeCalculated", Arguments.createMap().also { map ->
        map.putMap("params", Arguments.createMap().also {
          it.putInt("width", textSize.width)
          it.putInt("height", textSize.height)
          it.putInt("linesCount", textSize.linesCount)
        })
      })
    }
    return YogaMeasureOutput.make(textSize.width, textSize.height)
  }
}

class HtMarkViewViewManager : SimpleViewManager<HtMarkView>() {
  override fun getName() = "HtMarkView"

  companion object {
    var context: WeakReference<ThemedReactContext>? = null
  }

  override fun createViewInstance(reactContext: ThemedReactContext): HtMarkView {
    context = WeakReference(reactContext)
    return HtMarkView(reactContext)
  }

  @ReactProp(name = "params")
  fun setParams(textView: HtMarkView, params: ReadableMap) {
    if (params.hasKey("html")) {
      textView.setSpannableText(params.getString("html")!!)
    } else {
      textView.setMarkdownText(params.getString("markdown")!!)
    }
    textView.textView.maxLines = if (params.hasKey("maxLines")) params.getInt("maxLines") else Int.MAX_VALUE
    //textView.textView.isSingleLine = textView.textView.maxLines == 1
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
      .put("onSizeCalculated", MapBuilder.of("registrationName", "onSizeCalculated"))
      .build()
  }
}
