package com.easternsauce.libgdxgame.ability

import com.easternsauce.libgdxgame.ability.components.{AbilityComponent, Bubble}
import com.easternsauce.libgdxgame.ability.traits.ComposedAbility
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable.ListBuffer

class BubbleAbility private (val creature: Creature) extends ComposedAbility {
  val id = "bubble"
  protected val channelTime: Float = 0.05f
  protected val cooldownTime = 5f

  override def onChannellingStart(): Unit = {

    components = ListBuffer[AbilityComponent]()
    for (i <- 0 until 3) {
      val bubble = new Bubble(this, creature.pos.x, creature.pos.y, radius = 4f, speed = 30f, startTime = 0.4f * i)

      components += bubble
    }

    val lastComponent = components.maxBy(_.totalTime)
    lastComponentFinishTime = lastComponent.totalTime

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

object BubbleAbility {
  def apply(creature: Creature): BubbleAbility = {
    new BubbleAbility(creature)
  }
}
