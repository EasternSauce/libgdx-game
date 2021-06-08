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

  override def update(): Unit = {
    super.update()

    searchForAndAttackTargets(this)

  }

  override def takeHealthDamage(
    damage: Float,
    immunityFrames: Boolean,
    dealtBy: Option[Creature] = None,
    knockbackPower: Float = 0,
    sourceX: Float = 0,
    sourceY: Float = 0
  ) {
    super.takeHealthDamage(damage, immunityFrames, dealtBy, knockbackPower, sourceX, sourceY)

    if (aggroedTarget.isEmpty && dealtBy.nonEmpty) {
      aggroOnCreature(this, dealtBy.get)
    }

  }
}
