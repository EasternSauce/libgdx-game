package com.easternsauce.libgdxgame.ability.attack

import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets

case class ThrustAttack private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters =
    SoundParameters(activeSound = Some(Assets.sound(Assets.attackSound)), activeSoundVolume = Some(0.1f)),
  override val hitbox: Option[AttackHitbox] = None,
  override val body: Option[Body] = None,
  override val toRemoveBody: Boolean = false,
  override val bodyActive: Boolean = false
) extends MeleeAttack {

  override val id: String = "thrust"

  override protected val baseChannelTime = 0.6f
  override protected val baseActiveTime = 0.275f
  private val numOfChannelFrames = 7
  private val numOfFrames = 11

  override val attackRange: Float = 0.9375f
  override protected val aimed: Boolean = false
  override protected val spriteWidth: Int = 64
  override protected val spriteHeight: Int = 32
  override protected val knockbackVelocity: Float = 20f
  override protected val cooldownTime: Float = 0.7f

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

  override def setState(state: AbilityState): Ability = copy(state = state)

  override def setOnCooldown(onCooldown: Boolean): Ability = copy(onCooldown = onCooldown)

  override def setToRemoveBody(toRemoveBody: Boolean): MeleeAttack = copy(toRemoveBody = toRemoveBody)

  override def setBody(body: Option[Body]): MeleeAttack = copy(body = body)

  override def setHitbox(hitbox: Option[AttackHitbox]): MeleeAttack = copy(hitbox = hitbox)

  override def setBodyActive(bodyActive: Boolean): MeleeAttack = copy(bodyActive = bodyActive)
}
