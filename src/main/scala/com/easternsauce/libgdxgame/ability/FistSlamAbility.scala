package com.easternsauce.libgdxgame.ability

import com.easternsauce.libgdxgame.ability.components.Fist
import com.easternsauce.libgdxgame.ability.traits.Ability
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.system.GameSystem
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable.ListBuffer

class FistSlamAbility private (val creature: Creature) extends Ability {
  val id: String = "fist_slam"

  protected val cooldownTime: Float = 10f
  protected val activeTime: Float = 0.1f * 19 + 0.2f + 0.4f + 0.1f
  protected val channelTime: Float = 0.15f

  protected var fists: ListBuffer[Fist] = ListBuffer()

  override def render(batch: EsBatch): Unit = {
    if (state == AbilityState.Active) {
      for (fist <- fists) {
        fist.render(batch)
      }
    }
  }

  override def onChannellingStart(): Unit = {
    creature.activateEffect("immobilized", channelTime + activeTime)

    fists = new ListBuffer[Fist]

    for (i <- 0 until 20) {
      val range: Float = 7.8125f
      val aggroedCreature = creature.asInstanceOf[Enemy].aggroedTarget.get // TODO targeting?
      fists += new Fist(
        this,
        0.1f * i,
        aggroedCreature.pos.x + GameSystem.randomGenerator.between(-range, range),
        aggroedCreature.pos.y + GameSystem.randomGenerator.between(-range, range),
        2f
      )
    }
  }

  override protected def onActiveStart(): Unit = {
    creature.takeStaminaDamage(25f)
  }

  override protected def onUpdateActive(): Unit = {
    for (fist <- fists) {
      if (!fist.started && activeTimer.time > fist.startTime) {
        fist.start()
      }
      fist.onUpdateActive()
    }
  }

}

object FistSlamAbility {
  def apply(abilityCreature: Creature): FistSlamAbility = {
    new FistSlamAbility(abilityCreature)
  }
}
