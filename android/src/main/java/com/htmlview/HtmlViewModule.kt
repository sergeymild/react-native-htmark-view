package com.htmlview

import android.os.Handler
import android.os.Looper
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.UIManagerHelper

@ReactModule(name = HtmlViewModule.TAG)
class HtmlViewModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  companion object {
    const val TAG = "HtmlView"
  }

  override fun getName(): String {
    return TAG
  }

  private fun findView(viewId: Int): HtmlView? {
    val view = if (reactApplicationContext != null) UIManagerHelper.getUIManager(reactApplicationContext, viewId)?.resolveView(viewId) as HtmlView? else null
    return view
  }

  @ReactMethod
  fun findLink(viewTag: Int, locationX: Double, locationY: Double, promise: Promise) {
    Handler(Looper.getMainLooper()).post {
      try {
        val view = findView(viewTag)
        view?.findLink(locationX, locationY, promise)
      } catch (_: Throwable) {

      }
    }
  }
}
