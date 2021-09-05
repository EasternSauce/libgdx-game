package com.easternsauce.libgdxgame.bossfight

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.easternsauce.libgdxgame.creature.traits.Boss
import com.easternsauce.libgdxgame.system.GameSystem._
import com.easternsauce.libgdxgame.system.{Constants, Fonts}
import com.easternsauce.libgdxgame.util.EsBatch

class BossLifeBar {
  var boss: Boss = _
  private var visible: Boolean = false
  private var maxLifeRect: Rectangle = _
  private var lifeRect: Rectangle = _

  def render(batch: EsBatch): Unit = {
    if (visible && boss != null) {
      Fonts.defaultFont.draw(
        batch.spriteBatch,
        boss.name,
        Constants.WindowWidth / 2f - 60,
        Constants.WindowHeight - 10,
        Color.WHITE
      )
      batch.shapeDrawer.setColor(Color.ORANGE)
      batch.shapeDrawer.filledRectangle(maxLifeRect)
      batch.shapeDrawer.setColor(Color.RED)
      batch.shapeDrawer.filledRectangle(lifeRect)
    }
  }

  def onBossBattleStart(boss: Boss): Unit = {
    this.boss = boss
    visible = true
  }

  def hide(): Unit = {
    visible = false
  }

  def update(): Unit = {
    if (visible && boss != null) {
      maxLifeRect =
        new Rectangle(Constants.WindowWidth.toFloat / 2f - 250f, Constants.WindowHeight.toFloat - 35f, 500, 10)
      lifeRect = new Rectangle(
        Constants.WindowWidth / 2f - 250,
        Constants.WindowHeight.toFloat - 35f,
        500 * boss.life / boss.maxLife,
        10
      )
    }
  }
}
