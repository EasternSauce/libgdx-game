package com.easternsauce.libgdxgame.ability.misc

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.parameters.{SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

trait Ability {
  val creature: Creature
  val state: AbilityState
  val onCooldown: Boolean
  val timerParameters: TimerParameters

  val id: String

  val soundParameters: SoundParameters = SoundParameters()

  protected val isStoppable: Boolean = true

  protected val cooldownTime: Float
  protected lazy val activeTime: Float = 0f
  protected lazy val channelTime: Float = 0f
  protected val isAttack = false

  val channelSound: Option[Sound] = None
  val channelSoundVolume: Option[Float] = None

  def updateHitbox(): Ability = this

  def onActiveStart(): Ability = {
    // TODO: remove side effect
    val activeSound = soundParameters.activeSound
    val activeSoundVolume = soundParameters.activeSoundVolume
    if (activeSound.nonEmpty) activeSound.get.play(activeSoundVolume.get)

    this
  }

  def onUpdateActive(): Ability = this

  def onUpdateChanneling(): Ability = this

  def render(esBatch: EsBatch): Ability = this

  def forceStop(): Ability = {

    if (isStoppable && state != AbilityState.Inactive) {
      this
        .onStop()
        .setState(state = AbilityState.Inactive)
    } else {
      this
    }

  }

  def onStop(): Ability = this

  def perform(): Ability = {
    val channelTimer = timerParameters.channelTimer

    if (creature.staminaPoints > 0 && state == AbilityState.Inactive && !onCooldown && !creature.abilityActive) {

      // TODO: remove side effect
      channelTimer.restart()

      // TODO: remove side effect
      // + 0.01 to ensure regen doesn't start if we hold attack button
      creature.activateEffect("staminaRegenerationStopped", if (isAttack) channelTime + cooldownTime + 0.01f else 1f)

      this
        .onChannellingStart()
        .setState(state = AbilityState.Channeling)
    } else
      this
  }

  def update(): Ability = {
    val channelTimer = timerParameters.channelTimer
    val activeTimer = timerParameters.activeTimer

    import AbilityState._
    state match {
      case Channeling =>
        val ability: Ability = if (channelTimer.time > channelTime) {
          activeTimer.restart()

          this
            .onActiveStart()
            .setState(state = AbilityState.Active)
            .setOnCooldown(onCooldown = true)
        } else
          this

        ability
          .updateHitbox()
          .onUpdateChanneling()

      case Active =>
        val ability: Ability = if (activeTimer.time > activeTime) {
          setState(state = AbilityState.Inactive)
            .onStop()
        } else
          this

        ability
          .updateHitbox()
          .onUpdateActive()

      case Inactive =>
        if (onCooldown && activeTimer.time > cooldownTime) {

          this
            .setOnCooldown(onCooldown = false)
        } else
          this

      case _ => this
    }

  }

  def onChannellingStart(): Ability = {
    if (channelSound.nonEmpty) {
      // TODO: remove side effect
      channelSound.get.play(channelSoundVolume.get)
    }

    this
  }

  def onCollideWithCreature(creature: Creature): Ability = this

  def active: Boolean = state == AbilityState.Active

  def asMapEntry: (String, Ability) = id -> this

  def setState(state: AbilityState): Ability

  def setOnCooldown(onCooldown: Boolean): Ability
}
