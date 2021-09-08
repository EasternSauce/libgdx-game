package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Fist}
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.system.GameSystem

class FistSlamAbility private (val creature: Creature) extends ComposedAbility {
  val id: String = "fist_slam"

  protected val cooldownTime: Float = 10f
  protected val channelTime: Float = 0.15f

  override protected val numOfComponents = 20

  override protected def onActiveStart(): Unit = {
    creature.takeStaminaDamage(25f)
  }

  override def createComponent(index: Int): AbilityComponent = {
    val range: Float = 7.8125f
    val aggroedCreature = creature.asInstanceOf[Enemy].aggroedTarget.get // TODO targeting?
    new Fist(
      this,
      0.1f * index,
      aggroedCreature.pos.x + GameSystem.randomGenerator.between(-range, range),
      aggroedCreature.pos.y + GameSystem.randomGenerator.between(-range, range),
      2f
    )
  }

}

object FistSlamAbility {
  def apply(abilityCreature: Creature): FistSlamAbility = {
    new FistSlamAbility(abilityCreature)
  }
}
