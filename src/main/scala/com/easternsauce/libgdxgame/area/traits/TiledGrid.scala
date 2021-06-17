package com.easternsauce.libgdxgame.area.traits

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.easternsauce.libgdxgame.area.{Area, TerrainTile}
import com.easternsauce.libgdxgame.system.Constants

import scala.collection.mutable.ListBuffer

trait TiledGrid {
  this: Area =>

  val terrainTiles: ListBuffer[TerrainTile] = ListBuffer()
  val mapLoader: TmxMapLoader

  val areaFilesLocation: String
  val mapScale: Float

  val map: TiledMap = mapLoader.load(areaFilesLocation + "/tile_map.tmx")

  protected val tiledMapRenderer: OrthogonalTiledMapRenderer =
    new OrthogonalTiledMapRenderer(map, mapScale / Constants.PPM)
}
