package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.traits.AggressiveAi
import com.easternsauce.libgdxgame.system.GameSystem
import com.easternsauce.libgdxgame.util.EsTimer

abstract class Enemy(
  override val id: String,
  override val area: Option[Area] = None,
  override val b2Body: Option[Body] = None,
  override val standardAbilities: Map[String, Ability] = Map(),
  override val additionalAbilities: Map[String, Ability] = Map()
) extends Creature(id = id, area = area, b2Body = b2Body, standardAbilities = standardAbilities, additionalAbilities = additionalAbilities)
    with AggressiveAi {
  override val isEnemy: Boolean = true

  val activeSoundTimer: EsTimer = EsTimer()
  val activeSound: Option[Sound] = None
  var activeSoundTimeout: Float = 3f + GameSystem.randomGenerator.nextFloat() * 8f

  protected val dropTable: Map[String, Float] = Map()

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

    if (isAlive && aggroedTarget.isEmpty && dealtBy.nonEmpty) {
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
