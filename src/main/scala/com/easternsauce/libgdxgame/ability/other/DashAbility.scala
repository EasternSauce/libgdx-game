package com.easternsauce.libgdxgame.ability.other

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{AnimationParameters, BodyParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.Creature

case class DashAbility private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  dashVector: Vector2 = new Vector2(0f, 0f)
) extends Ability {
  type Self = DashAbility

  override val id = "dash"
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
    creature.activateEffect("immobilized", channelTime + activeTime)
    creature.takeStaminaDamage(35f)

    copy(dashVector = dashVector)
  }

  override def onUpdateActive(): Self = {
    // TODO: remove sideffect
    creature.sustainVelocity(dashVector)

    copy()
  }

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
    copy(state = state, onCooldown = onCooldown, soundParameters = soundParameters, timerParameters = timerParameters)
}
