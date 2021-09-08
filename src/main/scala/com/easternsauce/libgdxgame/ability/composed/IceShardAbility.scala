package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, IceShard}
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}

class IceShardAbility private (val creature: Creature) extends ComposedAbility {
  val id = "ice_shard"
  protected val channelTime: Float = 0.05f
  protected val cooldownTime = 5f

  override protected val numOfComponents = 9

  override protected def onActiveStart(): Unit = {
    creature.takeStaminaDamage(25f)
  }

  override def createComponent(index: Int): AbilityComponent = {
    val facingVector =
      if (creature.asInstanceOf[Enemy].aggroedTarget.nonEmpty) {
        creature.facingVector.cpy
      } else {
        new Vector2(1.0f, 0.0f)
      }

    new IceShard(
      this,
      creature.pos.x,
      creature.pos.y,
      speed = 30f,
      startTime = 0.05f * index,
      facingVector.cpy.rotateDeg(20f * (index - 5))
    )
  }
}

object IceShardAbility {
  def apply(creature: Creature): IceShardAbility = {
    new IceShardAbility(creature)
  }
}
