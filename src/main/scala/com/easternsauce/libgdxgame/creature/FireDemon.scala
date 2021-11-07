package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.{Music, Sound}
import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.templates.Ability
import com.easternsauce.libgdxgame.creature.traits.{AbilityUsage, AnimationParams, Boss}
import com.easternsauce.libgdxgame.effect.Effect
import com.easternsauce.libgdxgame.items.Item
import com.easternsauce.libgdxgame.spawns.PlayerSpawnPoint
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.{CreatureInfo, EsDirection, EsTimer}
import com.easternsauce.libgdxgame.wrapper.Sprite

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class FireDemon private (override val id: String, override val params: CreatureParameters = CreatureParameters())
    extends Boss(id = id, params = params) {

  override val creatureWidth = 7.5f
  override val creatureHeight = 7.5f

  val spriteWidth = 80
  val spriteHeight = 80

  override val maxLife = 5500f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.roarSound))

  override val aggroDropDistance = 999f

  override val directionalSpeed: Float = 25f

  override val bossMusic: Option[Music] = Some(Assets.music(Assets.fireDemonMusic))

  override val name = "Magma Stalker"

  override val dropTable =
    Map("ironSword" -> 0.3f, "poisonDagger" -> 0.3f, "steelArmor" -> 0.8f, "steelHelmet" -> 0.5f, "thiefRing" -> 1.0f)

  override val abilityUsages: Map[String, AbilityUsage] =
    Map(
      "dash" -> AbilityUsage(weight = 70f, minimumDistance = 15f),
      "meteorRain" -> AbilityUsage(weight = 30f, minimumDistance = 4f, lifeThreshold = 0.6f),
      "fistSlam" -> AbilityUsage(weight = 30f, minimumDistance = 4f, maximumDistance = 10f),
      "meteorCrash" -> AbilityUsage(weight = 30f, minimumDistance = 6f)
    )

  override var animationParams: AnimationParams = AnimationParams(
    regionName = "taurus",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = 4,
    frameDuration = 0.15f,
    neutralStanceFrame = 0,
    dirMap = Map(EsDirection.Left -> 1, EsDirection.Right -> 2, EsDirection.Up -> 3, EsDirection.Down -> 0)
  )

  mass = 10000f

  // TODO: how to get rid of casting?
  // TODO: refactor this before uncommenting!
  // abilities("thrust").asInstanceOf[ThrustAttack].attackRange = 1.5f

  override val additionalAbilities: List[String] = FireDemon.additionalAbilities

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
    staminaRegenerationTimer: EsTimer,
    staminaOveruseTimer: EsTimer,
    isAttacking: Boolean,
    params: CreatureParameters,
    sprite: Sprite,
    currentDirection: EsDirection.Value
  ): Creature = {
    val creature = FireDemon(id = id, params = params)
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
    creature.staminaRegenerationTimer = staminaRegenerationTimer
    creature.staminaOveruseTimer = staminaOveruseTimer
    creature.isAttacking = isAttacking
    creature.sprite = sprite
    creature.currentDirection = currentDirection

    if (creature.params.body.nonEmpty) creature.params.body.get.setUserData(creature)

    creature
  }
}

object FireDemon extends CreatureInfo {
  def apply(id: String, params: CreatureParameters = CreatureParameters()): FireDemon = {
    new FireDemon(id = id, params = params)
  }

  override val additionalAbilities: List[String] = List("meteor_rain", "fist_slam", "meteor_crash", "dash")
}
