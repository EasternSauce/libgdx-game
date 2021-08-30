package com.easternsauce.libgdxgame.ability

import com.easternsauce.libgdxgame.ability.components.{AbilityComponent, Bubble}
import com.easternsauce.libgdxgame.ability.traits.ComposedAbility
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable.ListBuffer

class BubbleAbility(val creature: Creature) extends ComposedAbility {
  val id = "bubble"
  override protected val channelTime: Float = 0.05f
  override protected val cooldownTime = 10f

  override def onChannellingStart(): Unit = {

    components = ListBuffer[AbilityComponent]()
    for (i <- 0 until 5) {
      val bubble = new Bubble(this, creature.pos.x, creature.pos.y, radius = 4f, speed = 20f, startTime = 0.7f * i)

      components += bubble
    }

    creature
      .effect("immobilized")
      .applyEffect(lastComponentFinishTime)
    println("immobilize for + " + lastComponentFinishTime)
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

    println("starting...")
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
