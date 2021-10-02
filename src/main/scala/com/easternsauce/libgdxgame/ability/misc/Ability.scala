package com.easternsauce.libgdxgame.ability.misc

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.parameters.{AnimationParameters, BodyParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch
import com.softwaremill.quicklens._

trait Ability extends Modification {
  type Self >: this.type <: Ability

  val id: String
  val creature: Creature
  val components: List[AbilityComponent] = List()
  val lastComponentFinishTime: Float = 0f
  val state: AbilityState = AbilityState.Inactive
  val onCooldown: Boolean = false
  val soundParameters: SoundParameters = SoundParameters()
  val timerParameters: TimerParameters = TimerParameters()
  val bodyParameters: BodyParameters = BodyParameters()
  val animationParameters: AnimationParameters = AnimationParameters()
  val dirVector: Vector2 = new Vector2(0f, 0f)

  val activeAnimation: Option[Animation] = None
  val channelAnimation: Option[Animation] = None

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
        .modify(_.state).setTo(AbilityState.Inactive)
//        .makeCopy(state = AbilityState.Inactive)
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
        .copy(state = AbilityState.Channeling)
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
            .copy(state = AbilityState.Active, onCooldown = true)
        } else
          this

        ability
          .updateHitbox()
          .onUpdateChanneling()

      case Active =>
        val ability: Ability = if (activeTimer.time > activeTime) {
          this
            .onStop()
            .copy(state = AbilityState.Inactive)
        } else
          this

        ability
          .updateHitbox()
          .onUpdateActive()

      case Inactive =>
        if (onCooldown && activeTimer.time > cooldownTime) {

          this
            .copy(onCooldown = false)
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
    components: List[AbilityComponent] = components,
    lastComponentFinishTime: Float = lastComponentFinishTime,
    state: AbilityState = state,
    onCooldown: Boolean = onCooldown,
    soundParameters: SoundParameters = soundParameters,
    timerParameters: TimerParameters = timerParameters,
    bodyParameters: BodyParameters = bodyParameters,
    animationParameters: AnimationParameters = animationParameters,
    dirVector: Vector2 = dirVector
  ): Self
}
