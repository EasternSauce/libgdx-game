package com.easternsauce.libgdxgame.hud

import com.badlogic.gdx.graphics.Color
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.util.{EsBatch, EsTimer}

import scala.collection.mutable.ListBuffer

class NotificationText {

  val notifications: ListBuffer[(EsTimer, String)] = ListBuffer[(EsTimer, String)]()

  val notificationTimeout = 3f

  def render(batch: EsBatch): Unit = {
    RpgGame.defaultFont.setColor(Color.RED)
    for (i <- notifications.indices) {
      val (_, text) = notifications(i)
      RpgGame.defaultFont.draw(batch.spriteBatch, text, 20, RpgGame.WindowHeight - (40 + i * 30))
    }

  }

  def update(): Unit = {
    notifications.filterInPlace { case (timer, _) => timer.time <= notificationTimeout }
  }

  def showNotification(text: String): Unit = {
    notifications.prepend((EsTimer(true), "> " + text))
  }
}
