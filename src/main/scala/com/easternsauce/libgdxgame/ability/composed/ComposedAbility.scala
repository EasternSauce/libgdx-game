package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.util.EsBatch

trait ComposedAbility extends Ability {
  type Self >: this.type <: ComposedAbility

  implicit def toComposedAbility(ability: Ability): Self = ability.asInstanceOf[Self]

  val components: List[AbilityComponent]
  val lastComponentFinishTime: Float

  protected val numOfComponents: Int = -1

  override protected lazy val activeTime: Float = 0

  override def update(): Self = {
    val channelTimer = timerParameters.channelTimer
    val activeTimer = timerParameters.activeTimer

    import AbilityState._

    val res: Self = state match {
      case Channeling =>
        val ability: Self = if (channelTimer.time > channelTime) {
          activeTimer.restart()

          this
            .makeCopy(state = AbilityState.Active, onCooldown = true)
            .onActiveStart()

        } else
          this

        ability
          .onUpdateChanneling()

      case Active =>
        //stop when all components are stopped
        val ability =
          if (activeTimer.time > lastComponentFinishTime)
            this
              .makeCopy(state = AbilityState.Inactive)
              .onStop()
          else
            this

        ability
          .onUpdateActive()

      case Inactive if onCooldown =>
        if (activeTimer.time > cooldownTime) {
          makeCopy(onCooldown = false)
        } else
          this
      case _ => this
    }
    res
  }

  override def render(batch: EsBatch): Self = {
    if (state == AbilityState.Active) {
      // TODO: remove sideeffect
      for (component <- components) {
        component.render(batch)
      }
    }

    this
  }

  override def onUpdateActive(): Self = {
    val activeTimer = timerParameters.activeTimer

    // TODO: remove sideeffect

    // TODO: update components!!
    for (component <- components) {
      if (!component.started && activeTimer.time > component.componentParameters.startTime) {
        component.start()
      }

      component.onUpdateActive()
    }

    this
  }

  override def onChannellingStart(): Self = {

    val components = for (i <- 0 until numOfComponents) yield createComponent(i)

    val lastComponent = components.maxBy(_.totalTime)
    val lastComponentFinishTime = lastComponent.totalTime

    // TODO: remove side effect
    creature.activateEffect("immobilized", lastComponentFinishTime)

    this
      .makeCopy(components = components.toList, lastComponentFinishTime = lastComponentFinishTime)
  }

  def createComponent(index: Int): AbilityComponent = ???

}
