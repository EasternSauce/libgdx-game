package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.effect.Effect
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

import scala.collection.mutable

trait Creature extends Sprite with PhysicalBody with Animated {

  val isEnemy: Boolean = false

  val id: String

  val creatureWidth: Float
  val creatureHeight: Float

  var currentDirection: EsDirection.Value = EsDirection.Down

  var isWalkAnimationActive = false
  val timeSinceMovedTimer: EsTimer = EsTimer()

  val initX: Float
  val initY: Float

  val directionalSpeed = 30f

  protected var area: Option[Area] = None

  var maxHealthPoints = 100f
  var healthPoints: Float = maxHealthPoints
  val maxStaminaPoints = 100f
  var staminaPoints: Float = maxStaminaPoints

  protected val effectMap: mutable.Map[String, Effect] = mutable.Map()

  def pos: Vector2 = b2Body.getPosition

  def alive: Boolean = {
    true // TODO
  }

  def initParams(mass: Float): Unit = {
    this.mass = mass
  }

  def update(): Unit = {

    if (isWalkAnimationActive) setRegion(walkAnimationFrame(currentDirection))
    else setRegion(standStillImage(currentDirection))

    setPosition(pos.x - getWidth / 2f, pos.y - getHeight / 2f)

    if (isWalkAnimationActive && timeSinceMovedTimer.time > 0.25f) isWalkAnimationActive = false
  }

  def moveInDirection(dirs: List[EsDirection.Value]): Unit = {

    timeSinceMovedTimer.restart()

    if (!isWalkAnimationActive) {
      animationTimer.restart()
      isWalkAnimationActive = true
    }

    val vector = new Vector2()

    currentDirection = dirs.last

    dirs.foreach {
      case EsDirection.Up    => vector.y += directionalSpeed
      case EsDirection.Down  => vector.y -= directionalSpeed
      case EsDirection.Left  => vector.x -= directionalSpeed
      case EsDirection.Right => vector.x += directionalSpeed
    }

    val horizontalCount = dirs.count(EsDirection.isHorizontal)
    val verticalCount = dirs.count(EsDirection.isVertical)

    if (horizontalCount < 2 && verticalCount < 2) {
      if (horizontalCount > 0 && verticalCount > 0) {
        sustainVelocity(new Vector2(vector.x / Math.sqrt(2).toFloat, vector.y / Math.sqrt(2).toFloat))
      } else {
        sustainVelocity(vector)
      }
    }
  }

  def assignToArea(area: Area): Unit = {
    if (this.area.isEmpty) {
      this.area = Some(area)
      initCircularBody(area.world, initX, initY, creatureWidth / 2f)

      area.creatureMap += (id -> this)
    } else {
      val oldArea = this.area.get

      oldArea.world.destroyBody(b2Body)
      oldArea.creatureMap -= id

      this.area = Some(area)
      initCircularBody(area.world, initX, initY, creatureWidth / 2f)

      area.creatureMap += (id -> this)
    }

  }

  def effect(effectName: String): Effect = {
    effectMap.get(effectName) match {
      case Some(effect) => effect
      case _ =>
        throw new RuntimeException("tried to access non-existing effect: " + effectName)
    }
  }
}
