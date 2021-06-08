package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.{TiledMapTileLayer, TmxMapLoader}
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.area.traits.{CollisionDetection, EnemySpawns, PhysicalTerrain, TiledGrid}
import com.easternsauce.libgdxgame.creature.traits.{Creature, Enemy}
import com.easternsauce.libgdxgame.items.Item
import com.easternsauce.libgdxgame.projectile.Arrow
import com.easternsauce.libgdxgame.spawns.EnemySpawnPoint
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Area(
  val game: RpgGame,
  val mapLoader: TmxMapLoader,
  val areaFilesLocation: String,
  val id: String,
  val mapScale: Float
) extends CollisionDetection
    with EnemySpawns
    with PhysicalTerrain
    with TiledGrid {

  val creaturesMap: mutable.Map[String, Creature] = mutable.Map()

  val arrowList: mutable.ListBuffer[Arrow] = ListBuffer()

  initPhysicalTerrain(map, mapScale)

  createContactListener(world)

  loadEnemySpawns(this, areaFilesLocation)

  def renderBottomLayer(): Unit = tiledMapRenderer.render(Array(0, 1))

  def renderTopLayer(): Unit = tiledMapRenderer.render(Array(2, 3))

  def update(): Unit = {

    world.step(Math.min(Gdx.graphics.getDeltaTime, 0.15f), 6, 2)

    creaturesMap.values.foreach(_.update())

    val toBeDeleted = ListBuffer[Arrow]()
    for (arrow <- arrowList) {
      arrow.update()
      if (arrow.markedForDeletion) {
        toBeDeleted += arrow
        arrow.destroyBody()
      }
    }

    arrowList.filterInPlace(!toBeDeleted.contains(_))

  }

  def render(batch: EsBatch): Unit = {
    creaturesMap.values.filter(!_.isAlive).foreach(_.draw(batch.spriteBatch))
    creaturesMap.values.filter(_.isAlive).foreach(_.draw(batch.spriteBatch))

    for (creature <- creaturesMap.values) {
      creature.renderAbilities(batch)
    }

    for (creature <- creaturesMap.values) {
      if (creature.isAlive && !creature.atFullLife)
        creature.renderHealthBar(batch)
    }

    for (creature <- creaturesMap.values.filter(_.isEnemy)) {
      val enemy = creature.asInstanceOf[Enemy]

      // render debug path
      enemy.path.foreach(node => {
        batch.shapeDrawer.setColor(Color.RED)
        val pos = enemy.area.get.getTileCenter(node.x, node.y)
        batch.shapeDrawer.filledCircle(pos.x, pos.y, 0.1f)
      })

    }
  }

  def setView(camera: OrthographicCamera): Unit = {
    tiledMapRenderer.setView(camera)
  }

  def dispose(): Unit = {
    world.dispose()
    tiledMapRenderer.dispose()
    map.dispose()
  }

  def reset(game: RpgGame): Unit = {
    creaturesMap.values
      .filter(creature => creature.isEnemy)
      .foreach(creature => creature.destroyBody(creature.area.get.world))
    game.allAreaCreaturesMap.filterInPlace{case (_, creature) => !(creature.isEnemy && creature.area.get == this)}
    creaturesMap.filterInPlace { case (_, creature) => !creature.isEnemy }
    enemySpawns.foreach(spawnPoint => spawnEnemy(game, spawnPoint))
    arrowList.clear()
  }

  private def spawnEnemy(game: RpgGame, spawnPoint: EnemySpawnPoint): Unit = {
    val indexOfDot = spawnPoint.creatureClass.lastIndexOf('.')
    val creatureId = spawnPoint.creatureClass.substring(indexOfDot + 1) + "_" + Math.abs(RpgGame.Random.nextInt())

    val action = Class
      .forName(spawnPoint.creatureClass)
      .getDeclaredConstructor(classOf[RpgGame], classOf[String])
      .newInstance(game, creatureId)
    val creature = action.asInstanceOf[Creature]

    creature.spawnPointId = Some(spawnPoint.id)

    creature.assignToArea(this, spawnPoint.posX, spawnPoint.posY)

    if (spawnPoint.weaponType.nonEmpty) {
      creature.equipmentItems(RpgGame.equipmentTypeIndices("weapon")) =
        Item.generateFromTemplate(spawnPoint.weaponType.get)
    }

    game.allAreaCreaturesMap += (creatureId -> creature)
  }

  def width: Float = {
    val layer = map.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
    layer.getWidth * RpgGame.TiledMapCellSize
  }

  def height: Float = {
    val layer = map.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
    layer.getHeight * RpgGame.TiledMapCellSize
  }

}
