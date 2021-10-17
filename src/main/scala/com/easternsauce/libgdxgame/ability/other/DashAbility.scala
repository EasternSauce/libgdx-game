package com.easternsauce.libgdxgame.ability.other

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.parameters.{AnimationParameters, BodyParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.ability.misc.templates.Ability
import com.easternsauce.libgdxgame.ability.misc.templates.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.AbilityInfo
import com.softwaremill.quicklens.ModifyPimp

case class DashAbility private (
  override val creatureId: String,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0f,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters =
    SoundParameters(activeSound = Some(Assets.sound(Assets.flybySound)), activeSoundVolume = Some(0.2f)),
  override val bodyParameters: BodyParameters = BodyParameters(),
  override val animationParameters: AnimationParameters = AnimationParameters(),
  override val dirVector: Vector2 = new Vector2(0f, 0f)
) extends Ability(
      creatureId = creatureId,
      state = state,
      onCooldown = onCooldown,
      components = components,
      lastComponentFinishTime = lastComponentFinishTime,
      timerParameters = timerParameters,
      soundParameters = soundParameters,
      bodyParameters = bodyParameters,
      animationParameters = animationParameters,
      dirVector = dirVector
    ) {
  type Self = DashAbility

  override val id: String = DashAbility.id
  override val cooldownTime: Float = 1.5f
  override lazy val activeTime: Float = 0.2f
  override lazy val channelTime: Float = 0f

  val speed = 60f

  override val activeAnimation: Option[Animation] = None
  override val channelAnimation: Option[Animation] = None

  override def onActiveStart(): Self = {
    super.onActiveStart()

    val dashVector = new Vector2(creature.walkingVector.x * speed, creature.walkingVector.y * speed)

    // TODO: remove sideffect
    modifyCreature(creature => { creature.activateEffect("immobilized", channelTime + activeTime); creature })
    modifyCreature(creature => { creature.takeStaminaDamage(35f); creature })

    this
      .modify(_.dirVector)
      .setTo(dashVector)
  }

  override def onUpdateActive(): Self = {
    // TODO: remove sideffect
    modifyCreature(creature => { creature.sustainVelocity(dirVector); creature })

    this
  }

  override def copy(
    creatureId: String,
    state: AbilityState,
    onCooldown: Boolean,
    components: List[AbilityComponent],
    lastComponentFinishTime: Float,
    soundParameters: SoundParameters,
    timerParameters: TimerParameters,
    bodyParameters: BodyParameters,
    animationParameters: AnimationParameters,
    dirVector: Vector2
  ): Self =
    DashAbility(
      creatureId = creatureId,
      state = state,
      onCooldown = onCooldown,
      components = components,
      lastComponentFinishTime = lastComponentFinishTime,
      soundParameters = soundParameters,
      timerParameters = timerParameters,
      bodyParameters = bodyParameters,
      animationParameters = animationParameters,
      dirVector = dirVector
    )
}

object DashAbility extends AbilityInfo {
  override val id = "dash"
}
