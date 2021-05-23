package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.easternsauce.libgdxgame.LibgdxGame

class Area(mapLoader: TmxMapLoader, fileName: String, val id: String, mapScale: Float) {
  private val map: TiledMap = mapLoader.load(fileName)

  private val tiledMapRenderer: OrthogonalTiledMapRenderer =
    new OrthogonalTiledMapRenderer(map, mapScale / LibgdxGame.PPM)

  val world: World = new World(new Vector2(0, 0), true)


  def render(): Unit = {
    tiledMapRenderer.render()
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
