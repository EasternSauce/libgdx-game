package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.creature.traits.AggressiveAi
import com.easternsauce.libgdxgame.system.GameSystem
import com.easternsauce.libgdxgame.util.EsTimer

import scala.collection.mutable

abstract class Enemy extends Creature with AggressiveAi {
  override val isEnemy: Boolean = true

  val activeSoundTimer: EsTimer = EsTimer()
  val activeSound: Option[Sound] = None
  var activeSoundTimeout: Float = 3f + GameSystem.randomGenerator.nextFloat() * 8f

  protected val dropTable: mutable.Map[String, Float] = mutable.Map()

  override def calculateFacingVector(): Unit = {
    if (aggroedTarget.nonEmpty) {
      val aggroed = aggroedTarget.get
      facingVector.x = aggroed.pos.x - pos.x
      facingVector.y = aggroed.pos.y - pos.y
      facingVector.nor()
    } else {
      facingVector.x = 0
      facingVector.y = 0
    }
  }

  override def update(): Unit = {
    super.update()

    searchForAndAttackTargets()

    if (
      targetFound
      && activeSound.nonEmpty
      && activeSoundTimer.time > activeSoundTimeout
    ) {
      activeSound.get.play(0.2f)
      activeSoundTimer.restart()
      activeSoundTimeout = 3f + GameSystem.randomGenerator.nextFloat() * 8f
    }

  }

  override def takeLifeDamage(
    damage: Float,
    immunityFrames: Boolean,
    dealtBy: Option[Creature] = None,
    attackKnockbackVelocity: Float = 0,
    sourceX: Float = 0,
    sourceY: Float = 0
  ): Unit = {
    super.takeLifeDamage(damage, immunityFrames, dealtBy, attackKnockbackVelocity, sourceX, sourceY)

    if (alive && aggroedTarget.isEmpty && dealtBy.nonEmpty) {
      aggroOnCreature(dealtBy.get)
    }

  }

  override def onDeath(): Unit = {
    super.onDeath()

    area.get.spawnLootPile(pos.x, pos.y, dropTable)
    aggroedTarget = None

  }

  def spawnPosition: Vector2 = {
    val spawnPoint = area.get.enemySpawns.filter(_.id == spawnPointId.get).head
    new Vector2(spawnPoint.posX, spawnPoint.posY)
  }

  override def calculateWalkingVector(): Unit = {
    walkingVector = facingVector.cpy()
  }
}
