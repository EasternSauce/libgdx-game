package com.easternsauce.libgdxgame.ability

import com.easternsauce.libgdxgame.ability.components.{AbilityComponent, IceShard}
import com.easternsauce.libgdxgame.ability.traits.ComposedAbility
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable.ListBuffer

class IceShardAbility(val creature: Creature) extends ComposedAbility {
  val id = "ice_shard"
  override protected val channelTime: Float = 0.05f
  override protected val cooldownTime = 5f

  override def onChannellingStart(): Unit = {

    components = ListBuffer[AbilityComponent]()
    for (i <- 0 until 3) {
      val bubble = new IceShard(this, creature.pos.x, creature.pos.y, speed = 30f, startTime = 0.4f * i)

      components += bubble
    }

    val lastComponent =
      components.maxBy(component => component.startTime + component.channelTime + component.activeTime)
    lastComponentFinishTime =
      lastComponent.startTime + lastComponent.channelTime + lastComponent.activeTime + 0.05f // with buffer

    creature.activateEffect("immobilized", lastComponentFinishTime)
  }

  override def render(batch: EsBatch): Unit = {
    if (state == AbilityState.Active) {
      for (bubble <- components) {
        bubble.render(batch)
      }
    }
  }

  override protected def onActiveStart(): Unit = {
    creature.takeStaminaDamage(25f)
  }

  override protected def onUpdateActive(): Unit = {
    for (bubble <- components) {
      if (!bubble.started && activeTimer.time >= bubble.startTime) {
        componentsStarted = true
        bubble.start()
      }

      bubble.onUpdateActive()
    }
  }
}
