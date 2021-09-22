package com.easternsauce.libgdxgame.ability.attack

import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{AbilityParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets

case class ThrustAttack private (
  creature: Creature,
  state: AbilityState = Inactive,
  onCooldown: Boolean = false,
  timerParameters: TimerParameters = TimerParameters(),
  hitbox: Option[AttackHitbox] = None
) extends MeleeAttack {

  val id: String = "thrust"

  protected val baseChannelTime = 0.6f
  protected val baseActiveTime = 0.275f
  private val numOfChannelFrames = 7
  private val numOfFrames = 11

  val attackRange: Float = 0.9375f
  protected val aimed: Boolean = false
  protected val spriteWidth: Int = 64
  protected val spriteHeight: Int = 32
  protected val knockbackVelocity: Float = 20f
  override protected val cooldownTime: Float = 0.7f

  override val soundParameters: SoundParameters =
    SoundParameters(activeSound = Some(Assets.sound(Assets.attackSound)), activeSoundVolume = Some(0.1f))

  setupActiveAnimation(
    regionName = "trident_thrust",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfFrames,
    frameDuration = baseActiveTime / numOfFrames
  )

  setupWindupAnimation(
    regionName = "trident_thrust_windup",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfChannelFrames,
    frameDuration = baseChannelTime / numOfChannelFrames
  )

  override protected def onUpdateActive(): AbilityParameters = {
    AbilityParameters()
  }

  override protected def onUpdateChanneling(): AbilityParameters = {
    AbilityParameters()
  }

  override def applyParams(params: AbilityParameters): ThrustAttack = {
    copy(
      creature = params.creature.getOrElse(creature),
      state = params.state.getOrElse(state),
      onCooldown = params.onCooldown.getOrElse(onCooldown),
      timerParameters = params.timerParameters.getOrElse(timerParameters),
      hitbox = params.hitbox.getOrElse(hitbox)
    )
  }
}

object ThrustAttack {
  def apply(abilityCreature: Creature): ThrustAttack = {
    new ThrustAttack(abilityCreature)
  }
}
