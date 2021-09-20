package com.easternsauce.libgdxgame.util

import com.badlogic.gdx.Gdx

import scala.collection.mutable.ListBuffer

class EsTimer private (var isStarted: Boolean = false) {
  var time: Float = 0

  EsTimer.timerList += this

  def start(): Unit = isStarted = true

  def stop(): Unit = isStarted = false

  def restart(): Unit = {
    time = 0
    isStarted = true
  }

  private def update(delta: Float): Unit = {
    if (isStarted) {
      time = time + delta
    }
  }

}

object EsTimer {
  private val timerList: ListBuffer[EsTimer] = ListBuffer()

  def apply(): EsTimer = new EsTimer()

  def apply(isStarted: Boolean): EsTimer = new EsTimer(isStarted)

  def updateTimers(): Unit =
    timerList.foreach(_.update(Gdx.graphics.getDeltaTime))
}
