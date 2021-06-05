package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.{TiledMapTileLayer, TmxMapLoader}
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.area.traits.{CollisionDetection, EnemySpawns, PhysicalTerrain, TiledGrid}
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.projectile.Arrow
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Area(
  val screen: PlayScreen,
  val mapLoader: TmxMapLoader,
  val areaFilesLocation: String,
  val id: String,
  val mapScale: Float
) extends CollisionDetection
    with EnemySpawns
    with PhysicalTerrain
    with TiledGrid {

  val creatureMap: mutable.Map[String, Creature] = mutable.Map()

  val arrowList: mutable.ListBuffer[Arrow] = ListBuffer()

  initPhysicalTerrain(map, mapScale, tiles)

  createContactListener(world)

  loadEnemySpawns(areaFilesLocation)

  def renderBottomLayer(): Unit = tiledMapRenderer.render(Array(0, 1))

  def renderTopLayer(): Unit = tiledMapRenderer.render(Array(2))

  def update(): Unit = {

    world.step(Math.min(Gdx.graphics.getDeltaTime, 0.15f), 6, 2)

    creatureMap.values.foreach(_.update())

    val toBeDeleted = ListBuffer[Arrow]()
    for (arrow <- arrowList) {
      arrow.update()
      if (arrow.markedForDeletion) {
        toBeDeleted += arrow
        arrow.area.world.destroyBody(arrow.body)
      }
    }

    arrowList.filterInPlace(!toBeDeleted.contains(_))

  }

  def render(batch: EsBatch): Unit = {
    creatureMap.values.filter(!_.isAlive).foreach(_.draw(batch.spriteBatch))
    creatureMap.values.filter(_.isAlive).foreach(_.draw(batch.spriteBatch))

    for (creature <- creatureMap.values) {
      creature.renderAbilities(batch)
    }

    for (creature <- creatureMap.values) {
      if (creature.isAlive && !creature.atFullLife)
        creature.renderHealthBar(batch)
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

  def reset(): Unit = {
    // TODO
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
