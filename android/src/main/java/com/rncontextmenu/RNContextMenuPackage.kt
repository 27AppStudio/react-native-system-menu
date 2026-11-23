package com.rncontextmenu

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class RNContextMenuPackage : ReactPackage {
  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> =
    listOf(RNContextMenuManager())

  @Deprecated("Deprecated in ReactPackage")
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> = emptyList()
}
