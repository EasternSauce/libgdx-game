package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Bubble}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{SoundParameters, TimerParameters}
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
  override val id = "bubble"
  override protected lazy val channelTime: Float = 0.05f
  override protected val cooldownTime = 5f

  override protected val numOfComponents = 3

  override def onActiveStart(): BubbleAbility = {

    // TODO: sideeffects
    creature.takeStaminaDamage(25f)

    copy()
  }

  override def createComponent(index: Int): AbilityComponent = {
    // TODO: factory method
    new Bubble(this, creature.pos.x, creature.pos.y, radius = 4f, speed = 30f, startTime = 0.4f * index)
  }

  override def setComponents(components: List[AbilityComponent]): BubbleAbility = copy(components = components)

  override def setLastComponentFinishTime(lastComponentFinishTime: Float): BubbleAbility =
    copy(lastComponentFinishTime = lastComponentFinishTime)

  override def setState(state: AbilityState): BubbleAbility = copy(state = state)

  override def setOnCooldown(onCooldown: Boolean): BubbleAbility = copy(onCooldown = onCooldown)
}
