package com.easternsauce.libgdxgame.effect

import com.easternsauce.libgdxgame.util.EsTimer

class Effect {

  protected val effectTimer: EsTimer = EsTimer()

  protected var effectEndTime: Float = 0f

  protected var effectActive: Boolean = false

  def applyEffect(effectTime: Float): Unit = {
    if (effectActive) {
      effectTimer.restart()
      val remainingTime = effectEndTime - effectTimer.time
      effectEndTime = Math.max(remainingTime, effectTime)
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
