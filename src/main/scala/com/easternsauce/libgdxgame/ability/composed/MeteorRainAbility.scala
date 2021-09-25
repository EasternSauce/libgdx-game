package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Meteor}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.GameSystem

case class MeteorRainAbility private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0f
) extends ComposedAbility(
      creature = creature,
      state = state,
      onCooldown = onCooldown,
      timerParameters = timerParameters,
      components = components,
      lastComponentFinishTime = lastComponentFinishTime
    ) {
  override val id = "meteor_rain"

  override protected lazy val channelTime: Float = 0.05f
  override protected val cooldownTime = 35f
  protected val explosionRange: Float = 9.375f

  override def onActiveStart(): MeteorRainAbility = {
    creature.takeStaminaDamage(25f)
    copy()
  }

  override def createComponent(index: Int): AbilityComponent = {
    val range = 34.375f

    new Meteor(
      this,
      0.15f * index,
      creature.pos.x + GameSystem.randomGenerator.between(-range, range),
      creature.pos.y + GameSystem.randomGenerator.between(-range, range),
      explosionRange,
      1.5f
    )
  }

}
