package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable.ListBuffer

trait ComposedAbility extends Ability {
  protected var components: ListBuffer[AbilityComponent] = ListBuffer()

  protected val numOfComponents = 0

  protected def activeTime: Float = 0

  var lastComponentFinishTime: Float = 0

  override def update(): Unit = {
    if ((state == AbilityState.Channeling) && channelTimer.time > channelTime) {
      state = AbilityState.Active
      onActiveStart()
      activeTimer.restart()
      onCooldown = true
    }
    // stop when all components are stopped

    if (state == AbilityState.Active) {
      if (activeTimer.time > lastComponentFinishTime) {
        onStop()

        state = AbilityState.Inactive
      }

    }

    if (state == AbilityState.Channeling || state == AbilityState.Active) {
      updateHitbox()
    }

    if (state == AbilityState.Channeling) onUpdateChanneling()
    else if (state == AbilityState.Active) onUpdateActive()

    if ((state == AbilityState.Inactive) && onCooldown)
      if (activeTimer.time > cooldownTime) onCooldown = false
  }

  override def render(batch: EsBatch): Unit = {
    if (state == AbilityState.Active) {
      for (component <- components) {
        component.render(batch)
      }
    }
  }

  override protected def onUpdateActive(): Unit = {
    for (component <- components) {
      if (!component.started && activeTimer.time > component.startTime) {
        component.start()
      }

      component.onUpdateActive()
    }
  }

  override def onChannellingStart(): Unit = {
    components = ListBuffer[AbilityComponent]()

    for (i <- 0 until numOfComponents) {
      val component = createComponent(i)

      components += component
    }

    val lastComponent = components.maxBy(_.totalTime)
    lastComponentFinishTime = lastComponent.totalTime

    creature.activateEffect("immobilized", lastComponentFinishTime)
  }

  def createComponent(index: Int): AbilityComponent

}
