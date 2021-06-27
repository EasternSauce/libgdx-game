package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.components.Meteor
import com.easternsauce.libgdxgame.ability.traits.Ability
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable.ListBuffer

class MeteorCrashAbility(val creature: Creature) extends Ability {
  val id = "meteorCrash"
  override protected val channelTime: Float = 0.05f
  override protected val activeTime: Float = 1.8f
  override protected val cooldownTime: Float = 6.5f
  protected var meteors: ListBuffer[Meteor] = ListBuffer()

  override def onChannellingStart(): Unit = {
    creature
      .effect("immobilized")
      .applyEffect(channelTime + activeTime)
  }

  override def render(batch: EsBatch): Unit = {
    if (state == AbilityState.Active) {
      for (meteor <- meteors) {
        meteor.render(batch)
      }
    }
  }

  override protected def onActiveStart(): Unit = {
    creature.takeStaminaDamage(25f)
    meteors = ListBuffer[Meteor]()
    val facingVector: Vector2 = creature.facingVector.nor()
    for (i <- 0 until 10) {
      meteors += new Meteor(
        this,
        0.1f * i,
        creature.pos.x + (3.125f * (i + 1)) * facingVector.x,
        creature.pos.y + (3.125f * (i + 1)) * facingVector.y,
        1.5625f + 0.09375f * i * i,
        2.5f
      )
    }
    for (i <- 0 until 10) {
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
    for (i <- 0 until 10) {
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
  }

  override protected def onUpdateActive(): Unit = {
    for (meteor <- meteors) {
      if (!meteor.started && activeTimer.time > meteor.startTime) {
        meteor.start()
      }

      meteor.onUpdateActive()
    }
  }
}
