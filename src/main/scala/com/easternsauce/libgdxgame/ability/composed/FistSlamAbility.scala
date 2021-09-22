package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Fist}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{AbilityParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.system.GameSystem

case class FistSlamAbility private (
  creature: Creature,
  state: AbilityState = Inactive,
  onCooldown: Boolean = false,
  soundParameters: SoundParameters = SoundParameters(),
  timerParameters: TimerParameters = TimerParameters(),
  components: List[AbilityComponent] = List(),
  lastComponentFinishTime: Float = 0f
) extends ComposedAbility {
  val id: String = "fist_slam"

  override protected val cooldownTime: Float = 10f
  override protected lazy val channelTime: Float = 0.15f

  override protected val numOfComponents = 20

  override protected def onActiveStart(): AbilityParameters = {

    // TODO: sideeffect
    creature.takeStaminaDamage(25f)

    AbilityParameters()
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

  override def applyParams(params: AbilityParameters): FistSlamAbility = {
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
