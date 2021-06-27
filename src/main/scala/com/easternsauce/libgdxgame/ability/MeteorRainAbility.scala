package com.easternsauce.libgdxgame.ability

import com.easternsauce.libgdxgame.ability.components.Meteor
import com.easternsauce.libgdxgame.ability.traits.Ability
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.GameSystem
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable.ListBuffer

class MeteorRainAbility(val creature: Creature) extends Ability {
  val id = "meteorRain"
  override protected val channelTime: Float = 0.05f
  override protected val activeTime: Float = 0.15f * 59 + (1.2f + 1.8f) / 1.5f + 0.1f
  override protected val cooldownTime = 35f
  protected val explosionRange: Float = 9.375f
  protected var meteors: ListBuffer[Meteor] = _

  override def onChannellingStart(): Unit = {
    creature
      .effect("immobilized")
      .applyEffect(channelTime + activeTime)
    meteors = ListBuffer[Meteor]()
    for (i <- 0 until 60) {
      val range = 34.375f
      val meteor = new Meteor(
        this,
        0.15f * i,
        creature.pos.x + GameSystem.randomGenerator.between(-range, range),
        creature.pos.y + GameSystem.randomGenerator.between(-range, range),
        explosionRange,
        1.5f
      )

      meteors += meteor
    }
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
