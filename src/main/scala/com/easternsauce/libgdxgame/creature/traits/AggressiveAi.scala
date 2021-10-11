package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.math.{Intersector, Polygon, Vector2}
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.pathfinding.{AStar, AStarNode}
import com.easternsauce.libgdxgame.system.GameSystem
import com.easternsauce.libgdxgame.system.GameSystem._
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

import scala.collection.mutable.ListBuffer

trait AggressiveAi {
  this: Enemy =>

  var aggroedTarget: Option[Creature] = None
  val aggroDistance = 20f

  val walkUpDistance = 15f

  val aggroDropDistance = 25f

  var circling = false
  val circlingDecisionTimer: EsTimer = EsTimer()
  val circlingDecisionMaxTime = 0.2f
  var circlingClockwise = true

  val aggroRecalculatePathTimer: EsTimer = EsTimer(true)
  val recalculatePathTimer: EsTimer = EsTimer(true)

  var goToSpawnTime: Float = _

  var path: ListBuffer[AStarNode] = ListBuffer()

  var lineOfSight: Option[Polygon] = None

  var targetVisible = false

  val useAbilityTimer: EsTimer = EsTimer()
  var useAbilityTimeout: Float = 1f + 3f * GameSystem.randomGenerator.nextFloat()

  def targetFound: Boolean = aggroedTarget.nonEmpty

  def attackDistance: Float =
    if (isWeaponEquipped) {
      currentWeapon.template.attackType match { // TODO change for this to work per weapon
        case Some("slash")       => 4f
        case Some("shoot_arrow") => 35f
        case Some("thrust")      => 6f
        case _                   => throw new RuntimeException("Unrecognized attack type")
      }
    } else {
      6f
    }

  val abilityUsages: Map[String, AbilityUsage] = Map()

  def lookForTarget(): Unit = {
    if (isAlive && !targetFound) {
      area.get.creaturesMap.values
        .filter(creature => !creature.isEnemy)
        .foreach(otherCreature => {
          if (otherCreature.isAlive && distanceTo(otherCreature) < aggroDistance) {

            if (aggroRecalculatePathTimer.time > 0.3f) {

              calculateLineOfSight(otherCreature)

              calculatePath(area.get, otherCreature.pos)

              if (path.length < 20) {
                aggroOnCreature(otherCreature)
              }

              aggroRecalculatePathTimer.restart()
            }

          }
        })
    }
  }

