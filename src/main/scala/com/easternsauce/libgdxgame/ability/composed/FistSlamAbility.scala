package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Fist}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{AnimationParameters, BodyParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.system.GameSystem

case class FistSlamAbility private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0f
) extends ComposedAbility {

  override val id: String = "fist_slam"

  override protected val cooldownTime: Float = 10f
  override protected lazy val channelTime: Float = 0.15f

  override protected val numOfComponents = 20

  override def onActiveStart(): FistSlamAbility = {

    // TODO: sideeffect
    creature.takeStaminaDamage(25f)

    copy()
  }

  override def createComponent(index: Int): AbilityComponent = {
    val range: Float = 7.8125f
    val aggroedCreature = creature.asInstanceOf[Enemy].aggroedTarget.get // TODO targeting?
    // TODO: factory method
    new Fist(
      this,
      0.1f * index,
      aggroedCreature.pos.x + GameSystem.randomGenerator.between(-range, range),
      aggroedCreature.pos.y + GameSystem.randomGenerator.between(-range, range),
      2f
    )
  }

  override def makeCopy(
    components: List[AbilityComponent] = components,
    lastComponentFinishTime: Float = lastComponentFinishTime,
    state: AbilityState = state,
    onCooldown: Boolean = onCooldown,
    soundParameters: SoundParameters = soundParameters,
    timerParameters: TimerParameters = timerParameters,
    bodyParameters: BodyParameters = bodyParameters,
    animationParameters: AnimationParameters = animationParameters
  ): FistSlamAbility =
    copy(
      components = components,
      lastComponentFinishTime = lastComponentFinishTime,
      state = state,
      onCooldown = onCooldown,
      soundParameters = soundParameters,
      timerParameters = timerParameters
    )
}
