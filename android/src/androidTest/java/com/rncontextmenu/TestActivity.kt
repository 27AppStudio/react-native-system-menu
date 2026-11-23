package com.rncontextmenu

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

/** A card in the top left of a plain screen, standing in for a React Native view. */
class TestActivity : AppCompatActivity() {
  lateinit var card: View

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val root = FrameLayout(this)
    card = View(this)
    root.addView(card, FrameLayout.LayoutParams(400, 400).apply { leftMargin = 40; topMargin = 300 })
    setContentView(root)
  }
}
