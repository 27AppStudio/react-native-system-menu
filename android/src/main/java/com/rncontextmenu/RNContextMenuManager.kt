package com.rncontextmenu

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.viewmanagers.RNContextMenuManagerDelegate
import com.facebook.react.viewmanagers.RNContextMenuManagerInterface
import com.facebook.react.views.view.ReactClippingViewManager

@ReactModule(name = RNContextMenuManager.NAME)
class RNContextMenuManager :
  ReactClippingViewManager<RNContextMenuView>(),
  RNContextMenuManagerInterface<RNContextMenuView> {

  private val delegate = RNContextMenuManagerDelegate(this)

  override fun getDelegate(): ViewManagerDelegate<RNContextMenuView> = delegate

  override fun getName() = NAME

  override fun createViewInstance(context: ThemedReactContext) = RNContextMenuView(context)

  override fun setActions(view: RNContextMenuView, value: ReadableArray?) {
    view.actions = MenuAction.listFrom(value)
  }

  override fun setAndroidTrigger(view: RNContextMenuView, value: String?) {
    view.opensOnTap = value == "tap"
  }

  override fun setAndroidDestructiveColor(view: RNContextMenuView, value: Int?) {
    view.destructiveColor = value
  }

  override fun setIosPreviewCornerRadius(view: RNContextMenuView, value: Float) = Unit

  override fun onDropViewInstance(view: RNContextMenuView) {
    view.dismissMenu()
    super.onDropViewInstance(view)
  }

  override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> =
    mutableMapOf(ActionPressEvent.NAME to mapOf("registrationName" to "onActionPress"))

  companion object {
    const val NAME = "RNContextMenu"
  }
}
