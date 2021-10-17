package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, Fixture}
import com.easternsauce.libgdxgame.ability.misc.templates.Ability
import com.easternsauce.libgdxgame.creature.traits.AnimationParams
import com.easternsauce.libgdxgame.effect.Effect
import com.easternsauce.libgdxgame.items.Item
import com.easternsauce.libgdxgame.spawns.PlayerSpawnPoint
import com.easternsauce.libgdxgame.system.GameSystem.areaMap
import com.easternsauce.libgdxgame.system.{Assets, GameSystem}
import com.easternsauce.libgdxgame.util.{CreatureInfo, EsDirection, EsTimer}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Player private (override val id: String) extends Creature(id = id) {

  override val creatureWidth = 1.85f
  override val creatureHeight = 1.85f

  override val maxLife = 200f

  override val isPlayer: Boolean = true

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.painSound))

  override val walkSound: Option[Sound] = Some(Assets.sound(Assets.runningSound))

  var onSpawnPointId: Option[String] = None

  var respawning: Boolean = false
  val respawnTimer: EsTimer = EsTimer()

  override var animationParams: AnimationParams = AnimationParams(
    regionName = "male1",
    textureWidth = 32,
    textureHeight = 32,
    animationFrameCount = 3,
    frameDuration = 0.1f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  override def calculateFacingVector(): Unit = {
    val mouseX = Gdx.input.getX
    val mouseY = Gdx.input.getY

    val centerX = Gdx.graphics.getWidth / 2f
    val centerY = Gdx.graphics.getHeight / 2f

    // we need to reverse y due to mouse coordinates being in different system
    facingVector = new Vector2(mouseX - centerX, (Gdx.graphics.getHeight - mouseY) - centerY).nor()
  }

  override def onDeath(): Unit = {
    super.onDeath()

    respawnTimer.restart()
    respawning = true
    sprinting = false

  }

  def onRespawn(): Unit = {
    GameSystem.bossfightManager.stopBossfight()
  }

  def interact(): Unit = {
    if (onSpawnPointId.nonEmpty) {
      playerSpawnPoint = Some(areaMap(areaId.get).playerSpawns.filter(_.id == onSpawnPointId.get).head)
      playerSpawnPoint.get.onRespawnSet()
    }
  }

  override def calculateWalkingVector(): Unit = {
    val dirs: List[EsDirection.Value] = GameSystem.playerMovementDirections

    val vector = new Vector2(0f, 0f)

    dirs.foreach {
      case EsDirection.Up    => vector.y += 1f
      case EsDirection.Down  => vector.y -= 1f
      case EsDirection.Left  => vector.x -= 1f
      case EsDirection.Right => vector.x += 1f
    }

    vector.nor()

    walkingVector = vector

  }

  override val additionalAbilities: List[String] = Player.additionalAbilities


  override def copy(
                     areaId: Option[String],
                     isInitialized: Boolean,
                     currentDirection: EsDirection.Value,
                     isMoving: Boolean,
                     timeSinceMovedTimer: EsTimer,
                     attackVector: Vector2,
                     facingVector: Vector2,
                     walkingVector: Vector2,
                     passedGateRecently: Boolean,
                     toSetBodyNonInteractive: Boolean,
                     spawnPointId: Option[String],
                     sprinting: Boolean,
                     playerSpawnPoint: Option[PlayerSpawnPoint],
                     recentDirections: ListBuffer[EsDirection.Value],
                     updateDirectionTimer: EsTimer,
                     abilities: mutable.Map[String, Ability],
                     b2Body: Body,
                     b2fixture: Fixture,
                     mass: Float,
                     bodyCreated: Boolean,
                     standStillImages: Array[TextureRegion],
                     walkAnimation: Array[Animation[TextureRegion]],
                     animationTimer: EsTimer,
                     dirMap: Map[EsDirection.Value, Int],
                     animationParams: AnimationParams,
                     equipmentItems: mutable.Map[Int, Item],
                     inventoryItems: mutable.Map[Int, Item],
                     effectMap: mutable.Map[String, Effect],
                     life: Float,
                     lifeRegenerationTimer: EsTimer,
                     healingTimer: EsTimer,
                     healingTickTimer: EsTimer,
                     healing: Boolean,
                     staminaPoints: Float,
                     staminaRegenerationTimer: EsTimer,
                     staminaOveruseTimer: EsTimer,
                     staminaOveruse: Boolean,
                     isAttacking: Boolean
                   ): Creature = {
    val creature = Player(id)
    creature.areaId = areaId
    creature.isInitialized = isInitialized
    creature.currentDirection = currentDirection
    creature.isMoving = isMoving
    creature.timeSinceMovedTimer = timeSinceMovedTimer
    creature.attackVector = attackVector
    creature.facingVector = facingVector
    creature.walkingVector = walkingVector
    creature.passedGateRecently = passedGateRecently
    creature.toSetBodyNonInteractive = toSetBodyNonInteractive
    creature.spawnPointId = spawnPointId
    creature.sprinting = sprinting
    creature.playerSpawnPoint = playerSpawnPoint
    creature.recentDirections = recentDirections
    creature.updateDirectionTimer = updateDirectionTimer
    creature.abilities = abilities
    creature.b2Body = b2Body
    creature.b2fixture = b2fixture
    creature.mass = mass
    creature.bodyCreated = bodyCreated
    creature.standStillImages = standStillImages
    creature.walkAnimation = walkAnimation
    creature.animationTimer = animationTimer
    creature.dirMap = dirMap
    creature.animationParams = animationParams
    creature.equipmentItems = equipmentItems
    creature.inventoryItems = inventoryItems
    creature.effectMap = effectMap
    creature.life = life
    creature.lifeRegenerationTimer = lifeRegenerationTimer
    creature.healingTimer = healingTimer
    creature.healingTickTimer = healingTickTimer
    creature.healing = healing
    creature.staminaPoints = staminaPoints
    creature.staminaRegenerationTimer = staminaRegenerationTimer
    creature.staminaOveruseTimer = staminaOveruseTimer
    creature.staminaOveruse = staminaOveruse
    creature.isAttacking = isAttacking
    creature
  }
}

object Player extends CreatureInfo {
  def apply(id: String): Player = {
    val obj = new Player(id)
    obj.init()
    obj
  }

  override val additionalAbilities: List[String] = List("dash")
}
