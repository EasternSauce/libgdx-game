package com.easternsauce.libgdxgame.creatures

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.util.EsDirection

trait Creature extends Sprite with PhysicalBody with Animated {

  val creatureWidth = 2f
  val creatureHeight = 2f

  var currentDirection: EsDirection.Value = EsDirection.Down

  def posX: Float = b2Body.getPosition.x

  def posY: Float = b2Body.getPosition.y

  def update(): Unit = {
    setRegion(walkAnimationFrame(currentDirection))
    setPosition(posX - getWidth / 2f, posY - getHeight / 2f)
  }

  def moveInDirection(dirs: List[EsDirection.Value], velocity: Float): Unit = {

    val vector = new Vector2()

    var impulseForce = 1000f

    currentDirection = dirs.last

    dirs.foreach {
      case EsDirection.Up => vector.y += impulseForce
      case EsDirection.Down => vector.y -= impulseForce
      case EsDirection.Left => vector.x -= impulseForce
      case EsDirection.Right => vector.x += impulseForce
    }

    sustainVelocity(vector, velocity)

  }

}
