package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.LibgdxGame
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

import scala.collection.mutable.ListBuffer

trait AggressiveAI extends Creature {

  var aggroedTarget: Option[Creature] = None
  val aggroDistance = 20f
  val minimumWalkUpDistance = 4f
  val circleDistance = 15f
  val attackDistance = 6f

  var circling = false
  val circlingDecisionTimer: EsTimer = EsTimer()
  val circlingDecisionMaxTime = 0.2f
  var circlingClockwise = true

  def targetFound: Boolean = aggroedTarget.nonEmpty

  def lookForTarget(): Unit = {
    if (alive && !targetFound) {
      area.get.creatureMap.values
        .filter(creature => !creature.isEnemy)
        .foreach(creature => {
          if (creature.isAlive && distanceTo(creature) < aggroDistance) {
            aggroedTarget = Some(creature)
            circlingDecisionTimer.restart()
          }
        })
    }
  }

  def decideIfCircling(): Unit = {
    if (isAttacking) {
      circling = false
    } else {
      if (circlingDecisionTimer.time > circlingDecisionMaxTime) {
        circlingDecisionTimer.restart()
        if (LibgdxGame.Random.nextFloat() < 0.25f) {
          circling = true
          circlingClockwise = LibgdxGame.Random.nextFloat() < 0.5f
        } else {
          circling = false
        }
      }
    }

  }

  def walkToTarget(destination: Vector2): Unit = {
    val dirs: ListBuffer[EsDirection.Value] = ListBuffer()

    if (pos.x < destination.x - 0.1f) dirs += EsDirection.Right
    if (pos.x > destination.x + 0.1f) dirs += EsDirection.Left
    if (pos.y > destination.y + 0.1f) dirs += EsDirection.Down
    if (pos.y < destination.y - 0.1f) dirs += EsDirection.Up

    val horizontalDistance = Math.abs(pos.x - destination.x)
    val verticalDistance = Math.abs(pos.y - destination.y)

    if (horizontalDistance > verticalDistance + 2f) {
      moveInDirection(dirs.filter(EsDirection.isHorizontal).toList)
    } else if (verticalDistance > horizontalDistance + 2f) {
      moveInDirection(dirs.filter(EsDirection.isVertical).toList)
    } else {
      moveInDirection(dirs.toList)
    }

  }

  def circleTarget(destination: Vector2): Unit = {

    val vector = new Vector2(destination.x - pos.x, destination.y - pos.y)

    val perpendicularDestination =
      if (circlingClockwise) new Vector2(pos.x - vector.y, pos.y + vector.x)
      else new Vector2(pos.x + vector.y, pos.y - vector.x)

    walkToTarget(perpendicularDestination)
  }

  def searchForAndAttackTargets(): Unit = {
    lookForTarget()

    decideIfCircling()

    if (targetFound) {
      if (circling && distanceTo(aggroedTarget.get) < circleDistance) {
        circleTarget(aggroedTarget.get.pos)
      } else if (distanceTo(aggroedTarget.get) > minimumWalkUpDistance) {
        walkToTarget(aggroedTarget.get.pos)
      }
      if (distanceTo(aggroedTarget.get) < attackDistance) {
        currentAttack.perform()
      }

      if (!aggroedTarget.get.isAlive) {
        aggroedTarget = None
      }
    }

  }

  override def update(): Unit = {
    super.update()

    searchForAndAttackTargets()
  }
}
