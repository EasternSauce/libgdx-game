package com.easternsauce.libgdxgame.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsBatch

class PlayerHealthStaminaBar(val playScreen: PlayScreen) {

  private var maxHealthRect = new Rectangle(10, 40, 100, 10)
  private var healthRect =
    new Rectangle(10, 40, 100 * playScreen.player.healthPoints / playScreen.player.maxHealthPoints, 10)
  private var maxStaminaRect = new Rectangle(10, 25, 100, 10)
  private var staminaRect =
    new Rectangle(10, 25, 100 * playScreen.player.staminaPoints / playScreen.player.maxStaminaPoints, 10)

  def render(batch: EsBatch): Unit = {
    batch.shapeDrawer.filledRectangle(maxHealthRect, Color.ORANGE)
    batch.shapeDrawer.filledRectangle(healthRect, Color.RED)
    batch.shapeDrawer.filledRectangle(maxStaminaRect, Color.ORANGE)
    batch.shapeDrawer.filledRectangle(staminaRect, Color.GREEN)
  }

  def update(): Unit = {
    maxHealthRect = new Rectangle(10, 40, 100, 10)
    healthRect = new Rectangle(10, 40, 100 * playScreen.player.healthPoints / playScreen.player.maxHealthPoints, 10)
    maxStaminaRect = new Rectangle(10, 25, 100, 10)
    staminaRect = new Rectangle(10, 25, 100 * playScreen.player.staminaPoints / playScreen.player.maxStaminaPoints, 10)
  }
}
