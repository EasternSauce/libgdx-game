package com.easternsauce.libgdxgame.ability.hud

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.easternsauce.libgdxgame.util.EsBatch

class InventoryWindow {
  var visible = false

  private val background: Rectangle = new Rectangle(
    (Gdx.graphics.getWidth * 0.2).toInt,
    (Gdx.graphics.getHeight * 0.3).toInt,
    (Gdx.graphics.getWidth * 0.6).toInt,
    (Gdx.graphics.getHeight * 0.6).toInt
  )

  def render(batch: EsBatch): Unit = {
    if (visible) {
      batch.shapeDrawer.filledRectangle(background, Color.LIGHT_GRAY)
    }

  }
}
