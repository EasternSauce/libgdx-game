package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Meteor}
import com.easternsauce.libgdxgame.creature.Creature

import scala.collection.mutable.ListBuffer

class MeteorCrashAbility private (val creature: Creature) extends ComposedAbility {
  val id = "meteor_crash"
  protected val channelTime: Float = 0.05f
  protected val cooldownTime: Float = 12f

  override protected val numOfComponents = 30

  override def onChannellingStart(): Unit = {
    val meteors = ListBuffer[Meteor]()
    val facingVector: Vector2 = creature.facingVector.nor()
    for (i <- 0 until numOfComponents / 3) {
      meteors += new Meteor(
        this,
        0.1f * i,
        creature.pos.x + (3.125f * (i + 1)) * facingVector.x,
        creature.pos.y + (3.125f * (i + 1)) * facingVector.y,
        1.5625f + 0.09375f * i * i,
        2.5f
      )
    }
    for (i <- 0 until numOfComponents / 3) {
      val vector: Vector2 = facingVector.cpy()
      vector.setAngleDeg(vector.angleDeg() + 50)
      meteors += new Meteor(
        this,
        0.1f * i,
        creature.pos.x + (3.125f * (i + 1)) * vector.x,
        creature.pos.y + (3.125f * (i + 1)) * vector.y,
        1.5625f + 0.09375f * i * i,
        2.5f
      )
    }
    for (i <- 0 until numOfComponents / 3) {
      val vector: Vector2 = facingVector.cpy()
      vector.setAngleDeg(vector.angleDeg() - 50)
      meteors += new Meteor(
        this,
        0.1f * i,
        creature.pos.x + (3.125f * (i + 1)) * vector.x,
        creature.pos.y + (3.125f * (i + 1)) * vector.y,
        1.5625f + 0.09375f * i * i,
        2.5f
      )
    }
    components.addAll(meteors)

    val lastComponent = components.maxBy(_.totalTime)
    lastComponentFinishTime = lastComponent.totalTime

    creature.activateEffect("immobilized", lastComponentFinishTime)
  }

  override protected def onActiveStart(): Unit = {
    creature.takeStaminaDamage(25f)

  }

  override def createComponent(index: Int): AbilityComponent = {
    val facingVector: Vector2 = creature.facingVector.nor()

    val vector: Vector2 = facingVector.cpy()
    // TODO: change angle based on index?
    vector.setAngleDeg(vector.angleDeg() + 50)

    new Meteor(
      this,
      0.1f * index,
      creature.pos.x + (3.125f * (index + 1)) * vector.x,
      creature.pos.y + (3.125f * (index + 1)) * vector.y,
      1.5625f + 0.09375f * index * index,
      2.5f
    )
  }
}

object MeteorCrashAbility {
  def apply(abilityCreature: Creature): MeteorCrashAbility = {
    new MeteorCrashAbility(abilityCreature)
  }
}
