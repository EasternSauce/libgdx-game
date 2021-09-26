package com.easternsauce.libgdxgame.ability.attack

import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{BodyParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets

case class SlashAttack private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters =
    SoundParameters(activeSound = Some(Assets.sound(Assets.attackSound)), activeSoundVolume = Some(0.1f)),
  override val bodyParameters: BodyParameters = BodyParameters()
) extends MeleeAttack {
  override val id: String = "slash"

  override protected val baseChannelTime = 0.3f
  override protected val baseActiveTime = 0.3f
  val numOfChannelFrames = 6
  val numOfFrames = 6

  override val attackRange: Float = 0.9375f
  override protected val aimed: Boolean = false
  override protected val spriteWidth: Int = 40
  override protected val spriteHeight: Int = 40
  override protected val knockbackVelocity: Float = 20f
  override protected val cooldownTime: Float = 0.8f

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

  override def makeCopy(
    components: List[AbilityComponent] = components,
    lastComponentFinishTime: Float = lastComponentFinishTime,
    state: AbilityState = state,
    onCooldown: Boolean = onCooldown,
    soundParameters: SoundParameters = soundParameters,
    timerParameters: TimerParameters = timerParameters,
    bodyParameters: BodyParameters = bodyParameters
  ): SlashAttack =
    copy(
      state = state,
      onCooldown = onCooldown,
      soundParameters = soundParameters,
      timerParameters = timerParameters,
      bodyParameters = bodyParameters
    )
}
