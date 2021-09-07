package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.components.{AbilityComponent, IceShard}
import com.easternsauce.libgdxgame.ability.traits.ComposedAbility
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable.ListBuffer

class IceShardAbility private (val creature: Creature) extends ComposedAbility {
  val id = "ice_shard"
  protected val channelTime: Float = 0.05f
  protected val cooldownTime = 5f

  val numOfShards = 9

  override def onChannellingStart(): Unit = {

    components = ListBuffer[AbilityComponent]()

    val facingVector =
      if (creature.asInstanceOf[Enemy].aggroedTarget.nonEmpty) {
        creature.facingVector.cpy
      } else {
        new Vector2(1.0f, 0.0f)
      }

    val iceShards =
      for (i <- 0 until numOfShards)
        yield new IceShard(
          this,
          creature.pos.x,
          creature.pos.y,
          speed = 30f,
          startTime = 0.05f * i,
          facingVector.cpy.rotateDeg(20f * (i - 5))
        )
    components.addAll(iceShards)

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

object IceShardAbility {
  def apply(creature: Creature): IceShardAbility = {
    new IceShardAbility(creature)
  }
}
