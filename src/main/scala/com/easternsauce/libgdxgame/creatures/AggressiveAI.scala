package com.easternsauce.libgdxgame.creatures

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.util.EsDirection

import scala.collection.mutable.ListBuffer

trait AggressiveAI extends Creature {

  var aggroedOn: Option[Creature] = None
  val aggroDistance = 15f

  def targetFound: Boolean = aggroedOn.nonEmpty

  def lookForTarget(): Unit = {
    if (alive && !targetFound) {
      area.get.creatureMap.values.filter(creature => !creature.isEnemy)
        .foreach(creature => {
          if (distanceTo(creature) < aggroDistance) {
            aggroedOn = Some(creature)
          }
        })
    }
  }

  def walkAndAttack(): Unit = {

  }

  def walkTo(destination: Vector2): Unit = {
    val dirs: ListBuffer[EsDirection.Value] = ListBuffer()


    if (pos.x < destination.x - 0.1f) dirs += EsDirection.Right
    if (pos.x > destination.x + 0.1f) dirs += EsDirection.Left
    if (pos.y > destination.y + 0.1f) dirs += EsDirection.Down
    if (pos.y < destination.y - 0.1f) dirs += EsDirection.Up

    val horizontalDistance = Math.abs(pos.x - destination.x)
    val verticalDistance = Math.abs(pos.y - destination.y)

    println(horizontalDistance + " "+ verticalDistance + " list; " + dirs)

    if (horizontalDistance > verticalDistance + 2f) {
      moveInDirection(dirs.filter(EsDirection.isHorizontal).toList)
    }
    else if (verticalDistance > horizontalDistance + 2f) {
      moveInDirection(dirs.filter(EsDirection.isVertical).toList)
    }
    else {
      moveInDirection(dirs.toList)
    }

  }
}
