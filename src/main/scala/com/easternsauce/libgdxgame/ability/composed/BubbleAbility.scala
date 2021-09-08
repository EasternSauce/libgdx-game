package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Bubble}
import com.easternsauce.libgdxgame.creature.Creature

class BubbleAbility private (val creature: Creature) extends ComposedAbility {
  val id = "bubble"
  protected val channelTime: Float = 0.05f
  protected val cooldownTime = 5f

  override protected val numOfComponents = 3

  override protected def onActiveStart(): Unit = {
    creature.takeStaminaDamage(25f)
  }

  override def createComponent(index: Int): AbilityComponent = {
    new Bubble(this, creature.pos.x, creature.pos.y, radius = 4f, speed = 30f, startTime = 0.4f * index)
  }
}

object BubbleAbility {
  def apply(creature: Creature): BubbleAbility = {
    new BubbleAbility(creature)
  }
}
