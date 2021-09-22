package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Bubble}
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{AbilityParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature

case class BubbleAbility private (
  creature: Creature,
  state: AbilityState = Inactive,
  onCooldown: Boolean = false,
  soundParameters: SoundParameters = SoundParameters(),
  timerParameters: TimerParameters = TimerParameters(),
  lastComponentFinishTime: Float = 0f,
  components: List[AbilityComponent] = List()
) extends ComposedAbility {
  val id = "bubble"
  override protected lazy val channelTime: Float = 0.05f
  override protected val cooldownTime = 5f

  override protected val numOfComponents = 3

  override protected def onActiveStart(): AbilityParameters = {

    // TODO: sideeffects
    creature.takeStaminaDamage(25f)

    AbilityParameters()
  }

  override def createComponent(index: Int): AbilityComponent = {
    // TODO: factory method
    new Bubble(this, creature.pos.x, creature.pos.y, radius = 4f, speed = 30f, startTime = 0.4f * index)
  }

  override def applyParams(params: AbilityParameters): Ability = {
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
