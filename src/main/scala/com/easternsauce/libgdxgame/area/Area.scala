package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.easternsauce.libgdxgame.LibgdxGame
import com.easternsauce.libgdxgame.creatures.Creature

import scala.collection.mutable

class Area(mapLoader: TmxMapLoader, fileName: String, val id: String, mapScale: Float) {
  private val map: TiledMap = mapLoader.load(fileName)

  private val tiledMapRenderer: OrthogonalTiledMapRenderer =
    new OrthogonalTiledMapRenderer(map, mapScale / LibgdxGame.PPM)

  val world: World = new World(new Vector2(0, 0), true)

  val creatureMap: mutable.Map[String, Creature] = mutable.Map()


  def render(batch: SpriteBatch): Unit = {
    tiledMapRenderer.render()

    creatureMap.values.foreach(_.draw(batch))
  }

  def setView(camera: OrthographicCamera): Unit = {
    tiledMapRenderer.setView(camera)
  }

  def dispose(): Unit = {
    world.dispose()
    tiledMapRenderer.dispose()
    map.dispose()
  }


}
