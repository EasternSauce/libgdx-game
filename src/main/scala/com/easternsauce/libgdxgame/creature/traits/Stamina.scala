package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.Gdx
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsTimer

trait Stamina {
  this: Creature =>

  val maxStaminaPoints = 100f
  var staminaPoints: Float = maxStaminaPoints

  protected val staminaRegenerationTimer: EsTimer = EsTimer(true)
  protected val staminaOveruseTimer: EsTimer = EsTimer()

  protected val staminaRegeneration = 0.8f

  var staminaOveruse = false
  protected val staminaOveruseTime = 1.3f

  protected val staminaRegenerationTickTime = 0.005f

  protected var staminaDrain = 0.0f

  def takeStaminaDamage(staminaDamage: Float): Unit = {
    if (staminaPoints - staminaDamage > 0) staminaPoints -= staminaDamage
    else {
      staminaPoints = 0f
      staminaOveruse = true
      staminaOveruseTimer.restart()
    }
  }

  def updateStamina(): Unit = {
    if (staminaDrain > 0.005f) {
      takeStaminaDamage(0.2f)

      staminaDrain = 0.0f
    }
  }

  def regenerateStamina(): Unit = {
    if (sprinting && staminaPoints > 0) {
      staminaDrain += Gdx.graphics.getDeltaTime
    }

    if (!isEffectActive("staminaRegenerationStopped") && !sprinting) {
      if (staminaRegenerationTimer.time > staminaRegenerationTickTime && !abilityActive && !staminaOveruse) {
        if (staminaPoints < maxStaminaPoints) {
          val afterRegeneration = staminaPoints + staminaRegeneration
          staminaPoints = Math.min(afterRegeneration, maxStaminaPoints)
        }
        staminaRegenerationTimer.restart()
      }
    }

    if (staminaOveruse) {
      if (staminaOveruseTimer.time > staminaOveruseTime) staminaOveruse = false
    }

  }

  def updateStaminaDrain(): Unit = {}

}
