package com.easternsauce.libgdxgame.ability.attack

import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{AbilityParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets

case class SlashAttack private (
  creature: Creature,
  state: AbilityState = Inactive,
  onCooldown: Boolean = false,
  timerParameters: TimerParameters = TimerParameters(),
  hitbox: Option[AttackHitbox] = None,
  b2Body: Option[Body] = None,
  toRemoveBody: Boolean = false,
) extends MeleeAttack {

  val id: String = "slash"

  protected val baseChannelTime = 0.3f
  protected val baseActiveTime = 0.3f
  val numOfChannelFrames = 6
  val numOfFrames = 6

  val attackRange: Float = 0.9375f
  protected val aimed: Boolean = false
  protected val spriteWidth: Int = 40
  protected val spriteHeight: Int = 40
  protected val knockbackVelocity: Float = 20f
  override protected val cooldownTime: Float = 0.8f

  override val soundParameters: SoundParameters =
    SoundParameters(activeSound = Some(Assets.sound(Assets.attackSound)), activeSoundVolume = Some(0.1f))

  setupActiveAnimation(
    regionName = "slash",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfFrames,
    frameDuration = baseActiveTime / numOfFrames
  )

  setupWindupAnimation(
    regionName = "slash_windup",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfChannelFrames,
    frameDuration = baseChannelTime / numOfChannelFrames
  )

  override def applyParams(params: AbilityParameters): SlashAttack = {
    copy(
      creature = params.creature.getOrElse(creature),
      state = params.state.getOrElse(state),
      onCooldown = params.onCooldown.getOrElse(onCooldown),
      timerParameters = params.timerParameters.getOrElse(timerParameters),
      hitbox = params.hitbox.getOrElse(hitbox),
      b2Body = params.b2Body.getOrElse(b2Body),
      toRemoveBody = params.toRemoveBody.getOrElse(toRemoveBody),
    )
    a
  }

  override protected def onUpdateActive(): AbilityParameters = {
    AbilityParameters()
  }

  override protected def onUpdateChanneling(): AbilityParameters = {
    AbilityParameters()
  }
}

object SlashAttack {
  def apply(abilityCreature: Creature): SlashAttack = {
    new SlashAttack(abilityCreature)
  }
}
