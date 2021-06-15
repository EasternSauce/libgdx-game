package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.creature.traits.AggressiveAI

import scala.collection.mutable

abstract class Enemy extends Creature with AggressiveAI {
  override val isEnemy: Boolean = true

  protected val dropTable: mutable.Map[String, Float] = mutable.Map()

  override def calculateFacingVector(): Unit = {
    if (aggroedTarget.nonEmpty) {
      val aggroed = aggroedTarget.get
      facingVector = new Vector2(aggroed.pos.x - pos.x, aggroed.pos.y - pos.y).nor()
    }
  }

  override def update(): Unit = {
    super.update()

    searchForAndAttackTargets()

  }

  override def takeLifeDamage(
    damage: Float,
    immunityFrames: Boolean,
    dealtBy: Option[Creature] = None,
    knockbackPower: Float = 0,
    sourceX: Float = 0,
    sourceY: Float = 0
  ): Unit = {
    super.takeLifeDamage(damage, immunityFrames, dealtBy, knockbackPower, sourceX, sourceY)

    if (aggroedTarget.isEmpty && dealtBy.nonEmpty) {
      aggroOnCreature(dealtBy.get)
    }

  }

  override def onDeath(): Unit = {
    super.onDeath()

    area.get.spawnLootPile(pos.x, pos.y, dropTable)

  }

  def spawnPosition: Vector2 = {
    val spawnPoint = area.get.enemySpawns.filter(_.id == spawnPointId.get).head
    new Vector2(spawnPoint.posX, spawnPoint.posY)
  }

}
