package com.easternsauce.libgdxgame.area.traits

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.easternsauce.libgdxgame.LibgdxGame
import com.easternsauce.libgdxgame.area.AreaTile

import scala.collection.mutable

trait TiledGrid {
  val tiles: mutable.Map[(Int, Int, Int), AreaTile] = mutable.Map()
  val mapLoader: TmxMapLoader

  val fileName: String
  val mapScale: Float

  protected val map: TiledMap = mapLoader.load(fileName)

  protected val tiledMapRenderer: OrthogonalTiledMapRenderer =
    new OrthogonalTiledMapRenderer(map, mapScale / LibgdxGame.PPM)

}
