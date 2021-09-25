package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.ability.parameters.TimerParameters
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

class ComposedAbility protected (
  override val creature: Creature,
  override val state: AbilityState,
  override val onCooldown: Boolean,
  override val timerParameters: TimerParameters,
  val components: List[AbilityComponent],
  val lastComponentFinishTime: Float
) extends Ability(creature = creature, state = state, onCooldown = onCooldown, timerParameters = timerParameters) {

  implicit def toComposedAbility(ability: Ability): ComposedAbility = ability.asInstanceOf[ComposedAbility]

  protected val numOfComponents: Int = -1

  override protected lazy val activeTime: Float = 0

  override def update(): ComposedAbility = {
    val channelTimer = timerParameters.channelTimer
    val activeTimer = timerParameters.activeTimer

    import AbilityState._

    val res: ComposedAbility = state match {
      case Channeling =>
        val ability: ComposedAbility = if (channelTimer.time > channelTime) {
          activeTimer.restart()

          makeComposedAbilityCopy(state = AbilityState.Active, onCooldown = true)
            .onActiveStart()

        } else
          makeComposedAbilityCopy()

        ability
          .onUpdateChanneling()

      case Active =>
        //stop when all components are stopped
        val ability =
          if (activeTimer.time > lastComponentFinishTime)
            makeComposedAbilityCopy(
              state = AbilityState.Inactive,
              components = components
            ) // TODO: refactor so it knows which implementation to take without copying components
              .onStop()
          else
            makeComposedAbilityCopy()

        ability
          .onUpdateActive()

      case Inactive if onCooldown =>
        if (activeTimer.time > cooldownTime) {
          makeComposedAbilityCopy(onCooldown = false)
        } else
          makeComposedAbilityCopy()
      case _ => makeComposedAbilityCopy()
    }
    res
  }

  override def render(batch: EsBatch): ComposedAbility = {
    if (state == AbilityState.Active) {
      // TODO: remove sideeffect
      for (component <- components) {
        component.render(batch)
      }
    }

    makeComposedAbilityCopy()
  }

  override def onUpdateActive(): ComposedAbility = {
    val activeTimer = timerParameters.activeTimer

    // TODO: remove sideeffect

    for (component <- components) {
      if (!component.started && activeTimer.time > component.startTime) {
        component.start()
      }

      component.onUpdateActive()
    }

    makeComposedAbilityCopy()
  }

  override def onChannellingStart(): ComposedAbility = {

    val components = for (i <- 0 until numOfComponents) yield createComponent(i)

    val lastComponent = components.maxBy(_.totalTime)
    val lastComponentFinishTime = lastComponent.totalTime

    // TODO: remove side effect
    creature.activateEffect("immobilized", lastComponentFinishTime)

    makeComposedAbilityCopy(components = components.toList, lastComponentFinishTime = lastComponentFinishTime)
  }

  def createComponent(index: Int): AbilityComponent = ???

  private def makeComposedAbilityCopy(
    creature: Creature = creature,
    state: AbilityState = state,
    onCooldown: Boolean = onCooldown,
    timerParameters: TimerParameters = timerParameters,
    components: List[AbilityComponent] = components,
    lastComponentFinishTime: Float = lastComponentFinishTime
  ): ComposedAbility = {
    ComposedAbility(creature, state, onCooldown, timerParameters, components, lastComponentFinishTime)
  }

}

object ComposedAbility {
  def apply(
    creature: Creature,
    state: AbilityState,
    onCooldown: Boolean,
    timerParameters: TimerParameters,
    components: List[AbilityComponent],
    lastComponentFinishTime: Float
  ): ComposedAbility =
    new ComposedAbility(creature, state, onCooldown, timerParameters, components, lastComponentFinishTime)
}
