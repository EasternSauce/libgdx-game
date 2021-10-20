package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.effect.Effect
import com.easternsauce.libgdxgame.util.EsTimer

import scala.collection.mutable

trait Effects {
  this: Creature =>

  var effectMap: mutable.Map[String, Effect] = mutable.Map()

  def updateEffects(): Creature = {
    for (effect <- effectMap.values) {
      effect.update()
    }

    this
  }

  protected def defineEffects(): Creature = {
    def initEffect(effectName: String): Creature = {
      effectMap.put(effectName, Effect())

      this
    }

    initEffect("immune")
    initEffect("immobilized")
    initEffect("staminaRegenerationStopped")
    initEffect("poisoned")
    initEffect("knockedBack")

    this
  }

  private def effect(effectName: String): Effect = {
    effectMap.get(effectName) match {
      case Some(effect) => effect
      case _ =>
        throw new RuntimeException("tried to access non-existing effect: " + effectName)
    }
  }

  def activateEffect(effectName: String, time: Float): Creature = {
    effect(effectName).activate(time)

    this
  }

  def isEffectActive(effectName: String): Boolean = {
    effect(effectName).isActive
  }

  def isImmune: Boolean = effect("immune").isActive

  def ableToMove: Boolean = !effect("immobilized").isActive && !effect("knockedBack").isActive

  // knockbacks

  val isKnockbackable = true
  var knockbackVector = new Vector2(0f, 0f)

  var knockbackVelocity: Float = 0f

  def handleKnockback(): Creature = {
    if (effect("knockedBack").isActive) {
      sustainVelocity(new Vector2(knockbackVector.x * knockbackVelocity, knockbackVector.y * knockbackVelocity))
    }

    this
  }

  // poison

  val poisonTickTimer: EsTimer = EsTimer()

  protected val poisonTickTime = 0.8f
  protected val poisonTime = 8f

  def handlePoison(): Creature = {
    if (effect("poisoned").isActive) {
      if (poisonTickTimer.time > poisonTickTime) {
        val poisonDamage = 16f
        takeLifeDamage(poisonDamage, immunityFrames = false)
        poisonTickTimer.restart()
      }
    }

    this
  }
}
