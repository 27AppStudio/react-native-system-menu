package com.rncontextmenu

import android.graphics.Color
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Collections
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** The popup against the real AppCompat menu with the library's Material 3 theme. */
@RunWith(AndroidJUnit4::class)
class MenuPopupTest {
  private lateinit var scenario: ActivityScenario<TestActivity>
  private val picked: MutableList<String> = Collections.synchronizedList(mutableListOf())
  private var popup: MenuPopup? = null

  private val actions =
    listOf(
      MenuAction("invite", "Invite", ICON, destructive = false),
      MenuAction("edit", "Edit", null, destructive = false),
      MenuAction("delete", "Delete", ICON, destructive = true),
    )

  @Before
  fun setUp() {
    nightMode(AppCompatDelegate.MODE_NIGHT_NO)
    scenario = ActivityScenario.launch(TestActivity::class.java)
  }

  @After
  fun tearDown() {
    scenario.onActivity { popup?.dismiss() }
    idle()
    scenario.close()
    nightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
  }

  @Test
  fun showsEveryItemInOrder() {
    show()
    for (title in listOf("Invite", "Edit", "Delete")) {
      onView(withText(title)).inRoot(isPlatformPopup()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun reportsThePickedId() {
    show()
    onView(withText("Edit")).inRoot(isPlatformPopup()).perform(click())
    idle()
    assertEquals(listOf("edit"), picked)
  }

  @Test
  fun colorsDestructiveItems() {
    show(destructiveColor = RED)
    onView(withText("Delete")).inRoot(isPlatformPopup()).check { view, _ ->
      val text = (view as TextView).text as Spanned
      val spans = text.getSpans(0, text.length, ForegroundColorSpan::class.java)
      assertEquals(RED, spans.single().foregroundColor)
    }
    onView(withText("Edit")).inRoot(isPlatformPopup()).check { view, _ ->
      val text = (view as TextView).text
      assertTrue(text !is Spanned || text.getSpans(0, text.length, ForegroundColorSpan::class.java).isEmpty())
    }
  }

  @Test
  fun opensWithItsCornerAtTheTouch() {
    show(x = 120f, y = 150f)
    onView(withText("Invite")).inRoot(isPlatformPopup()).check { view, _ ->
      val menu = view.rootView
      val at = IntArray(2).also(menu::getLocationOnScreen)
      val card = IntArray(2)
      scenario.onActivity { it.card.getLocationOnScreen(card) }
      // The popup's window may add a few pixels of shadow inset around the menu.
      assertTrue("left ${at[0]} vs ${card[0] + 120}", Math.abs(at[0] - (card[0] + 120)) < 48)
      assertTrue("top ${at[1]} vs ${card[1] + 150}", Math.abs(at[1] - (card[1] + 150)) < 48)
    }
  }

  @Test
  fun removesItsAnchorWhenDismissed() {
    show()
    scenario.onActivity { activity ->
      assertEquals(2, content(activity).childCount)
      popup?.dismiss()
    }
    idle()
    scenario.onActivity { activity -> assertEquals(1, content(activity).childCount) }
  }

  @Test
  fun followsNightMode() {
    // AppCompat recreates the activity itself.
    nightMode(AppCompatDelegate.MODE_NIGHT_YES)
    idle()
    show()
    onView(withText("Edit")).inRoot(isPlatformPopup()).check { view, _ ->
      val color = (view as TextView).currentTextColor
      // Material 3's dark surface text is light.
      assertTrue("text color ${Integer.toHexString(color)}", Color.luminance(color) > 0.5f)
    }
  }

  private fun show(x: Float = 20f, y: Float = 20f, destructiveColor: Int? = null) {
    scenario.onActivity { activity ->
      popup = MenuPopup(activity.card, actions, destructiveColor) { picked += it }
      assertTrue(popup!!.show(activity, x, y))
    }
    idle()
  }

  private fun content(activity: TestActivity): FrameLayout =
    activity.findViewById<View>(android.R.id.content) as FrameLayout

  private fun nightMode(mode: Int) =
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      AppCompatDelegate.setDefaultNightMode(mode)
    }

  private fun idle() = InstrumentationRegistry.getInstrumentation().waitForIdleSync()

  private companion object {
    const val ICON = "ic_test"
    val RED = Color.rgb(255, 59, 48)
  }
}
