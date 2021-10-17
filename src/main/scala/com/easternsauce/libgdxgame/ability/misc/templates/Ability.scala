package com.easternsauce.libgdxgame.ability.misc.templates

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.parameters.{AnimationParameters, BodyParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.ability.misc.templates.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.GameSystem
import com.easternsauce.libgdxgame.util.EsBatch
import com.softwaremill.quicklens._

abstract class Ability(
  val creatureId: String,
  val state: AbilityState = Inactive,
  val onCooldown: Boolean = false,
  val components: List[AbilityComponent] = List(),
  val lastComponentFinishTime: Float = 0,
  val timerParameters: TimerParameters = TimerParameters(),
  val soundParameters: SoundParameters = SoundParameters(),
  val bodyParameters: BodyParameters = BodyParameters(),
  val animationParameters: AnimationParameters = AnimationParameters(),
  val dirVector: Vector2 = new Vector2(0f, 0f)
) {
  type Self >: this.type <: Ability

  val id: String

  val activeAnimation: Option[Animation] = None
  val channelAnimation: Option[Animation] = None

  protected val isStoppable: Boolean = true

  protected val cooldownTime: Float
  protected lazy val activeTime: Float = 0f
  protected lazy val channelTime: Float = 0f
  protected val isAttack = false

  val channelSound: Option[Sound] = None
  val channelSoundVolume: Option[Float] = None

  def creature: Creature = GameSystem.creature(creatureId)
  def modifyCreature(modification: Creature => Creature): Unit = GameSystem.modifyCreature(creatureId, modification)

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
        .modify(_.state)
        .setTo(AbilityState.Inactive)
    } else {
      this
    }

  }

  def onStop(): Ability = this

  def perform(): Ability = {
    val channelTimer = timerParameters.channelTimer

    val creature = GameSystem.creature(creatureId)
    if (creature.staminaPoints > 0 && state == AbilityState.Inactive && !onCooldown && !creature.abilityActive) {

      // TODO: remove side effect
      channelTimer.restart()

      // TODO: remove side effect
      // + 0.01 to ensure regen doesn't start if we hold attack button
      GameSystem.modifyCreature(
        creatureId,
        { creature =>
          creature
            .activateEffect("staminaRegenerationStopped", if (isAttack) channelTime + cooldownTime + 0.01f else 1f)
          creature // TODO: activateEffect should return creature
        }
      )

      this
        .onChannellingStart()
        .modify(_.state)
        .setTo(AbilityState.Channeling)
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
            .modify(_.state)
            .setTo(AbilityState.Active)
            .modify(_.onCooldown)
            .setTo(true)
        } else
          this

        ability
          .updateHitbox()
          .onUpdateChanneling()

      case Active =>
        val ability: Ability = if (activeTimer.time > activeTime) {
          this
            .onStop()
            .modify(_.state)
            .setTo(AbilityState.Inactive)
        } else
          this

        ability
          .updateHitbox()
          .onUpdateActive()

      case Inactive =>
        if (onCooldown && activeTimer.time > cooldownTime) {

          this
            .modify(_.onCooldown)
            .setTo(false)
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

  def onCollideWithCreature(creature: Creature): Self = this

  def active: Boolean = state == AbilityState.Active

  def asMapEntry: (String, Ability) = id -> this

  def copy(
    creatureId: String = creatureId,
    state: AbilityState = state,
    onCooldown: Boolean = onCooldown,
    components: List[AbilityComponent] = components,
    lastComponentFinishTime: Float = lastComponentFinishTime,
    soundParameters: SoundParameters = soundParameters,
    timerParameters: TimerParameters = timerParameters,
    bodyParameters: BodyParameters = bodyParameters,
    animationParameters: AnimationParameters = animationParameters,
    dirVector: Vector2 = dirVector
  ): Self
}
