package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.ability.parameters.AbilityParameters
import com.easternsauce.libgdxgame.util.EsBatch

trait ComposedAbility extends Ability {
  val components: List[AbilityComponent]
  val lastComponentFinishTime: Float

  protected val numOfComponents = 0

  override protected lazy val activeTime: Float = 0

  override def update(): AbilityParameters = {
    val channelTimer = timerParameters.channelTimer
    val activeTimer = timerParameters.activeTimer

    import AbilityState._

    state match {
      case Channeling =>
        val params = if (channelTimer.time > channelTime) {
          activeTimer.restart()

          AbilityParameters(state = Some(AbilityState.Active), onCooldown = Some(true))
            .add(onActiveStart())

        } else
          AbilityParameters()

        params
          .add(updateHitbox())
          .add(onUpdateChanneling())

      case Active =>
        // stop when all components are stopped
        val params =
          if (activeTimer.time > lastComponentFinishTime)
            AbilityParameters(state = Some(AbilityState.Inactive))
              .add(onStop())
          else
            AbilityParameters()

        params
          .add(updateHitbox())
          .add(onUpdateActive())

      case Inactive if onCooldown =>
        if (activeTimer.time > cooldownTime) {
          AbilityParameters(onCooldown = Some(false))
        } else
          AbilityParameters()
      case _ => AbilityParameters()
    }

  }

  override def render(batch: EsBatch): AbilityParameters = {
    if (state == AbilityState.Active) {
      // TODO: remove sideeffect
      for (component <- components) {
        component.render(batch)
      }
    }

    AbilityParameters()
  }

  override protected def onUpdateActive(): AbilityParameters = {
    val activeTimer = timerParameters.activeTimer

    // TODO: remove sideeffect

    for (component <- components) {
      if (!component.started && activeTimer.time > component.startTime) {
        component.start()
      }

      component.onUpdateActive()
    }

    AbilityParameters()
  }

  override def onChannellingStart(): AbilityParameters = {

    val components = for (i <- 0 until numOfComponents) yield createComponent(i)

    val lastComponent = components.maxBy(_.totalTime)
    val lastComponentFinishTime = lastComponent.totalTime

    // TODO: remove side effect
    creature.activateEffect("immobilized", lastComponentFinishTime)

    AbilityParameters(components = Some(components.toList), lastComponentFinishTime = Some(lastComponentFinishTime))
  }

  def createComponent(index: Int): AbilityComponent

}