  def calculateLineOfSight(otherCreature: Creature): Unit = {

    val lineWidth = 0.5f
    lineOfSight = Some(
      new Polygon(
        Array(
          pos.x,
          pos.y,
          pos.x + lineWidth,
          pos.y + lineWidth,
          otherCreature.pos.x + lineWidth,
          otherCreature.pos.y + lineWidth,
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
    useAbilityTimer.restart()
    activeSoundTimer.restart()

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
    if (isAlive) {
      lookForTarget()

      decideIfCircling()

      if (targetFound) {
        if (recalculatePathTimer.time > 0.3f) {
          calculateLineOfSight(aggroedTarget.get)
          calculatePath(area.get, aggroedTarget.get.pos)
          recalculatePathTimer.restart()
        }

        if (useAbilityTimer.time > useAbilityTimeout) {
          if (abilityUsages.nonEmpty) {
            val pickedAbility = pickAbilityToUse()

            if (pickedAbility.nonEmpty) {
              // TODO: temp workaround
              val currentAttack = abilityMap(pickedAbility.get.id)

              abilityMap.update(pickedAbility.get.id, currentAttack.perform(this))
            }

          }

          useAbilityTimeout = 1f + 2f * GameSystem.randomGenerator.nextFloat()
          useAbilityTimer.restart()
        }

        if (targetVisible && distanceTo(aggroedTarget.get) < walkUpDistance) {
          if (circling && distanceTo(aggroedTarget.get) < attackDistance) {
            circleTarget(aggroedTarget.get.pos.cpy().add(perpendicularNoise(0.7f)))
          } else if (distanceTo(aggroedTarget.get) > attackDistance) {
            walkToTarget(aggroedTarget.get.pos.cpy().add(perpendicularNoise(0.7f)))
          }
        } else {
          if (path.nonEmpty) {
            val destination = area.get.getTileCenter(path.head.x, path.head.y)
            if (destination.dst(pos) < 1.5f) path.dropInPlace(1)
            walkToTarget(destination.add(perpendicularNoise(0.7f)))
          } else if (distanceTo(aggroedTarget.get) < walkUpDistance) {
            walkToTarget(aggroedTarget.get.pos)
          }
        }

        if (targetVisible && distanceTo(aggroedTarget.get) < attackDistance) {
          // TODO: temp workaround
          val thisCurrentAttack = abilityMap(currentAttack.id)
          abilityMap.update(thisCurrentAttack.id, thisCurrentAttack.perform(this))
        }

        if (!aggroedTarget.get.isAlive || (path.size > 15 && !isBoss)) {
          dropAggro()
        }
      } else {
        if (recalculatePathTimer.time > goToSpawnTime) {
          calculatePath(area.get, spawnPosition)
          recalculatePathTimer.restart()
          recalculatePathTimer.stop()
        }
        if (path.nonEmpty) {
          val destination = area.get.getTileCenter(path.head.x, path.head.y)
          if (destination.dst(pos) < 1.5f) {
            path.dropInPlace(1)
          }
          walkToTarget(destination)
        }
      }
    }

  }

  private def perpendicularNoise(noiseStrength: Float) = {
    if (GameSystem.randomGenerator.nextFloat() < 0.5f) {
      new Vector2(
        -facingVector.y * GameSystem.randomGenerator.nextFloat() * noiseStrength,
        facingVector.x * GameSystem.randomGenerator.nextFloat() * noiseStrength
      )
    } else {
      new Vector2(
        facingVector.y * GameSystem.randomGenerator.nextFloat() * noiseStrength,
        -facingVector.x * GameSystem.randomGenerator.nextFloat() * noiseStrength
      )
    }
  }

  private def dropAggro(): Unit = {
    aggroedTarget = None
    goToSpawnTime = 7 * randomGenerator.nextFloat()
    recalculatePathTimer.restart()
  }

  def calculatePath(area: Area, target: Vector2): Unit = {
    area.resetPathfindingGraph()

    val start: Vector2 = area.getClosestTile(pos.x, pos.y)
    val end: Vector2 = area.getClosestTile(target.x, target.y)

    val node = AStar.aStar(area.aStarNodes(start.y.toInt)(start.x.toInt), area.aStarNodes(end.y.toInt)(end.x.toInt))
    path = ListBuffer().addAll(AStar.getPath(node))

  }

  def pickAbilityToUse(): Option[Ability] = {

    val filteredAbilityUsages = abilityUsages.filter {
      case (abilityId, usage) =>
        life / maxLife <= usage.lifeThreshold && pos.dst(aggroedTarget.get.pos) > usage.minimumDistance && pos.dst(
          aggroedTarget.get.pos
        ) < usage.maximumDistance && !abilityMap(abilityId).onCooldown
    }

    var completeWeight = 0.0f
    for (abilityUsage <- filteredAbilityUsages.values) {
      completeWeight += abilityUsage.weight
    }
    val r = Math.random * completeWeight
    var countWeight = 0.0
    for (abilityUsage <- filteredAbilityUsages) {
      val (key, value) = abilityUsage
      countWeight += value.weight
      if (countWeight > r) return Some(abilityMap(key))
    }
    None
  }

}

case class AbilityUsage(
  weight: Float,
  minimumDistance: Float = 0f,
  maximumDistance: Float = 999f,
  lifeThreshold: Float = 1.0f
)
