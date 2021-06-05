package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

import scala.collection.mutable.ListBuffer

trait AggressiveAI {

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

  def lookForTarget(creature: Creature): Unit = {
    if (creature.alive && !targetFound) {
      creature.area.get.creatureMap.values
        .filter(creature => !creature.isEnemy)
        .foreach(otherCreature => {
          if (otherCreature.isAlive && creature.distanceTo(otherCreature) < aggroDistance) {
            aggroedTarget = Some(otherCreature)
            circlingDecisionTimer.restart()
          }
        })
    }
  }

  def decideIfCircling(creature: Creature): Unit = {
    if (creature.isAttacking) {
      circling = false
    } else {
      if (circlingDecisionTimer.time > circlingDecisionMaxTime) {
        circlingDecisionTimer.restart()
        if (RpgGame.Random.nextFloat() < 0.25f) {
          circling = true
          circlingClockwise = RpgGame.Random.nextFloat() < 0.5f
        } else {
          circling = false
        }
      }
    }

  }

  def walkToTarget(creature: Creature, destination: Vector2): Unit = {
    val dirs: ListBuffer[EsDirection.Value] = ListBuffer()

    if (creature.pos.x < destination.x - 0.1f) dirs += EsDirection.Right
    if (creature.pos.x > destination.x + 0.1f) dirs += EsDirection.Left
    if (creature.pos.y > destination.y + 0.1f) dirs += EsDirection.Down
    if (creature.pos.y < destination.y - 0.1f) dirs += EsDirection.Up

    val horizontalDistance = Math.abs(creature.pos.x - destination.x)
    val verticalDistance = Math.abs(creature.pos.y - destination.y)

    if (horizontalDistance > verticalDistance + 2f) {
      creature.moveInDirection(dirs.filter(EsDirection.isHorizontal).toList)
    } else if (verticalDistance > horizontalDistance + 2f) {
      creature.moveInDirection(dirs.filter(EsDirection.isVertical).toList)
    } else {
      creature.moveInDirection(dirs.toList)
    }

  }

  def circleTarget(creature: Creature, destination: Vector2): Unit = {

    val vector = new Vector2(destination.x - creature.pos.x, destination.y - creature.pos.y)

    val perpendicularDestination =
      if (circlingClockwise) new Vector2(creature.pos.x - vector.y, creature.pos.y + vector.x)
      else new Vector2(creature.pos.x + vector.y, creature.pos.y - vector.x)

    walkToTarget(creature, perpendicularDestination)
  }

  def searchForAndAttackTargets(creature: Creature): Unit = {
    if (creature.isAlive) {
      lookForTarget(creature)

      decideIfCircling(creature)

      if (targetFound) {
        if (circling && creature.distanceTo(aggroedTarget.get) < circleDistance) {
          circleTarget(creature, aggroedTarget.get.pos)
        } else if (creature.distanceTo(aggroedTarget.get) > minimumWalkUpDistance) {
          walkToTarget(creature, aggroedTarget.get.pos)
        }
        if (creature.distanceTo(aggroedTarget.get) < attackDistance) {
          creature.currentAttack.perform()
        }

        if (!aggroedTarget.get.isAlive) {
          aggroedTarget = None
        }
      }
    }

  }

}
