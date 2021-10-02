package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Bubble}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.misc.Modification
import com.easternsauce.libgdxgame.ability.parameters._
import com.easternsauce.libgdxgame.creature.Creature

case class BubbleAbility private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0f
) extends ComposedAbility {
  override type Self = BubbleAbility

  override val id = "bubble"
  override protected lazy val channelTime: Float = 0.05f
  override protected val cooldownTime = 5f

  override protected val numOfComponents = 3

  override def onActiveStart(): Self = {

    // TODO: sideeffects
    creature.takeStaminaDamage(25f)

    copy()
  }

  override def createComponent(index: Int): AbilityComponent = {
    Bubble(
      this,
      componentParameters = ComponentParameters(
        startX = creature.pos.x,
        startY = creature.pos.y,
        radius = 4f,
        speed = 30f,
        startTime = 0.4f * index
      )
    )
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
    copy(
      components = components,
      lastComponentFinishTime = lastComponentFinishTime,
      state = state,
      onCooldown = onCooldown,
      soundParameters = soundParameters,
      timerParameters = timerParameters
    )
}
