package com.easternsauce.libgdxgame.ability.traits

import com.easternsauce.libgdxgame.ability.AbilityState
import com.easternsauce.libgdxgame.ability.components.AbilityComponent

import scala.collection.mutable.ListBuffer

trait ComposedAbility extends Ability {
  protected var components: ListBuffer[AbilityComponent] = ListBuffer()

  protected var componentsStarted = false

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
      if (componentsStarted && activeTimer.time > lastComponentFinishTime) {
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

}
