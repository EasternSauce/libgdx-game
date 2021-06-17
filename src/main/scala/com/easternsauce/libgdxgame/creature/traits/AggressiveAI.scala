package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.math.{Intersector, Polygon, Vector2}
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.pathfinding.{AStar, AStarNode}
import com.easternsauce.libgdxgame.system.GameSystem._
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

import scala.collection.mutable.ListBuffer

trait AggressiveAI {
  this: Enemy =>

  var aggroedTarget: Option[Creature] = None
  val aggroDistance = 20f

  val walkUpDistance = 10f

  val circleDistance = 10f

  val aggroDropDistance = 25f

  var circling = false
  val circlingDecisionTimer: EsTimer = EsTimer()
  val circlingDecisionMaxTime = 0.2f
  var circlingClockwise = true

  val aggroRecalculatePathTimer: EsTimer = EsTimer(true)
  val recalculatePathTimer: EsTimer = EsTimer(true)

  var goToSpawnTime: Float = _

  var pathWithMargins: ListBuffer[AStarNode] = ListBuffer()

  var path: ListBuffer[AStarNode] = ListBuffer()

  var lineOfSight: Option[Polygon] = None

  var targetVisible = false

  def targetFound: Boolean = aggroedTarget.nonEmpty

  def attackDistance: Float =
    if (isWeaponEquipped) {
      currentWeapon.template.attackType match {
        case Some("sword")   => 6f
        case Some("bow")     => 35f
        case Some("trident") => 6f
        case _               => throw new RuntimeException("Unrecognized attack type")
      }
    } else {
      6f
    }

  def lookForTarget(): Unit = {
    if (alive && !targetFound) {
      area.get.creaturesMap.values
        .filter(creature => !creature.isEnemy)
        .foreach(otherCreature => {
          if (otherCreature.alive && distanceTo(otherCreature) < aggroDistance) {

            if (aggroRecalculatePathTimer.time > 0.3f) {

              calculateLineOfSight(otherCreature)

              calculatePath(area.get, otherCreature.pos)

              if (pathWithMargins.length < 20 || path.length < 20) {
                aggroOnCreature(otherCreature)
              }
              pathWithMargins.clear()
              path.clear()
              aggroRecalculatePathTimer.restart()
            }
          }
        })
    }
  }

  def calculateLineOfSight(otherCreature: Creature): Unit = {
    lineOfSight = Some(
      new Polygon(
        Array(
          pos.x,
          pos.y,
          pos.x,
          pos.y + 1f,
          otherCreature.pos.x,
          otherCreature.pos.y + 1f,
          otherCreature.pos.x,
          otherCreature.pos.y
        )
      )
    )

    targetVisible = currentArea.get.terrainTiles
      .map(tile => tile.polygon)
      .forall(!Intersector.overlapConvexPolygons(_, lineOfSight.get))

  }

  def aggroOnCreature(otherCreature: Creature): Unit = {
    aggroedTarget = Some(otherCreature)
    circlingDecisionTimer.restart()
    recalculatePathTimer.restart()
    calculatePath(area.get, aggroedTarget.get.pos)

  }

  def decideIfCircling(): Unit = {
    if (isAttacking) {
      circling = false
    } else {
      if (circlingDecisionTimer.time > circlingDecisionMaxTime) {
        circlingDecisionTimer.restart()
        if (randomGenerator.nextFloat() < 0.25f) {
          circling = true
          circlingClockwise = randomGenerator.nextFloat() < 0.5f
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
    if (alive) {
      lookForTarget()

      decideIfCircling()

      if (targetFound) {
        if (recalculatePathTimer.time > 0.3f) {
          calculateLineOfSight(aggroedTarget.get)
          calculatePath(area.get, aggroedTarget.get.pos)
          recalculatePathTimer.restart()
        }

        if (targetVisible && distanceTo(aggroedTarget.get) < walkUpDistance) {
          if (circling && distanceTo(aggroedTarget.get) < circleDistance) {
            circleTarget(aggroedTarget.get.pos)
          } else if (distanceTo(aggroedTarget.get) > attackDistance) {
            walkToTarget(aggroedTarget.get.pos)
          }
        } else {
          if (pathWithMargins.nonEmpty) {
            val destination = area.get.getTileCenter(pathWithMargins.head.x, pathWithMargins.head.y)
            if (destination.dst(pos) < 1f) pathWithMargins.dropInPlace(1)
            walkToTarget(destination)
          } else if (path.nonEmpty) {
            val destination = area.get.getTileCenter(path.head.x, path.head.y)
            if (destination.dst(pos) < 1f) path.dropInPlace(1)
            walkToTarget(destination)
          } else if (distanceTo(aggroedTarget.get) < walkUpDistance) {
            walkToTarget(aggroedTarget.get.pos)
          }
        }

        if (targetVisible && distanceTo(aggroedTarget.get) < attackDistance) {
          currentAttack.perform()
        }

        if (!aggroedTarget.get.alive || pathWithMargins.size > 15 || path.size > 15) {
          dropAggro()
        }
      } else {
        if (recalculatePathTimer.time > goToSpawnTime) {
          calculatePath(area.get, spawnPosition)
          recalculatePathTimer.restart()
          recalculatePathTimer.stop()
        }
        if (pathWithMargins.nonEmpty) {
          val destination = area.get.getTileCenter(pathWithMargins.head.x, pathWithMargins.head.y)
          if (destination.dst(pos) < 2f) {
            pathWithMargins.dropInPlace(1)
          }
          walkToTarget(destination)
        } else if (path.nonEmpty) {
          val destination = area.get.getTileCenter(path.head.x, path.head.y)
          if (destination.dst(pos) < 2f) {
            path.dropInPlace(1)
          }
          walkToTarget(destination)
        }
      }
    }

  }

  private def dropAggro(): Unit = {
    aggroedTarget = None
    goToSpawnTime = 7 * randomGenerator.nextFloat()
    recalculatePathTimer.restart()
  }

  def calculatePath(area: Area, target: Vector2): Unit = {
    area.resetPathfindingGraphs()

    val start: Vector2 = area.getClosestTile(pos.x, pos.y)
    val end: Vector2 = area.getClosestTile(target.x, target.y)

    val node = AStar.aStar(area.aStarNodes(start.y.toInt)(start.x.toInt), area.aStarNodes(end.y.toInt)(end.x.toInt))
    path = ListBuffer().addAll(AStar.getPath(node))

    val nodeWithMargins = AStar.aStar(
      area.aStarNodesWithMargins(start.y.toInt)(start.x.toInt),
      area.aStarNodesWithMargins(end.y.toInt)(end.x.toInt)
    )
    pathWithMargins = ListBuffer().addAll(AStar.getPath(nodeWithMargins))

  }

}
