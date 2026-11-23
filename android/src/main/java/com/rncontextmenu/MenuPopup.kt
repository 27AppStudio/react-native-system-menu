package com.rncontextmenu

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.View.MeasureSpec
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuItemCompat
import com.facebook.react.bridge.ReadableArray

internal data class MenuAction(
  val id: String,
  val title: String,
  val icon: String?,
  val destructive: Boolean,
) {
  companion object {
    fun listFrom(array: ReadableArray?): List<MenuAction> {
      array ?: return emptyList()
      return (0 until array.size()).mapNotNull { index ->
        val map = array.getMap(index) ?: return@mapNotNull null
        MenuAction(
          id = map.getString("id") ?: return@mapNotNull null,
          title = map.getString("title").orEmpty(),
          icon = if (map.hasKey("androidIcon")) map.getString("androidIcon") else null,
          destructive = map.hasKey("destructive") && map.getBoolean("destructive"),
        )
      }
    }
  }
}

/** One showing of the menu: themed when shown, so it follows the current night mode. */
internal class MenuPopup(
  private val host: View,
  private val actions: List<MenuAction>,
  private val destructiveColor: Int?,
  private val onSelect: (String) -> Unit,
) {
  private var popup: PopupMenu? = null
  private var anchor: View? = null

  /** Opens with its top start corner at ([x], [y]), in the host's coordinates. */
  fun show(activity: Context, x: Float, y: Float): Boolean {
    // The host's own window, which differs from the activity's inside a Modal.
    val content = host.rootView.findViewById<FrameLayout>(android.R.id.content) ?: return false
    val context = ContextThemeWrapper(activity, R.style.Theme_RNContextMenu)

    val anchor = addAnchor(content, x, y + backgroundTopPadding(context))
    // androidx reads popupMenuStyle by default, which doesn't overlap its anchor.
    val popup = PopupMenu(context, anchor, Gravity.NO_GRAVITY, android.R.attr.contextPopupMenuStyle, 0)
    popup.setForceShowIcon(true)

    actions.forEachIndexed { index, action ->
      val color = destructiveColor?.takeIf { action.destructive }
      val title: CharSequence =
        if (color == null) action.title
        else SpannableString(action.title).apply {
          setSpan(ForegroundColorSpan(color), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
      val item = popup.menu.add(Menu.NONE, index, index, title)
      action.icon?.let { name ->
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (id != 0) item.icon = AppCompatResources.getDrawable(context, id)?.mutate()
      }
      if (color != null) MenuItemCompat.setIconTintList(item, ColorStateList.valueOf(color))
    }

    popup.setOnMenuItemClickListener { item ->
      actions.getOrNull(item.itemId)?.let { onSelect(it.id) }
      true
    }
    popup.setOnDismissListener { removeAnchor() }
    this.popup = popup
    popup.show()
    return true
  }

  fun dismiss() {
    popup?.dismiss()
    popup = null
    removeAnchor()
  }

  // The popup pulls itself up by its background's padding, to line its first row up with
  // the anchor. Pushing the anchor down by as much puts the menu's own corner at the touch.
  private fun backgroundTopPadding(context: Context): Int {
    val style =
      context.obtainStyledAttributes(
        null,
        intArrayOf(android.R.attr.popupBackground),
        android.R.attr.contextPopupMenuStyle,
        0,
      )
    val padding = Rect()
    style.getDrawable(0)?.getPadding(padding)
    style.recycle()
    return padding.top
  }

  // Outside Fabric's tree, which mounts children by index. As tall as the host, so the
  // menu flips above it near the bottom of the screen.
  private fun addAnchor(content: FrameLayout, x: Float, y: Float): View {
    val hostAt = IntArray(2).also(host::getLocationInWindow)
    val contentAt = IntArray(2).also(content::getLocationInWindow)
    val left = hostAt[0] - contentAt[0] + x.toInt()
    val top = hostAt[1] - contentAt[1] + y.toInt()
    val height = host.height.coerceAtLeast(1)

    val view = View(host.context)
    view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    view.layoutDirection = host.layoutDirection
    val params = FrameLayout.LayoutParams(1, height, Gravity.TOP or Gravity.LEFT)
    params.leftMargin = left
    params.topMargin = top
    content.addView(view, params)
    // The popup measures its anchor now, before the next layout pass.
    view.measure(
      MeasureSpec.makeMeasureSpec(1, MeasureSpec.EXACTLY),
      MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
    )
    view.layout(left, top, left + 1, top + height)
    anchor = view
    return view
  }

  private fun removeAnchor() {
    val view = anchor ?: return
    anchor = null
    // Posted: the popup may still be reading it while it animates out.
    view.post { (view.parent as? FrameLayout)?.removeView(view) }
  }
}
