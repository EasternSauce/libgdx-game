package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.traits.Ability
import com.easternsauce.libgdxgame.creature.Creature

class DashAbility(val creature: Creature) extends Ability {

  override val id: String = "dash"

  override protected val cooldownTime: Float = 1.5f
  override protected def activeTime: Float = 0.2f
  protected var dashVector: Vector2 = new Vector2(0f, 0f)
  override protected def channelTime: Float = 0f

  val speed = 60f

  override def onActiveStart(): Unit = {
    super.onActiveStart()

    dashVector = new Vector2(creature.walkingVector.x * speed, creature.walkingVector.y * speed)

    creature.activateEffect("immobilized", channelTime + activeTime)
    creature.takeStaminaDamage(35f)
  }

  override def onUpdateActive(): Unit = {
    super.onUpdateActive()

    creature.sustainVelocity(dashVector)
  }
}

object DashAbility {
  def apply(abilityCreature: Creature): DashAbility = {
    new DashAbility(abilityCreature)
  }
}
