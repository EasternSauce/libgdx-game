package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.Gdx
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsTimer
import com.softwaremill.quicklens.ModifyPimp

trait Stamina {
  this: Creature =>

  var staminaRegenerationTimer: EsTimer = EsTimer(true)
  var staminaOveruseTimer: EsTimer = EsTimer()

  protected val staminaRegeneration = 0.8f

  protected val staminaOveruseTime = 1.3f

  protected val staminaRegenerationTickTime = 0.005f

  var staminaDrain = 0.0f

  def takeStaminaDamage(staminaDamage: Float): Creature = {
    if (params.staminaPoints - staminaDamage > 0) {
      this
        .modify(_.params.staminaPoints)
        .setTo(params.staminaPoints - staminaDamage)
    } else {
      staminaOveruseTimer.restart()

      this
        .modify(_.params.staminaPoints)
        .setTo(0f)
        .modify(_.params.staminaOveruse)
        .setTo(true)
    }
  }

  def updateStamina(): Creature = {
    if (staminaDrain > 0.005f) {

      staminaDrain = 0.0f

      this.takeStaminaDamage(0.2f)
    } else this
  }

  def regenerateStamina(): Creature = {
    if (sprinting && params.staminaPoints > 0) {
      staminaDrain += Gdx.graphics.getDeltaTime
    }

    val updated1 = if (!isEffectActive("staminaRegenerationStopped") && !sprinting) {
      if (staminaRegenerationTimer.time > staminaRegenerationTickTime && !abilityActive && !params.staminaOveruse) {
        staminaRegenerationTimer.restart()

        if (params.staminaPoints < params.maxStaminaPoints) {
          val afterRegeneration = params.staminaPoints + staminaRegeneration
          this.modify(_.params.staminaPoints).setTo(Math.min(afterRegeneration, params.maxStaminaPoints))
        } else this
      } else this
    } else this

    if (params.staminaOveruse) {
      if (staminaOveruseTimer.time > staminaOveruseTime) updated1.modify(_.params.staminaOveruse).setTo(false)
      else updated1
    } else updated1
  }

}
