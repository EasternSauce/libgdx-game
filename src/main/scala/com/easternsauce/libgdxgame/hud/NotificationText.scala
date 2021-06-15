package com.easternsauce.libgdxgame.hud

import com.badlogic.gdx.graphics.Color
import com.easternsauce.libgdxgame.system.GameSystem._
import com.easternsauce.libgdxgame.system.{Constants, Fonts}
import com.easternsauce.libgdxgame.util.{EsBatch, EsTimer}

import scala.collection.mutable.ListBuffer

class NotificationText {

  val notifications: ListBuffer[(EsTimer, String)] = ListBuffer[(EsTimer, String)]()

  val notificationTimeout = 3f

  def render(batch: EsBatch): Unit = {
    for (i <- notifications.indices) {
      val (_, text) = notifications(i)
      Fonts.defaultFont.draw(batch.spriteBatch, text, 20, Constants.WindowHeight - (40 + i * 30), Color.RED)
    }

  }

  def update(): Unit = {
    notifications.filterInPlace { case (timer, _) => timer.time <= notificationTimeout }
  }

  def showNotification(text: String): Unit = {
    notifications.prepend((EsTimer(true), "> " + text))
  }
}
