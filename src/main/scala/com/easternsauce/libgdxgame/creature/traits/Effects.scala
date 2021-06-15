package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.effect.Effect
import com.easternsauce.libgdxgame.util.EsTimer

import scala.collection.mutable

trait Effects {
  this: Creature =>

  val effectMap: mutable.Map[String, Effect] = mutable.Map()

  def updateEffects(): Unit = {
    for (effect <- effectMap.values) {
      effect.update()
    }
  }

  protected def defineEffects(): Unit = {
    def initEffect(effectName: String): Unit = {
      effectMap.put(effectName, Effect())
    }

    initEffect("immune")
    initEffect("immobilized")
    initEffect("staminaRegenerationStopped")
    initEffect("poisoned")
    initEffect("knockedBack")

  }

  def effect(effectName: String): Effect = {
    effectMap.get(effectName) match {
      case Some(effect) => effect
      case _ =>
        throw new RuntimeException("tried to access non-existing effect: " + effectName)
    }
  }

  def immune: Boolean = effect("immune").isActive

  def ableToMove: Boolean = !effect("immobilized").isActive && !effect("knockedBack").isActive

  // knockbacks

  val knockbackable = true
  var knockbackVector = new Vector2(0f, 0f)

  protected var knockbackSpeed: Float = 0f
  protected val knockbackPower = 0f

  def handleKnockback(): Unit = {
    if (effect("knockedBack").isActive) {
      sustainVelocity(new Vector2(knockbackVector.x * knockbackSpeed, knockbackVector.y * knockbackSpeed))
    }
  }

  // poison

  protected val poisonTickTimer: EsTimer = EsTimer()

  protected val poisonTickTime = 1.5f
  protected val poisonTime = 20f

  def handlePoison(): Unit = {
    if (effect("poisoned").isActive) {
      if (poisonTickTimer.time > poisonTickTime) {
        takeLifeDamage(15f, immunityFrames = false)
        poisonTickTimer.restart()
      }
    }
  }
}
