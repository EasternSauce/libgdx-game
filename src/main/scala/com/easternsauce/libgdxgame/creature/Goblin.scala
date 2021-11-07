package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.{Animation, Sprite, TextureRegion}
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.templates.Ability
import com.easternsauce.libgdxgame.creature.traits.AnimationParams
import com.easternsauce.libgdxgame.effect.Effect
import com.easternsauce.libgdxgame.items.Item
import com.easternsauce.libgdxgame.spawns.PlayerSpawnPoint
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.{CreatureInfo, EsDirection, EsTimer}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Goblin private (override val id: String, override val params: CreatureParameters = CreatureParameters())
    extends Enemy(id = id, params = params) {
  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val maxLife = 190f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.evilYellingSound))

  override val dropTable = Map(
    "ironSword" -> 0.03f,
    "poisonDagger" -> 0.07f,
    "healingPowder" -> 0.3f,
    "steelArmor" -> 0.03f,
    "steelGreaves" -> 0.05f,
    "steelGloves" -> 0.05f,
    "steelHelmet" -> 0.05f
  )

  override var animationParams: AnimationParams = AnimationParams(
    regionName = "goblin",
    textureWidth = 32,
    textureHeight = 32,
    animationFrameCount = 3,
    frameDuration = 0.25f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  override val additionalAbilities: List[String] = Goblin.additionalAbilities

  override def copy(
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
    params: CreatureParameters,
    sprite: Sprite
  ): Creature = {
    val creature = Goblin(id = id, params = params)
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
    creature.sprite = new Sprite(sprite) // refactor later

    if (creature.params.body.nonEmpty) creature.params.body.get.setUserData(creature)

    creature
  }
}

object Goblin extends CreatureInfo {
  def apply(id: String, params: CreatureParameters = CreatureParameters()): Goblin = {
    new Goblin(id = id, params = params)
  }

  override val additionalAbilities: List[String] = List()
}
