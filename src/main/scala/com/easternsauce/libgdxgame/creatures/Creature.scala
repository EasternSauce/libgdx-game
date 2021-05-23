package com.easternsauce.libgdxgame.creatures

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

trait Creature extends Sprite with PhysicalBody with Animated {

  val creatureWidth: Float
  val creatureHeight: Float

  var currentDirection: EsDirection.Value = EsDirection.Down

  var isWalkAnimationActive = false
  val timeSinceMovedTimer: EsTimer = EsTimer()

  val area: Area

  def posX: Float = b2Body.getPosition.x

  def posY: Float = b2Body.getPosition.y

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

    var impulseForce = 1000f

    currentDirection = dirs.last

    dirs.foreach {
      case EsDirection.Up    => vector.y += impulseForce
      case EsDirection.Down  => vector.y -= impulseForce
      case EsDirection.Left  => vector.x -= impulseForce
      case EsDirection.Right => vector.x += impulseForce
    }

    sustainVelocity(vector, velocity)

  }

}
