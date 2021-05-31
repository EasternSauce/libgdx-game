package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.{TiledMapTileLayer, TmxMapLoader}
import com.easternsauce.libgdxgame.LibgdxGame
import com.easternsauce.libgdxgame.area.traits.CollisionDetection
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.projectile.Arrow
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Area(
  val screen: PlayScreen,
  val mapLoader: TmxMapLoader,
  val fileName: String,
  val id: String,
  val mapScale: Float
) extends CollisionDetection {

  val creatureMap: mutable.Map[String, Creature] = mutable.Map()

  val arrowList: mutable.ListBuffer[Arrow] = ListBuffer()

  initPhysicalTerrain()

  createContactListener()

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
    layer.getWidth * LibgdxGame.TiledMapCellSize
  }

  def height: Float = {
    val layer = map.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
    layer.getHeight * LibgdxGame.TiledMapCellSize
  }

}
