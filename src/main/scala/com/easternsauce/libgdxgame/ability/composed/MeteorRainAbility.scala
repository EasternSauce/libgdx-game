package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Meteor}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{AbilityParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.GameSystem

case class MeteorRainAbility private (
  creature: Creature,
  state: AbilityState = Inactive,
  onCooldown: Boolean = false,
  soundParameters: SoundParameters = SoundParameters(),
  timerParameters: TimerParameters = TimerParameters(),
  components: List[AbilityComponent] = List(),
  lastComponentFinishTime: Float = 0f
) extends ComposedAbility {
  val id = "meteor_rain"
  override protected lazy val channelTime: Float = 0.05f
  override protected val cooldownTime = 35f
  protected val explosionRange: Float = 9.375f

  override protected def onActiveStart(): AbilityParameters = {
    creature.takeStaminaDamage(25f)
    AbilityParameters()
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

  override def applyParams(params: AbilityParameters): MeteorRainAbility = {
    copy(
      creature = params.creature.getOrElse(creature),
      state = params.state.getOrElse(state),
      onCooldown = params.onCooldown.getOrElse(onCooldown),
      soundParameters = params.soundParameters.getOrElse(soundParameters),
      timerParameters = params.timerParameters.getOrElse(timerParameters),
      lastComponentFinishTime = params.lastComponentFinishTime.getOrElse(lastComponentFinishTime),
      components = params.components.getOrElse(components)
    )
  }

  override def updateHitbox(): AbilityParameters = {
    AbilityParameters()
  }

  override protected def onUpdateChanneling(): AbilityParameters = {
    AbilityParameters()
  }

  override protected def onStop(): AbilityParameters = {
    AbilityParameters()
  }

  override def onCollideWithCreature(creature: Creature): AbilityParameters = {
    AbilityParameters()
  }
}
