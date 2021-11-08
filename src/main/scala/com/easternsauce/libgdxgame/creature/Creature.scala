package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.templates.{Ability, AbilityFactory}
import com.easternsauce.libgdxgame.creature.traits._
import com.easternsauce.libgdxgame.effect.Effect
import com.easternsauce.libgdxgame.items.Item
import com.easternsauce.libgdxgame.spawns.PlayerSpawnPoint
import com.easternsauce.libgdxgame.system.GameSystem.areaMap
import com.easternsauce.libgdxgame.system.{Assets, GameSystem}
import com.easternsauce.libgdxgame.util.{EsBatch, EsDirection, EsTimer}
import com.easternsauce.libgdxgame.wrapper.Sprite
import com.softwaremill.quicklens.ModifyPimp

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class Creature(val id: String, val params: CreatureParameters)
    extends PhysicalBody
    with AnimatedWalk
    with Inventory
    with SavefileParser
    with Effects
    with Life
    with Stamina
    with Abilities {

  val isEnemy = false
  val isPlayer = false
  val isNPC = false
  val isBoss = false

  var sprite: Sprite = Sprite()

  var currentDirection: EsDirection.Value = EsDirection.Down

  val creatureWidth: Float
  val creatureHeight: Float

  var isMoving = false
  var timeSinceMovedTimer: EsTimer = EsTimer()

  val directionalSpeed = 18f

  val onGettingHitSound: Option[Sound] = None
  val walkSound: Option[Sound] = None

  var attackVector: Vector2 = new Vector2(0f, 0f)

  var facingVector: Vector2 = new Vector2(0f, 0f)
  var walkingVector: Vector2 = new Vector2(0f, 0f)

  var passedGateRecently = false
  var toSetBodyNonInteractive = false

  var spawnPointId: Option[String] = None

  val onItemConsumeSound: Sound = Assets.sound(Assets.appleCrunchSound)

  var sprinting = false

  var playerSpawnPoint: Option[PlayerSpawnPoint] = None

  var recentDirections: ListBuffer[EsDirection.Value] = ListBuffer()

  var updateDirectionTimer: EsTimer = EsTimer(true)

  val standardAbilities: List[String] = List("shoot_arrow", "slash", "thrust")
  val additionalAbilities: List[String]

  var abilities: mutable.Map[String, Ability] = mutable.Map()

  def calculateFacingVector(): Creature
  def calculateWalkingVector(): Creature

  protected def creatureType: String = getClass.getName

  def totalArmor: Float = equipmentItems.values.map(item => item.armor.getOrElse(0)).sum.toFloat

  def update(): Creature = {
    val updated1 = if (params.isInitialized && isAlive) {

      calculateFacingVector()
      calculateWalkingVector()

      regenerateLife()
      val updated = regenerateStamina()

      updated.handlePoison()

      updated
    } else this


    updated1.updateEffects()

    // temporary, until this class is immutable
    for ((id, ability) <- updated1.abilities) {
      updated1.abilities.update(id, ability.update())
    }

    val updated2 = updated1.updateStamina()

    if (updated2.toSetBodyNonInteractive) {
      updated2.setNonInteractive()
      updated2.toSetBodyNonInteractive = false
    }

    if (updated2.isMoving) updated2.sprite = updated2.sprite.setRegion(updated2.walkAnimationFrame(updated2.currentDirection))
    else updated2.sprite = updated2.sprite.setRegion(updated2.standStillImage(updated2.currentDirection))

    if (updated2.params.bodyCreated) {
      val roundedX = (math.floor(updated2.pos.x * 100) / 100).toFloat
      val roundedY = (math.floor(updated2.pos.y * 100) / 100).toFloat
      updated2.sprite = updated2.sprite.setPosition(roundedX - updated2.sprite.width / 2f, roundedY - updated2.sprite.height / 2f)
    }

    if (updated2.isMoving && updated2.timeSinceMovedTimer.time > 0.25f) {
      updated2.isMoving = false
      if (updated2.walkSound.nonEmpty) updated2.walkSound.get.stop()
    }

    updated2.handleKnockback()

    updated2
  }

  def onDeath(): Creature = {
    isMoving = false
    if (walkSound.nonEmpty) walkSound.get.stop()

    for (ability <- abilities.values) {
      ability.forceStop()
    }
    currentAttack.forceStop()

    sprite = sprite.setRotation(90f)

    toSetBodyNonInteractive = true

    this
  }

  def moveInDirection(dirs: List[EsDirection.Value]): Creature = {

    if (dirs.nonEmpty) {
      if (isAlive) {
        timeSinceMovedTimer.restart()

        if (!isMoving) {
          animationTimer.restart()
          isMoving = true
          if (walkSound.nonEmpty) walkSound.get.loop(0.1f)
        }

        val vector = new Vector2(0f, 0f)

        val creature = updateDirection(dirs.last)

        val modifiedSpeed =
          if (isAttacking) directionalSpeed / 3f
          else if (sprinting && params.staminaPoints > 0) directionalSpeed * 1.75f
          else directionalSpeed

        dirs.foreach {
          case EsDirection.Up    => vector.y += modifiedSpeed
          case EsDirection.Down  => vector.y -= modifiedSpeed
          case EsDirection.Left  => vector.x -= modifiedSpeed
          case EsDirection.Right => vector.x += modifiedSpeed
        }

        val horizontalCount = dirs.count(EsDirection.isHorizontal)
        val verticalCount = dirs.count(EsDirection.isVertical)

        if (ableToMove) {
          if (horizontalCount < 2 && verticalCount < 2) {
            if (horizontalCount > 0 && verticalCount > 0) {
              sustainVelocity(new Vector2(vector.x / Math.sqrt(2).toFloat, vector.y / Math.sqrt(2).toFloat))
            } else {
              sustainVelocity(vector)
            }
          }
        }

        creature
      } else this

    } else this
  }

  private def updateDirection(dir: EsDirection.Value): Creature = {
    if (updateDirectionTimer.time > 0.01f) {
      if (recentDirections.size > 11) {
        recentDirections.dropInPlace(1)
      }

      val creature = if (recentDirections.nonEmpty) {

        val newCurrentDirection = recentDirections.groupBy(identity).view.mapValues(_.size).maxBy(_._2)._1

        currentDirection = newCurrentDirection // TODO: this will be val
        this
      } else this

      recentDirections += dir

      updateDirectionTimer.restart()

      creature
    } else this

  }

  def assignToArea(areaId: String, x: Float, y: Float): Creature = {
    if (params.areaId.isEmpty) {

      val newAreaId = Some(areaId)

      val (body, fixture) = initBody(areaMap(areaId).world, x, y, creatureWidth / 2f)

      val creature = this
        .modify(_.params.body)
        .setTo(body)
        .modify(_.params.fixture)
        .setTo(fixture)
        .modify(_.params.areaId)
        .setTo(newAreaId)
        .modify(_.params.bodyCreated)
        .setTo(true)

      creature
    } else {
      val oldArea = areaMap(params.areaId.get)

      val newAreaId = Some(areaId)
      val (body, fixture) = initBody(areaMap(params.areaId.get).world, x, y, creatureWidth / 2f)

      val creature = this
        .modify(_.params.body)
        .setTo(body)
        .modify(_.params.fixture)
        .setTo(fixture)
        .modify(_.params.areaId)
        .setTo(newAreaId)
        .modify(_.params.bodyCreated)
        .setTo(true)
        .destroyBody(oldArea.world)

      creature
    }

  }

  def init(): Creature = {
    setupAnimation()

    sprite = sprite.setBounds(0, 0, creatureWidth, creatureHeight)
    sprite = sprite.setOrigin(creatureWidth / 2f, creatureHeight / 2f)

    abilities = mutable.Map() ++
      (for (abilityId <- standardAbilities ++ additionalAbilities)
        yield abilityId -> AbilityFactory.ability(abilityId, id)).toMap

    defineEffects()

    sprite = sprite.setRegion(standStillImage(currentDirection))
    sprite.setRegion(standStillImage(currentDirection))

    life = maxLife

    this
      .modify(_.params.isInitialized)
      .setTo(true)
  }

  def render(batch: EsBatch): Creature = {

    if (isAlive && isImmune) {
      val alpha = effectMap("immune").getRemainingTime * 35f
      val colorComponent = 0.3f + 0.7f * (Math.sin(alpha).toFloat + 1f) / 2f

      sprite = sprite.setColor(1f, colorComponent, colorComponent, 1f)
    }

    sprite = sprite.draw(batch.spriteBatch)
    sprite = sprite.setColor(1, 1, 1, 1)

    this
  }

  def copy(
    isMoving: Boolean = isMoving,
    timeSinceMovedTimer: EsTimer = timeSinceMovedTimer,
    attackVector: Vector2 = attackVector,
    facingVector: Vector2 = facingVector,
    walkingVector: Vector2 = walkingVector,
    passedGateRecently: Boolean = passedGateRecently,
    toSetBodyNonInteractive: Boolean = toSetBodyNonInteractive,
    spawnPointId: Option[String] = spawnPointId,
    sprinting: Boolean = sprinting,
    playerSpawnPoint: Option[PlayerSpawnPoint] = playerSpawnPoint,
    recentDirections: ListBuffer[EsDirection.Value] = recentDirections,
    updateDirectionTimer: EsTimer = updateDirectionTimer,
    abilities: mutable.Map[String, Ability] = abilities,
    mass: Float = mass,
    standStillImages: Array[TextureRegion] = standStillImages,
    walkAnimation: Array[Animation[TextureRegion]] = walkAnimation,
    animationTimer: EsTimer = animationTimer,
    dirMap: Map[EsDirection.Value, Int] = dirMap,
    animationParams: AnimationParams = animationParams,
    equipmentItems: mutable.Map[Int, Item] = equipmentItems,
    inventoryItems: mutable.Map[Int, Item] = inventoryItems,
    effectMap: mutable.Map[String, Effect] = effectMap,
    life: Float = life,
    lifeRegenerationTimer: EsTimer = lifeRegenerationTimer,
    healingTimer: EsTimer = healingTimer,
    healingTickTimer: EsTimer = healingTickTimer,
    healing: Boolean = healing,
    staminaRegenerationTimer: EsTimer = staminaRegenerationTimer,
    staminaOveruseTimer: EsTimer = staminaOveruseTimer,
    isAttacking: Boolean = isAttacking,
    params: CreatureParameters,
    sprite: Sprite = sprite,
    currentDirection: EsDirection.Value = currentDirection

  ): Creature
}
