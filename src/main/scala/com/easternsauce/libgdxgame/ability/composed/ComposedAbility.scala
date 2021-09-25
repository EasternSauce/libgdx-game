package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.util.EsBatch

trait ComposedAbility extends Ability {

  implicit def toComposedAbility(ability: Ability): ComposedAbility = ability.asInstanceOf[ComposedAbility]

  val components: List[AbilityComponent]
  val lastComponentFinishTime: Float

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

          this
            .setState(state = AbilityState.Active)
            .setOnCooldown(onCooldown = true)
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
              .setState(state = AbilityState.Inactive)
              .onStop()
          else
            this

        ability
          .onUpdateActive()

      case Inactive if onCooldown =>
        if (activeTimer.time > cooldownTime) {
          setOnCooldown(onCooldown = false)
        } else
          this
      case _ => this
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

    this
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

    this
  }

  override def onChannellingStart(): ComposedAbility = {

    val components = for (i <- 0 until numOfComponents) yield createComponent(i)

    val lastComponent = components.maxBy(_.totalTime)
    val lastComponentFinishTime = lastComponent.totalTime

    // TODO: remove side effect
    creature.activateEffect("immobilized", lastComponentFinishTime)

    this
      .setComponents(components = components.toList)
      .setLastComponentFinishTime(lastComponentFinishTime = lastComponentFinishTime)
  }

  def createComponent(index: Int): AbilityComponent = ???

  def setComponents(components: List[AbilityComponent]): ComposedAbility
  def setLastComponentFinishTime(lastComponentFinishTime: Float): ComposedAbility

}
