package com.easternsauce.libgdxgame.effect

import com.easternsauce.libgdxgame.util.EsTimer

class Effect {

  protected val effectTimer: EsTimer = EsTimer()

  var effectEndTime: Float = 0f

  var effectActive: Boolean = false

  def activate(effectTime: Float): Unit = {
    if (effectActive) {
      effectEndTime = Math.max(getRemainingTime, effectTime)
      effectTimer.restart()

    } else {
      effectActive = true
      effectTimer.restart()
      effectEndTime = effectTime
    }
  }

  def isActive: Boolean = {
    effectActive
  }

  def update(): Unit = {
    if (effectActive && effectTimer.time > effectEndTime) effectActive = false
  }

  def getRemainingTime: Float = effectEndTime - effectTimer.time

  def stop(): Unit = {
    effectActive = false
    effectEndTime = 0
    effectTimer.stop()
    effectTimer.restart()
  }
}

object Effect {
  def apply() = new Effect()
}
