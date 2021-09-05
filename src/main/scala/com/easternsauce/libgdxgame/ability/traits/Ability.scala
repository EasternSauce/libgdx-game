package com.easternsauce.libgdxgame.ability.traits

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.ability.AbilityState
import com.easternsauce.libgdxgame.ability.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.{EsBatch, EsTimer}

trait Ability {
  val id: String
  val creature: Creature
  protected val isStoppable: Boolean = true
  var state: AbilityState = Inactive
  var onCooldown = false
  protected val activeTimer: EsTimer = EsTimer()
  protected val channelTimer: EsTimer = EsTimer()
  protected val cooldownTime: Float
  protected def activeTime: Float
  protected def channelTime: Float
  protected val isAttack = false

  var channelSound: Option[Sound] = None
  var channelSoundVolume: Option[Float] = None
  var activeSound: Option[Sound] = None
  var activeSoundVolume: Option[Float] = None

  def updateHitbox(): Unit = {}

  protected def onActiveStart(): Unit = {
    if (activeSound.nonEmpty) activeSound.get.play(activeSoundVolume.get)
  }

  protected def onUpdateActive(): Unit = {}

  protected def onUpdateChanneling(): Unit = {}

  def render(esBatch: EsBatch): Unit = {}

  def forceStop(): Unit = {
    if (isStoppable && state != AbilityState.Inactive) {
      onStop()

      state = AbilityState.Inactive
    }
  }

  protected def onStop(): Unit = {}

  def perform(): Unit = {
    if (creature.staminaPoints > 0 && (state == AbilityState.Inactive) && !onCooldown && !creature.abilityActive) {
      channelTimer.restart()
      state = AbilityState.Channeling
      onChannellingStart()

      if (isAttack) { // + 0.01 to ensure regen doesn't start if we hold attack button
        creature.activateEffect("staminaRegenerationStopped", channelTime + cooldownTime + 0.01f)
      } else {
        creature.activateEffect("staminaRegenerationStopped", 1f)
      }
    }
  }

  def update(): Unit = {

    import com.easternsauce.libgdxgame.ability.AbilityState._
    state match {
      case Channeling =>
        if (channelTimer.time > channelTime) {
          state = AbilityState.Active
          onActiveStart()
          activeTimer.restart()
          onCooldown = true
        }
        updateHitbox()
        onUpdateChanneling()
      case Active =>
        if (activeTimer.time > activeTime) {
          onStop()

          state = AbilityState.Inactive
        }
        updateHitbox()
        onUpdateActive()
      case Inactive =>
        if (onCooldown && activeTimer.time > cooldownTime) onCooldown = false
    }
  }

  def onChannellingStart(): Unit = {
    if (channelSound.nonEmpty) {
      channelSound.get.play(channelSoundVolume.get)
    }

  }

  def active: Boolean = {
    state == AbilityState.Active
  }

  def onCollideWithCreature(creature: Creature): Unit = {}
}
