package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.GameSystem._
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.assets.Assets
import com.easternsauce.libgdxgame.creature.traits._
import com.easternsauce.libgdxgame.spawns.PlayerSpawnPoint
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

abstract class Creature
    extends Sprite
    with PhysicalBody
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

  val id: String

  val creatureWidth: Float
  val creatureHeight: Float

  var currentDirection: EsDirection.Value = EsDirection.Down

  var isMoving = false
  val timeSinceMovedTimer: EsTimer = EsTimer()

  val directionalSpeed = 18f

  var area: Option[Area] = None

  val onGettingHitSound: Option[Sound] = None
  val walkSound: Option[Sound] = None

  var attackVector: Vector2 = new Vector2(0f, 0f)

  var facingVector: Vector2 = new Vector2(0f, 0f)

  var passedGateRecently = false
  var toSetBodyNonInteractive = false

  var spawnPointId: Option[String] = None

  val onItemConsumeSound: Sound = sound(Assets.appleCrunchSound)

  var sprinting = false

  var playerSpawnPoint: Option[PlayerSpawnPoint] = None

  def calculateFacingVector(): Unit

  protected def creatureType: String = getClass.getName

  def totalArmor: Float = equipmentItems.values.map(item => item.armor.getOrElse(0)).sum

  def update(): Unit = {
    if (alive) {
      updateStaminaDrain()

      calculateFacingVector()

      regenerateLife()
      regenerateStamina()

      handlePoison()

    }

    updateEffects()

    for (ability <- abilityList) {
      ability.update()
    }

    currentAttack.update()

    updateStamina()

    if (toSetBodyNonInteractive) {
      setNonInteractive()
      toSetBodyNonInteractive = false
    }

    if (isMoving) setRegion(walkAnimationFrame(currentDirection))
    else setRegion(standStillImage(currentDirection))

    if (bodyExists) {
      setPosition(pos.x - getWidth / 2f, pos.y - getHeight / 2f)
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

    for (ability <- abilityList) {
      ability.forceStop()
    }
    currentAttack.forceStop()

    setRotation(90f)

    toSetBodyNonInteractive = true
  }

  def moveInDirection(dirs: List[EsDirection.Value]): Unit = {

    if (dirs.nonEmpty) {
      if (alive) {
        timeSinceMovedTimer.restart()

        if (!isMoving) {
          animationTimer.restart()
          isMoving = true
          if (walkSound.nonEmpty) walkSound.get.loop(0.1f)
        }

        val vector = new Vector2()

        currentDirection = dirs.last

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

  def assignToArea(area: Area, x: Float, y: Float): Unit = {
    if (this.area.isEmpty) {
      this.area = Some(area)
      initCircularBody(area.world, x, y, creatureWidth / 2f)

      area.creaturesMap += (id -> this)

    } else {
      val oldArea = this.area.get

      destroyBody(oldArea.world)
      oldArea.creaturesMap -= id

      this.area = Some(area)
      initCircularBody(area.world, x, y, creatureWidth / 2f)

      area.creaturesMap += (id -> this)

    }

  }

}
