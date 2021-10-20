package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.creature.traits.AggressiveAi
import com.easternsauce.libgdxgame.system.GameSystem
import com.easternsauce.libgdxgame.system.GameSystem.areaMap
import com.easternsauce.libgdxgame.util.EsTimer

abstract class Enemy(override val id: String, override val params: CreatureParameters = CreatureParameters())
    extends Creature(id = id, params = params)
    with AggressiveAi {
  override val isEnemy: Boolean = true

  val activeSoundTimer: EsTimer = EsTimer()
  val activeSound: Option[Sound] = None
  var activeSoundTimeout: Float = 3f + GameSystem.randomGenerator.nextFloat() * 8f

  protected val dropTable: Map[String, Float] = Map()

  override def calculateFacingVector(): Creature = {
    if (aggroedTarget.nonEmpty) {
      val aggroed = aggroedTarget.get
      facingVector.x = aggroed.pos.x - pos.x
      facingVector.y = aggroed.pos.y - pos.y
      facingVector.nor()
    } else {
      facingVector.x = 0
      facingVector.y = 0
    }

    this
  }

  override def update(): Creature = {
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

    this
  }

  override def takeLifeDamage(
    damage: Float,
    immunityFrames: Boolean,
    dealtBy: Option[Creature] = None,
    attackKnockbackVelocity: Float = 0,
    sourceX: Float = 0,
    sourceY: Float = 0
  ): Creature = {
    super.takeLifeDamage(damage, immunityFrames, dealtBy, attackKnockbackVelocity, sourceX, sourceY)

    if (isAlive && aggroedTarget.isEmpty && dealtBy.nonEmpty) {
      aggroOnCreature(dealtBy.get)
    }

    this
  }

  override def onDeath(): Creature = {
    super.onDeath()

    areaMap(params.areaId.get).spawnLootPile(pos.x, pos.y, dropTable)
    aggroedTarget = None

    this
  }

  def spawnPosition: Vector2 = {
    val spawnPoint = areaMap(params.areaId.get).enemySpawns.filter(_.id == spawnPointId.get).head
    new Vector2(spawnPoint.posX, spawnPoint.posY)
  }

  override def calculateWalkingVector(): Creature = {
    walkingVector = facingVector.cpy()

    this
  }
}
