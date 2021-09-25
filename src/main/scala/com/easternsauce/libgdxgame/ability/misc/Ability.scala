package com.easternsauce.libgdxgame.ability.misc

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.parameters.{SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

class Ability protected (
  val creature: Creature,
  val state: AbilityState,
  val onCooldown: Boolean,
  val timerParameters: TimerParameters
) {

  val id: String = "INCORRECT_ID"

  val soundParameters: SoundParameters = SoundParameters()

  protected val isStoppable: Boolean = true

  protected val cooldownTime: Float = 0f
  protected lazy val activeTime: Float = 0f
  protected lazy val channelTime: Float = 0f
  protected val isAttack = false

  val channelSound: Option[Sound] = None
  val channelSoundVolume: Option[Float] = None

  def updateHitbox(): Ability = makeAbilityCopy()

  def onActiveStart(): Ability = {
    // TODO: remove side effect
    val activeSound = soundParameters.activeSound
    val activeSoundVolume = soundParameters.activeSoundVolume
    if (activeSound.nonEmpty) activeSound.get.play(activeSoundVolume.get)

    makeAbilityCopy()
  }

  def onUpdateActive(): Ability = makeAbilityCopy()

  def onUpdateChanneling(): Ability = makeAbilityCopy()

  def render(esBatch: EsBatch): Ability = makeAbilityCopy()

  def forceStop(): Ability = {

    if (isStoppable && state != AbilityState.Inactive) {
      onStop()
        .makeAbilityCopy(state = AbilityState.Inactive)
    } else {
      makeAbilityCopy()
    }

  }

  def onStop(): Ability = makeAbilityCopy()

  def perform(): Ability = {
    val channelTimer = timerParameters.channelTimer

    if (creature.staminaPoints > 0 && state == AbilityState.Inactive && !onCooldown && !creature.abilityActive) {

      // TODO: remove side effect
      channelTimer.restart()

      // TODO: remove side effect
      // + 0.01 to ensure regen doesn't start if we hold attack button
      creature.activateEffect("staminaRegenerationStopped", if (isAttack) channelTime + cooldownTime + 0.01f else 1f)

      onChannellingStart()
        .makeAbilityCopy(state = AbilityState.Channeling)
    } else
      makeAbilityCopy()
  }

  def update(): Ability = {
    val channelTimer = timerParameters.channelTimer
    val activeTimer = timerParameters.activeTimer

    import AbilityState._
    state match {
      case Channeling =>
        val ability: Ability = if (channelTimer.time > channelTime) {
          activeTimer.restart()

          onActiveStart()
            .makeAbilityCopy(state = AbilityState.Active, onCooldown = true)
        } else
          makeAbilityCopy()

        ability
          .updateHitbox()
          .onUpdateChanneling()

      case Active =>
        val ability: Ability = if (activeTimer.time > activeTime) {
          makeAbilityCopy(state = AbilityState.Inactive)
            .onStop()
        } else
          makeAbilityCopy()

        ability
          .updateHitbox()
          .onUpdateActive()

      case Inactive =>
        if (onCooldown && activeTimer.time > cooldownTime) {
          makeAbilityCopy(onCooldown = false)
        } else
          makeAbilityCopy()

      case _ => makeAbilityCopy()
    }

  }

  def onChannellingStart(): Ability = {
    if (channelSound.nonEmpty) {
      // TODO: remove side effect
      channelSound.get.play(channelSoundVolume.get)
    }

    makeAbilityCopy()
  }

  def onCollideWithCreature(creature: Creature): Ability = makeAbilityCopy()

  def active: Boolean = state == AbilityState.Active

  def asMapEntry: (String, Ability) = id -> this

  private def makeAbilityCopy(
    creature: Creature = creature,
    state: AbilityState = state,
    onCooldown: Boolean = onCooldown,
    timerParameters: TimerParameters = timerParameters
  ): Ability = {
    Ability(creature, state, onCooldown, timerParameters)
  }

}

object Ability {
  def apply(creature: Creature, state: AbilityState, onCooldown: Boolean, timerParameters: TimerParameters): Ability = {
    println("creating ability")

    new Ability(creature, state, onCooldown, timerParameters)
  }
}
