package com.easternsauce.libgdxgame.ability.composed

import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Meteor}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.GameSystem

class MeteorRainAbility private (val creature: Creature) extends ComposedAbility {
  val id = "meteor_rain"
  protected val channelTime: Float = 0.05f
  protected val cooldownTime = 35f
  protected val explosionRange: Float = 9.375f

  override protected def onActiveStart(): Unit = {
    creature.takeStaminaDamage(25f)
  }

  override def createComponent(index: Int): AbilityComponent = {
    val range = 34.375f

    new Meteor(
      this,
      0.15f * index,
      creature.pos.x + GameSystem.randomGenerator.between(-range, range),
      creature.pos.y + GameSystem.randomGenerator.between(-range, range),
      explosionRange,
      1.5f
    )
  }
}

object MeteorRainAbility {
  def apply(abilityCreature: Creature): MeteorRainAbility = {
    new MeteorRainAbility(abilityCreature)
  }
}
