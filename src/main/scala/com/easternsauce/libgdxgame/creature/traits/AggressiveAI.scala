package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.pathfinding.{AStar, AStarNode}
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

import scala.collection.mutable.ListBuffer

trait AggressiveAI {

  var aggroedTarget: Option[Creature] = None
  val aggroDistance = 20f
  val minimumWalkUpDistance = 4f
  val circleDistance = 15f
  val attackDistance = 6f
  val aggroDropDistance = 25f

  var circling = false
  val circlingDecisionTimer: EsTimer = EsTimer()
  val circlingDecisionMaxTime = 0.2f
  var circlingClockwise = true

  val recalculatePathTimer: EsTimer = EsTimer()

  var goToSpawnTime: Float = _

  var path: ListBuffer[AStarNode] = ListBuffer()

  def targetFound: Boolean = aggroedTarget.nonEmpty

  def lookForTarget(creature: Creature): Unit = {
    if (creature.alive && !targetFound) {
      creature.area.get.creaturesMap.values
        .filter(creature => !creature.isEnemy)
        .foreach(otherCreature => {
          if (otherCreature.isAlive && creature.distanceTo(otherCreature) < aggroDistance) {
            aggroOnCreature(creature, otherCreature)
          }
        })
    }
  }

  def aggroOnCreature(creature: Creature, otherCreature: Creature): Unit = {
    aggroedTarget = Some(otherCreature)
    circlingDecisionTimer.restart()
    recalculatePathTimer.restart()
    calculatePath(creature.area.get, creature, aggroedTarget.get.pos)
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
        if (recalculatePathTimer.time > 1.5f) {
          calculatePath(creature.area.get, creature, aggroedTarget.get.pos)
          recalculatePathTimer.restart()
        }

        if (circling && creature.distanceTo(aggroedTarget.get) < circleDistance) {
          circleTarget(creature, aggroedTarget.get.pos)
        } else if (creature.distanceTo(aggroedTarget.get) > minimumWalkUpDistance) {

          if (path.nonEmpty && path.size > 3) {
            val destination = creature.area.get.getTileCenter(path.head.x, path.head.y)
            if (destination.dst(creature.pos) < 2f) path.dropInPlace(1)
            walkToTarget(creature, destination)
          } else {
            walkToTarget(creature, aggroedTarget.get.pos)
          }

        }
        if (creature.distanceTo(aggroedTarget.get) < attackDistance) {
          creature.currentAttack.perform()
        }

        if (!aggroedTarget.get.isAlive || path.size > 10) {
          dropAggro()
        }
      } else {
        if (recalculatePathTimer.time > goToSpawnTime) {
          calculatePath(creature.area.get, creature, creature.spawnPosition)
          recalculatePathTimer.restart()
          recalculatePathTimer.stop()
        }
        if (path.nonEmpty) {
          val destination = creature.area.get.getTileCenter(path.head.x, path.head.y)
          if (destination.dst(creature.pos) < 2f) {
            path.dropInPlace(1)
          }
          walkToTarget(creature, destination)
        }
      }
    }

  }

  private def dropAggro(): Unit = {
    aggroedTarget = None
    goToSpawnTime = 7 * RpgGame.Random.nextFloat()
    recalculatePathTimer.restart()
  }

  def calculatePath(area: Area, creature: Creature, target: Vector2): Unit = {
    area.resetPathfindingGraph()

    val start: Vector2 = area.getClosestTile(creature.pos.x, creature.pos.y)
    val end: Vector2 = area.getClosestTile(target.x, target.y)

    val node = AStar.aStar(area.aStarNodes(start.y.toInt)(start.x.toInt), area.aStarNodes(end.y.toInt)(end.x.toInt))
    path = ListBuffer().addAll(AStar.getPath(node))

  }

}
