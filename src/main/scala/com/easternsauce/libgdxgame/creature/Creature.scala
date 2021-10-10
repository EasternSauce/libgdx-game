package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.attack.{ShootArrowAttack, SlashAttack, ThrustAttack}
import com.easternsauce.libgdxgame.ability.misc.Action
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.traits._
import com.easternsauce.libgdxgame.spawns.PlayerSpawnPoint
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.{EsBatch, EsDirection, EsTimer}
import com.softwaremill.quicklens.ModifyPimp

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class Creature(
  val id: String,
  val area: Option[Area] = None,
  val b2Body: Option[Body] = None,
  val standardAbilities: Map[String, Action] = Map(),
  val additionalAbilities: Map[String, Action] = Map()
) extends Sprite
    with PhysicalBody
    with AnimatedWalk
    with Inventory
    with SavefileParser
    with Effects
    with Life
    with Stamina
    with Abilities {
  type Self >: this.type <: Creature

  val isEnemy = false
  val isPlayer = false
  val isNPC = false
  val isBoss = false

  val creatureWidth: Float
  val creatureHeight: Float

  var isInitialized = false

  var currentDirection: EsDirection.Value = EsDirection.Down

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

  // TODO: refactor this
  lazy val abilityMap: mutable.Map[String, Action] = {
    val map = mutable.Map[String, Action]()
    map.addAll(standardAbilities)
    map.addAll(additionalAbilities)

    map
  }

  def calculateFacingVector(): Unit
  def calculateWalkingVector(): Unit

  protected def creatureType: String = getClass.getName

  def totalArmor: Float = equipmentItems.values.map(item => item.armor.getOrElse(0)).sum.toFloat

  def update(): Unit = {
    if (isPlayer) println("body is " + b2Body)

    if (isInitialized && isAlive) {
      updateStaminaDrain()

      calculateFacingVector()
      calculateWalkingVector()

      regenerateLife()
      regenerateStamina()

      handlePoison()

    }

    updateEffects()

    for ((id, ability) <- abilityMap) {
      abilityMap.update(id, ability.update())
    }

    //currentAttack.update()

    updateStamina()

    if (toSetBodyNonInteractive) {
      setNonInteractive()
      toSetBodyNonInteractive = false
    }

    if (isMoving) setRegion(walkAnimationFrame(currentDirection))
    else setRegion(standStillImage(currentDirection))

    if (b2Body.nonEmpty) {
      val roundedX = (math.floor(pos.x * 100) / 100).toFloat
      val roundedY = (math.floor(pos.y * 100) / 100).toFloat
      setPosition(roundedX - getWidth / 2f, roundedY - getHeight / 2f)
    }

    if (isMoving && timeSinceMovedTimer.time > 0.25f) {
      isMoving = false
      if (walkSound.nonEmpty) walkSound.get.stop()
    }

    handleKnockback()

  }

  def onDeath(): Unit = {
    isMoving = false
    if (walkSound.nonEmpty) walkSound.get.stop()

    for (ability <- abilityMap.values) {
      ability.forceStop()
    }
    currentAttack.forceStop()

    setRotation(90f)

    toSetBodyNonInteractive = true
  }

  def moveInDirection(dirs: List[EsDirection.Value]): Unit = {

    if (dirs.nonEmpty) {
      if (isAlive) {
        timeSinceMovedTimer.restart()

        if (!isMoving) {
          animationTimer.restart()
          isMoving = true
          if (walkSound.nonEmpty) walkSound.get.loop(0.1f)
        }

        val vector = new Vector2(0f, 0f)

        updateDirection(dirs.last)

        val modifiedSpeed =
          if (isAttacking) directionalSpeed / 3f
          else if (sprinting && staminaPoints > 0) directionalSpeed * 1.75f
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
      }
    }

  }

  private def updateDirection(dir: EsDirection.Value): Unit = {
    if (updateDirectionTimer.time > 0.01f) {
      if (recentDirections.size > 11) {
        recentDirections.dropInPlace(1)
      }

      if (recentDirections.nonEmpty) {
        currentDirection = recentDirections.groupBy(identity).view.mapValues(_.size).maxBy(_._2)._1
      }

      recentDirections += dir

      updateDirectionTimer.restart()

    }
  }

  def assignToArea(area: Area, x: Float, y: Float): Creature = {
    val newArea = Some(area)

    val creature: Creature = this.modify(_.area).setTo(newArea)

    area.creaturesMap += (id -> this)

    if (this.area.isEmpty) {
      val body = creature.initBody(area.world, x, y, creatureWidth / 2f)

      creature.modify(_.b2Body).setTo(body)
    } else {
      val oldArea = this.area.get

      oldArea.creaturesMap -= id

      val body = initBody(area.world, x, y, creatureWidth / 2f)

      creature
        .destroyBody(oldArea.world)
        .modify(_.b2Body)
        .setTo(body)

    }
  }

  def init(): Unit = {
    if (!isInitialized) {
      setupAnimation()

      setBounds(0, 0, creatureWidth, creatureHeight)
      setOrigin(creatureWidth / 2f, creatureHeight / 2f)

      defineEffects()

      setRegion(standStillImage(currentDirection))

      life = maxLife

      isInitialized = true
    }
  }

  def render(batch: EsBatch): Unit = {

    if (isAlive && isImmune) {
      val alpha = effectMap("immune").getRemainingTime * 35f
      val colorComponent = 0.3f + 0.7f * (Math.sin(alpha).toFloat + 1f) / 2f

      setColor(1f, colorComponent, colorComponent, 1f)
    }

    draw(batch.spriteBatch)
    setColor(1, 1, 1, 1)

  }

  def _temp_copyVars[T <: Creature](that: T): T = {
    isInitialized = that.isInitialized
    currentDirection = that.currentDirection
    isMoving = that.isMoving
    timeSinceMovedTimer = that.timeSinceMovedTimer
    timeSinceMovedTimer = that.timeSinceMovedTimer
    attackVector = that.attackVector
    facingVector = that.facingVector
    walkingVector = that.walkingVector
    passedGateRecently = that.passedGateRecently
    toSetBodyNonInteractive = that.toSetBodyNonInteractive
    spawnPointId = that.spawnPointId
    sprinting = that.sprinting
    playerSpawnPoint = that.playerSpawnPoint
    recentDirections = that.recentDirections
    updateDirectionTimer = that.updateDirectionTimer

    this.modify(_.additionalAbilities).setTo(that.additionalAbilities).asInstanceOf[T]
  }

  def copy(
    id: String = id,
    area: Option[Area] = area,
    b2Body: Option[Body] = b2Body,
    standardAbilities: Map[String, Action] = standardAbilities,
    additionalAbilities: Map[String, Action] = additionalAbilities
  ): Self
}
