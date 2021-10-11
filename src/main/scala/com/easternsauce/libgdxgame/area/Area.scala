package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.{TiledMapTileLayer, TmxMapLoader}
import com.easternsauce.libgdxgame.area.traits._
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.items.Item
import com.easternsauce.libgdxgame.pathfinding.AStarNode
import com.easternsauce.libgdxgame.projectile.Arrow
import com.easternsauce.libgdxgame.spawns.EnemySpawnPoint
import com.easternsauce.libgdxgame.system.GameSystem._
import com.easternsauce.libgdxgame.system.{Constants, InventoryMapping}
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable.ListBuffer

class Area(val mapLoader: TmxMapLoader, val areaFilesLocation: String, val id: String, val mapScale: Float)
    extends CollisionDetection
    with EnemySpawns
    with PlayerSpawns
    with PhysicalTerrain
    with TiledGrid
    with LootManagement {

  val arrowList: ListBuffer[Arrow] = ListBuffer()

  val aStarNodeList: ListBuffer[AStarNode] = ListBuffer()

  var music: Option[Music] = None

  initPhysicalTerrain()

  createContactListener()

  loadEnemySpawns()
  loadPLayerSpawns()
  loadTreasures()

  setupPathfindingGraph()

  def renderBottomLayer(): Unit = tiledMapRenderer.render(Array(0, 1))

  def renderTopLayer(): Unit = tiledMapRenderer.render(Array(2, 3))

  def creaturesMap: List[Creature] =
    globalCreaturesMap.values.filter(creature => creature.areaId.nonEmpty && creature.areaId.get == id).toList

  def update(): Unit = {

    world.step(Math.min(Gdx.graphics.getDeltaTime, 0.15f), 6, 2)

    creaturesMap.foreach(_.update())

    val toBeDeleted = ListBuffer[Arrow]()
    for (arrow <- arrowList) {
      arrow.update()
      if (arrow.markedForDeletion) {
        toBeDeleted += arrow
        arrow.destroyBody()
      }
    }

    playerSpawns.foreach(_.update())

    arrowList.filterInPlace(!toBeDeleted.contains(_))

    updateLoot()

  }

  def renderLootPiles(batch: EsBatch): Unit = {
    lootPileList.foreach(_.draw(batch.spriteBatch))
  }

  def renderAiDebug(batch: EsBatch): Unit = {
    if (debugMode) {

      for (creature <- creaturesMap.filter(_.isEnemy)) {
        val enemy = creature.asInstanceOf[Enemy]

        // render debug
        enemy.path.foreach(node => {
          batch.shapeDrawer.setColor(Color.RED)
          val area = areaMap(enemy.areaId.get)
          val pos = area.getTileCenter(node.x, node.y)
          batch.shapeDrawer.filledCircle(pos.x, pos.y, 0.1f)
        })

        if (enemy.lineOfSight.nonEmpty) {

          if (enemy.targetVisible) batch.shapeDrawer.setColor(Color.BLUE)
          else batch.shapeDrawer.setColor(Color.RED)

          batch.shapeDrawer.setDefaultLineWidth(0.05f)
          batch.shapeDrawer.filledPolygon(enemy.lineOfSight.get)
        }
      }
    }
  }

  def renderPlayerSpawns(batch: EsBatch): Unit = {
    playerSpawns.foreach(_.draw(batch.spriteBatch))
  }

  def renderBossArenaBlockades(batch: EsBatch): Unit = {
    bossArenaBlockades.foreach(_.draw(batch.spriteBatch))
  }

  def renderCreatureLifeBars(batch: EsBatch): Unit = {
    for (creature <- creaturesMap) {
      if (creature.isAlive && !creature.atFullLife)
        creature.renderLifeBar(batch)
    }
  }

  def renderCreatureAbilities(batch: EsBatch): Unit = {
    for (creature <- creaturesMap) {
      creature.renderAbilities(batch)
    }
  }

  def renderAliveCreatures(batch: EsBatch): Unit = {
    creaturesMap.filter(_.isAlive).foreach(_.render(batch))
  }

  def renderDeadCreatures(batch: EsBatch): Unit = {
    creaturesMap.filter(!_.isAlive).foreach(_.render(batch))
  }

  def setView(camera: OrthographicCamera): Unit = {
    tiledMapRenderer.setView(camera)
  }

  def dispose(): Unit = {
    world.dispose()
    tiledMapRenderer.dispose()
    map.dispose()
  }

  def reset(): Unit = {
    creaturesMap
      .filter(creature => creature.isEnemy)
      .foreach(creature => creature.destroyBody(areaMap(creature.areaId.get).world))
    globalCreaturesMap.filterInPlace { case (_, creature) => !(creature.isEnemy && creature.areaId.get == id) }
    enemySpawns.foreach(spawnPoint => spawnEnemy(spawnPoint))
    arrowList.clear()

    lootPileList.foreach(_.destroyBody(world))
    lootPileList.clear()

    lootPileList.addAll(
      treasuresList.filterNot(treasure => treasureLootedList.contains((this.id, treasure.treasureId.get)))
    )
  }

  private def spawnEnemy(spawnPoint: EnemySpawnPoint): Unit = {
    val indexOfDot = spawnPoint.creatureClass.lastIndexOf('.')
    val creatureId = spawnPoint.creatureClass.substring(indexOfDot + 1) + "_" + Math.abs(randomGenerator.nextInt())

    val action = Class
      .forName(spawnPoint.creatureClass)
      .getMethod("apply", classOf[String])
      .invoke(null, creatureId)

    val creature = action.asInstanceOf[Creature]

    creature.spawnPointId = Some(spawnPoint.id)

    creature.assignToArea(id, spawnPoint.posX, spawnPoint.posY)

    if (spawnPoint.weaponType.nonEmpty) {
      creature.equipmentItems(InventoryMapping.primaryWeaponIndex) =
        Item.generateFromTemplate(spawnPoint.weaponType.get)
    }

    globalCreaturesMap += (creatureId -> creature)
  }

  def width: Float = {
    val layer = map.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
    layer.getWidth * Constants.TiledMapCellSize
  }

  def height: Float = {
    val layer = map.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
    layer.getHeight * Constants.TiledMapCellSize
  }

  def resetPathfindingGraph(): Unit = {
    aStarNodeList.foreach(node => {
      node.f = Double.MaxValue
      node.g = Double.MaxValue
      node.parent = None
    })

  }

  private def setupPathfindingGraph(): Unit = {
    aStarNodes = Array.ofDim[AStarNode](heightInTiles, widthInTiles)
    for {
      x <- 0 until widthInTiles
      y <- 0 until heightInTiles
    } aStarNodes(y)(x) = new AStarNode(x, y, "(" + x + "," + y + ")")

    def tryAddingEdge(node: AStarNode, x: Int, y: Int, weight: Int): Unit = {
      if (0 <= y && y < heightInTiles && 0 <= x && x < widthInTiles) {
        if (traversable(y)(x)) {
          val targetNode = aStarNodes(y)(x)
          node.addEdge(weight, targetNode)
          aStarNodeList += node
        }
      }
    }

    for {
      x <- 0 until widthInTiles
      y <- 0 until heightInTiles
    } {
      tryAddingEdge(aStarNodes(y)(x), x - 1, y, 10) // left
      tryAddingEdge(aStarNodes(y)(x), x + 1, y, 10) // right
      tryAddingEdge(aStarNodes(y)(x), x, y - 1, 10) // bottom
      tryAddingEdge(aStarNodes(y)(x), x, y + 1, 10) // top
      if (
        x - 1 >= 0 && y - 1 >= 0
        && traversable(y)(x - 1) && traversable(y - 1)(x)
      ) tryAddingEdge(aStarNodes(y)(x), x - 1, y - 1, 14)
      if (
        x + 1 < widthInTiles && y - 1 >= 0
        && traversable(y)(x + 1) && traversable(y - 1)(x)
      ) tryAddingEdge(aStarNodes(y)(x), x + 1, y - 1, 14)
      if (
        x - 1 >= 0 && y + 1 < heightInTiles
        && traversable(y)(x - 1) && traversable(y + 1)(x)
      ) tryAddingEdge(aStarNodes(y)(x), x - 1, y + 1, 14)
      if (
        x + 1 < widthInTiles && y + 1 < heightInTiles
        && traversable(y)(x + 1) && traversable(y + 1)(x)
      ) tryAddingEdge(aStarNodes(y)(x), x + 1, y + 1, 14)

    }
  }

}
