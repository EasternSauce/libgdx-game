package com.easternsauce.libgdxgame.area.traits

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.area.{Area, TerrainTile}
import com.easternsauce.libgdxgame.pathfinding.AStarNode
import com.easternsauce.libgdxgame.system.Constants

trait PhysicalTerrain {
  this: Area =>

  val world: World = new World(new Vector2(0, 0), true)

  var traversable: Array[Array[Boolean]] = _
  var flyover: Array[Array[Boolean]] = _

  var aStarNodes: Array[Array[AStarNode]] = _

  var widthInTiles: Int = _
  var heightInTiles: Int = _
  var tileWidth: Float = _
  var tileHeight: Float = _

  def initPhysicalTerrain(): Unit = {
    val layer = map.getLayers.get(0).asInstanceOf[TiledMapTileLayer]

    widthInTiles = layer.getWidth
    heightInTiles = layer.getHeight

    tileWidth = layer.getTileWidth * mapScale / Constants.PPM
    tileHeight = layer.getTileHeight * mapScale / Constants.PPM

    traversable = Array.ofDim(heightInTiles, widthInTiles)
    flyover = Array.ofDim(heightInTiles, widthInTiles)

    for {
      x <- 0 until widthInTiles
      y <- 0 until heightInTiles
    } traversable(y)(x) = true
    for {
      x <- 0 until widthInTiles
      y <- 0 until heightInTiles
    } flyover(y)(x) = true

    createMapTerrain()
    createBorders()

  }

  private def createMapTerrain(): Unit = {
    for (layerNum <- 0 to 1) { // two layers
      val layer: TiledMapTileLayer =
        map.getLayers.get(layerNum).asInstanceOf[TiledMapTileLayer]

      for {
        x <- Seq.range(0, layer.getWidth)
        y <- Seq.range(0, layer.getHeight)
      } {
        val cell: TiledMapTileLayer.Cell = layer.getCell(x, y)

        if (cell != null) {
          val isTileTraversable: Boolean =
            cell.getTile.getProperties.get("traversable").asInstanceOf[Boolean]
          val isTileFlyover: Boolean =
            cell.getTile.getProperties.get("flyover").asInstanceOf[Boolean]

          if (!isTileTraversable) traversable(y)(x) = false
          if (!isTileFlyover) flyover(y)(x) = false
        }

      }

      for {
        x <- 0 until widthInTiles
        y <- 0 until heightInTiles
      } {

        if (!traversable(y)(x)) {
          val bodyDef = new BodyDef()
          bodyDef.`type` = BodyDef.BodyType.StaticBody
          bodyDef.position
            .set(x * tileWidth + tileWidth / 2, y * tileHeight + tileHeight / 2)

          val body: Body = world.createBody(bodyDef)

          val tile: TerrainTile = TerrainTile((layerNum, x, y), body, flyover(y)(x))

          body.setUserData(tile)

          val shape: PolygonShape = new PolygonShape()

          shape.setAsBox(tileWidth / 2, tileHeight / 2)

          val fixtureDef: FixtureDef = new FixtureDef

          fixtureDef.shape = shape

          body.createFixture(fixtureDef)
        }
      }

    }
  }

  private def createBorders(): Unit = {

    for { x <- Seq.range(0, widthInTiles) } {
      createBorderTile(x, -1)
      createBorderTile(x, heightInTiles)
    }

    for { y <- Seq.range(0, heightInTiles) } {
      createBorderTile(-1, y)
      createBorderTile(widthInTiles, y)
    }
  }

  private def createBorderTile(x: Int, y: Int) = {
    val bodyDef = new BodyDef()
    bodyDef.`type` = BodyDef.BodyType.StaticBody
    val tileCenter = getTileCenter(x, y)
    bodyDef.position.set(tileCenter.x, tileCenter.y)

    val body: Body = world.createBody(bodyDef)

    body.setUserData(this)

    val shape: PolygonShape = new PolygonShape()

    shape.setAsBox(tileWidth / 2, tileHeight / 2)

    val fixtureDef: FixtureDef = new FixtureDef

    fixtureDef.shape = shape

    body.createFixture(fixtureDef)
  }

  def getTileCenter(x: Int, y: Int): Vector2 = {
    new Vector2(x * tileWidth + tileWidth / 2, y * tileHeight + tileHeight / 2)
  }

  def getClosestTile(x: Float, y: Float): Vector2 = {
    new Vector2(x / tileWidth, y / tileHeight)
  }
}
