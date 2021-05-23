package com.easternsauce.libgdxgame.creatures

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

trait Creature extends Sprite with PhysicalBody with Animated {

  val id: String

  val creatureWidth: Float
  val creatureHeight: Float

  var currentDirection: EsDirection.Value = EsDirection.Down

  var isWalkAnimationActive = false
  val timeSinceMovedTimer: EsTimer = EsTimer()

  val initX: Float
  val initY: Float

  private var area: Option[Area] = None

  def posX: Float = b2Body.getPosition.x

  def posY: Float = b2Body.getPosition.y

  def initParams(mass: Float): Unit = {
    this.mass = mass
  }

  def update(): Unit = {

    if (isWalkAnimationActive) setRegion(walkAnimationFrame(currentDirection))
    else setRegion(standStillImage(currentDirection))

    setPosition(posX - getWidth / 2f, posY - getHeight / 2f)

    if (isWalkAnimationActive && timeSinceMovedTimer.time > 0.25f) isWalkAnimationActive = false
  }

  def moveInDirection(dirs: List[EsDirection.Value], velocity: Float): Unit = {

    timeSinceMovedTimer.restart()

    if (!isWalkAnimationActive) {
      animationTimer.restart()
      isWalkAnimationActive = true
    }

    val vector = new Vector2()

    currentDirection = dirs.last

    dirs.foreach {
      case EsDirection.Up    => vector.y += velocity
      case EsDirection.Down  => vector.y -= velocity
      case EsDirection.Left  => vector.x -= velocity
      case EsDirection.Right => vector.x += velocity
    }

    val horizontalCount = dirs.count(EsDirection.isHorizontal)
    val verticalCount = dirs.count(EsDirection.isVertical)

    if (horizontalCount < 2 && verticalCount < 2) {
      if (horizontalCount > 0 && verticalCount > 0) {
        sustainVelocity(new Vector2(vector.x / Math.sqrt(2).toFloat, vector.y / Math.sqrt(2).toFloat))
      }
      else {
        sustainVelocity(vector)
      }
    }
  }

  def assignToArea(area: Area): Unit = {
    if (this.area.isEmpty) {
      this.area = Some(area)
      initCircularBody(area.world, initX, initY, creatureWidth / 2f)

      area.creatureMap += (id -> this)
    }
    else {
      val oldArea = this.area.get

      oldArea.world.destroyBody(b2Body)
      oldArea.creatureMap -= id

      this.area = Some(area)
      initCircularBody(area.world, initX, initY, creatureWidth / 2f)

      area.creatureMap += (id -> this)
    }

  }
}
