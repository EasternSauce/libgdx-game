package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.math.Vector2

trait Enemy extends Creature with AggressiveAI {

  override val isEnemy: Boolean = true

  override def calculateFacingVector(): Unit = {
    if (aggroedTarget.nonEmpty) {
      val aggroed = aggroedTarget.get
      facingVector = new Vector2(aggroed.pos.x - pos.x, aggroed.pos.y - pos.y).nor()
    }
  }
}
