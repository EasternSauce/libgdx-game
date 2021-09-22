package com.easternsauce.libgdxgame.ability.misc

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.parameters.{AbilityParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

trait Ability {
  val id: String

  val creature: Creature
  val state: AbilityState
  val onCooldown: Boolean
  val soundParameters: SoundParameters
  val timerParameters: TimerParameters

  protected val isStoppable: Boolean = true
  protected val cooldownTime: Float = 0f
  protected lazy val activeTime: Float = 0f
  protected lazy val channelTime: Float = 0f
  protected val isAttack = false

  val channelSound: Option[Sound] = None
  val channelSoundVolume: Option[Float] = None

  def updateHitbox(): AbilityParameters

  protected def onActiveStart(): AbilityParameters = {
    // TODO: remove side effect
    val activeSound = soundParameters.activeSound
    val activeSoundVolume = soundParameters.activeSoundVolume
    if (activeSound.nonEmpty) activeSound.get.play(activeSoundVolume.get)

    AbilityParameters()
  }

  protected def onUpdateActive(): AbilityParameters

  protected def onUpdateChanneling(): AbilityParameters

  def render(esBatch: EsBatch): AbilityParameters

  def forceStop(): AbilityParameters = {

    if (isStoppable && state != AbilityState.Inactive) {
      onStop().copy(state = Some(AbilityState.Inactive))
    } else {
      AbilityParameters()
    }

  }

  protected def onStop(): AbilityParameters

  def perform(): AbilityParameters = {
    val channelTimer = timerParameters.channelTimer

    if (creature.staminaPoints > 0 && state == AbilityState.Inactive && !onCooldown && !creature.abilityActive) {

      // TODO: remove side effect
      channelTimer.restart()

      // TODO: remove side effect
      // + 0.01 to ensure regen doesn't start if we hold attack button
      creature.activateEffect("staminaRegenerationStopped", if (isAttack) channelTime + cooldownTime + 0.01f else 1f)

      onChannellingStart().copy(state = Some(AbilityState.Channeling))
    } else
      AbilityParameters()
  }

  def update(): AbilityParameters = {
    val channelTimer = timerParameters.channelTimer
    val activeTimer = timerParameters.activeTimer

    import AbilityState._
    state match {
      case Channeling =>
        val params = if (channelTimer.time > channelTime) {
          activeTimer.restart()
          onActiveStart().copy(state = Some(AbilityState.Active), onCooldown = Some(true))
        } else
          AbilityParameters()

        params
          .add(updateHitbox())
          .add(onUpdateChanneling())

      case Active =>
        val params = if (activeTimer.time > activeTime) {
          AbilityParameters(state = Some(AbilityState.Inactive))
            .add(onStop())
        } else
          AbilityParameters()

        params
          .add(updateHitbox())
          .add(onUpdateActive())

      case Inactive =>
        if (onCooldown && activeTimer.time > cooldownTime) {
          AbilityParameters(onCooldown = Some(false))
        } else
          AbilityParameters()

      case _ => AbilityParameters()
    }
  }

  def onChannellingStart(): AbilityParameters = {
    if (channelSound.nonEmpty) {
      // TODO: remove side effect
      channelSound.get.play(channelSoundVolume.get)
    }

    AbilityParameters()
  }

  def active: Boolean = state == AbilityState.Active

  def onCollideWithCreature(creature: Creature): AbilityParameters

  def asMapEntry: (String, Ability) = id -> this

  def applyParams(params: AbilityParameters): Ability
}
