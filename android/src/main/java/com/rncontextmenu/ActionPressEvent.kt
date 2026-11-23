package com.rncontextmenu

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

internal class ActionPressEvent(surfaceId: Int, viewId: Int, private val id: String) :
  Event<ActionPressEvent>(surfaceId, viewId) {

  override fun getEventName() = NAME

  override fun canCoalesce() = false

  override fun getEventData(): WritableMap = Arguments.createMap().apply { putString("id", id) }

  companion object {
    const val NAME = "topActionPress"
  }
}
