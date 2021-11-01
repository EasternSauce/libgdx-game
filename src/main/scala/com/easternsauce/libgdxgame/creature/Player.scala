package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.badlogic.gdx.math.Vector2
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

class Player private (override val id: String, override val params: CreatureParameters = CreatureParameters())
    extends Creature(id = id, params = params) {

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

  override def calculateFacingVector(): Creature = {
    val mouseX = Gdx.input.getX
    val mouseY = Gdx.input.getY

    val centerX = Gdx.graphics.getWidth / 2f
    val centerY = Gdx.graphics.getHeight / 2f

    // we need to reverse y due to mouse coordinates being in different system
    facingVector = new Vector2(mouseX - centerX, (Gdx.graphics.getHeight - mouseY) - centerY).nor()

    this
  }

  override def onDeath(): Creature = {
    super.onDeath()

    respawnTimer.restart()
    respawning = true
    sprinting = false

    this
  }

  def onRespawn(): Creature = {
    GameSystem.bossfightManager.stopBossfight()

    this
  }

  def interact(): Creature = {
    if (onSpawnPointId.nonEmpty) {
      playerSpawnPoint = Some(areaMap(params.areaId.get).playerSpawns.filter(_.id == onSpawnPointId.get).head)
      playerSpawnPoint.get.onRespawnSet()
    }

    this
  }

  override def calculateWalkingVector(): Creature = {
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

    this
  }

  override val additionalAbilities: List[String] = Player.additionalAbilities

  override def copy(
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
    mass: Float,
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
    isAttacking: Boolean,
    params: CreatureParameters
  ): Creature = {
    val creature = Player(id = id, params = params)
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
    creature.mass = mass
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

    if (creature.params.body.nonEmpty) creature.params.body.get.setUserData(creature)

    creature
  }
}

object Player extends CreatureInfo {
  def apply(id: String, params: CreatureParameters = CreatureParameters()): Player = {
    new Player(id = id, params = params)
  }

  override val additionalAbilities: List[String] = List("dash")
}
