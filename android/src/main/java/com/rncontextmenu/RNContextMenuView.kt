package com.rncontextmenu

import android.content.res.Configuration
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.events.NativeGestureUtil
import com.facebook.react.views.view.ReactViewGroup
import java.lang.ref.WeakReference

class RNContextMenuView(private val reactContext: ThemedReactContext) : ReactViewGroup(reactContext) {
  internal var actions: List<MenuAction> = emptyList()
  internal var opensOnTap = false
  internal var destructiveColor: Int? = null

  private var popup: MenuPopup? = null
  private var ownsGesture = false
  private var longClickTarget: WeakReference<View>? = null

  private val detector =
    GestureDetector(
      reactContext,
      object : GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent) = true

        override fun onShowPress(e: MotionEvent) = Unit

        // Not onSingleTapConfirmed, which waits out the double-tap timeout.
        override fun onSingleTapUp(e: MotionEvent): Boolean {
          if (opensOnTap) showMenu(e.x, e.y)
          return opensOnTap
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float) = false

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float) = false

        override fun onLongPress(e: MotionEvent) {
          if (opensOnTap || !showMenu(e.x, e.y)) return
          takeGesture(e)
          performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
      },
    )

  override fun dispatchTouchEvent(event: MotionEvent): Boolean {
    detector.onTouchEvent(event)
    val handled = super.dispatchTouchEvent(event)
    if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
      if (ownsGesture) {
        ownsGesture = false
        NativeGestureUtil.notifyNativeGestureEnded(this, event)
      }
    }
    return handled
  }

  // Tells JS the press is cancelled, so releasing doesn't also press the child.
  private fun takeGesture(down: MotionEvent) {
    val cancel = MotionEvent.obtain(down).apply { action = MotionEvent.ACTION_CANCEL }
    NativeGestureUtil.notifyNativeGestureStarted(this, cancel)
    cancel.recycle()
    parent?.requestDisallowInterceptTouchEvent(true)
    ownsGesture = true
  }

  // TalkBack and keyboard activation don't go through the gesture detector.
  override fun performClick(): Boolean =
    if (opensOnTap) showMenu(width / 2f, height / 2f) else super.performClick()

  override fun performLongClick(): Boolean =
    if (!opensOnTap) showMenu(width / 2f, height / 2f) else super.performLongClick()

  // Screen readers and keyboards focus the pressable inside, not this view, so the long
  // press goes there too. Runs when accessibility reads the tree or focus enters it.
  override fun addChildrenForAccessibility(outChildren: ArrayList<View>) {
    updateLongClickTarget()
    super.addChildrenForAccessibility(outChildren)
  }

  override fun requestChildFocus(child: View, focused: View?) {
    updateLongClickTarget()
    super.requestChildFocus(child, focused)
  }

  // A listener, not an accessibility action: that installs a delegate, and React Native
  // then skips its own, losing the role and state. Only on a ReactViewGroup, whose
  // onTouchEvent ignores long clicks, so touch stays with the gesture detector.
  private fun updateLongClickTarget() {
    val target = if (opensOnTap || actions.isEmpty()) null else firstPressable(this)
    val previous = longClickTarget?.get()
    if (previous === target) return
    previous?.apply {
      setOnLongClickListener(null)
      isLongClickable = false
    }
    longClickTarget = target?.let(::WeakReference)
    target?.setOnLongClickListener { showMenu(width / 2f, height / 2f) }
  }

  private fun firstPressable(parent: ReactViewGroup): View? {
    for (index in 0 until parent.childCount) {
      val child = parent.getChildAt(index) as? ReactViewGroup ?: continue
      if (child is RNContextMenuView) continue
      if (child.isFocusable || child.isClickable) return child
      firstPressable(child)?.let { return it }
    }
    return null
  }

  private fun showMenu(x: Float, y: Float): Boolean {
    if (actions.isEmpty() || !isAttachedToWindow) return false
    val activity = reactContext.currentActivity ?: return false
    dismissMenu()
    val menu = MenuPopup(this, actions, destructiveColor, ::emitAction)
    if (!menu.show(activity, x, y)) return false
    popup = menu
    return true
  }

  private fun emitAction(actionId: String) {
    UIManagerHelper.getEventDispatcherForReactTag(reactContext, id)
      ?.dispatchEvent(ActionPressEvent(UIManagerHelper.getSurfaceId(this), id, actionId))
  }

  internal fun dismissMenu() {
    popup?.dismiss()
    popup = null
  }

  // A PopupWindow stays up when its anchor's source leaves.
  override fun onDetachedFromWindow() {
    dismissMenu()
    super.onDetachedFromWindow()
  }

  override fun onConfigurationChanged(newConfig: Configuration?) {
    dismissMenu()
    super.onConfigurationChanged(newConfig)
  }
}
