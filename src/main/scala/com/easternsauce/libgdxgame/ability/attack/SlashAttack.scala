package com.easternsauce.libgdxgame.ability.attack

import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.misc.Modification
import com.easternsauce.libgdxgame.ability.parameters.{AnimationParameters, BodyParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets

case class SlashAttack private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters =
    SoundParameters(activeSound = Some(Assets.sound(Assets.attackSound)), activeSoundVolume = Some(0.1f)),
  override val bodyParameters: BodyParameters = BodyParameters(),
  override val animationParameters: AnimationParameters = AnimationParameters(
    textureWidth = 40,
    textureHeight = 40,
    activeRegionName = "slash",
    channelRegionName = "slash_windup",
    channelFrameCount = 6,
    activeFrameCount = 6
  )
) extends MeleeAttack {
  override type Self = MeleeAttack

  override val id: String = "slash"

  override protected val baseChannelTime = 0.3f
  override protected val baseActiveTime = 0.3f

  override val attackRange: Float = 0.9375f
  override protected val aimed: Boolean = false
  override protected val spriteWidth: Int = 40
  override protected val spriteHeight: Int = 40
  override protected val knockbackVelocity: Float = 20f
  override protected val cooldownTime: Float = 0.8f

  override val activeAnimation: Option[Animation] = Some(
    Animation.activeAnimationFromParameters(animationParameters, baseActiveTime)
  )
  override val channelAnimation: Option[Animation] = Some(
    Animation.channelAnimationFromParameters(animationParameters, baseChannelTime)
  )

  override def makeCopy(
    components: List[AbilityComponent],
    lastComponentFinishTime: Float,
    state: AbilityState,
    onCooldown: Boolean,
    soundParameters: SoundParameters,
    timerParameters: TimerParameters,
    bodyParameters: BodyParameters,
    animationParameters: AnimationParameters
  ): Self =
    copy(
      state = state,
      onCooldown = onCooldown,
      soundParameters = soundParameters,
      timerParameters = timerParameters,
      bodyParameters = bodyParameters,
      animationParameters = animationParameters
    )
}
