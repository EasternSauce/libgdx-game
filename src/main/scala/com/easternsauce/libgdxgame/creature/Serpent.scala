package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.templates.Ability
import com.easternsauce.libgdxgame.creature.traits.{AbilityUsage, AnimationParams}
import com.easternsauce.libgdxgame.effect.Effect
import com.easternsauce.libgdxgame.items.Item
import com.easternsauce.libgdxgame.spawns.PlayerSpawnPoint
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.{CreatureInfo, EsDirection, EsTimer}
import com.easternsauce.libgdxgame.wrapper.Sprite

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Serpent private (override val id: String, override val params: CreatureParameters = CreatureParameters())
    extends Enemy(id = id, params = params) {
  override val creatureWidth = 3.85f
  override val creatureHeight = 3.85f

  override val maxLife = 160f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.boneClickSound))

  override val activeSound: Option[Sound] = Some(Assets.sound(Assets.boneRattleSound))

  override val dropTable = Map(
    "ringmailGreaves" -> 0.1f,
    "leatherArmor" -> 0.05f,
    "hideGloves" -> 0.1f,
    "leatherHelmet" -> 0.1f,
    "woodenSword" -> 0.1f,
    "healingPowder" -> 0.5f
  )

  override val abilityUsages: Map[String, AbilityUsage] =
    Map(
      "bubble" -> AbilityUsage(weight = 100f, minimumDistance = 2f),
      "ice_shard" -> AbilityUsage(weight = 100f, minimumDistance = 2f)
    )

  override var animationParams: AnimationParams = AnimationParams(
    regionName = "serpent",
    textureWidth = 48,
    textureHeight = 56,
    animationFrameCount = 3,
    frameDuration = 0.15f,
    neutralStanceFrame = 0,
    dirMap = Map(EsDirection.Up -> 0, EsDirection.Down -> 2, EsDirection.Left -> 1, EsDirection.Right -> 3)
  )

  override val additionalAbilities: List[String] = Serpent.additionalAbilities

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
    val creature = Serpent(id = id, params = params)
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

object Serpent extends CreatureInfo {
  def apply(id: String, params: CreatureParameters = CreatureParameters()): Serpent = {
    new Serpent(id = id, params = params)
  }

  override val additionalAbilities: List[String] = List("bubble", "ice_shard")
}
