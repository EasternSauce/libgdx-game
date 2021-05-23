package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer, TmxMapLoader}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.LibgdxGame
import com.easternsauce.libgdxgame.creatures.Creature

import scala.collection.mutable

class Area(mapLoader: TmxMapLoader, fileName: String, val id: String, mapScale: Float) {
  private val map: TiledMap = mapLoader.load(fileName)

  private val tiledMapRenderer: OrthogonalTiledMapRenderer =
    new OrthogonalTiledMapRenderer(map, mapScale / LibgdxGame.PPM)

  val world: World = new World(new Vector2(0, 0), true)

  val creatureMap: mutable.Map[String, Creature] = mutable.Map()

  val tiles: mutable.Map[(Int, Int, Int), AreaTile] = mutable.Map()

  initPhysicalTerrain()

  def initPhysicalTerrain(): Unit = {
    for (layerNum <- 0 to 1) { // two layers
      val layer: TiledMapTileLayer =
        map.getLayers.get(layerNum).asInstanceOf[TiledMapTileLayer]

      for {
        x <- Seq.range(0, layer.getWidth)
        y <- Seq.range(0, layer.getHeight)
      } {
        val cell: TiledMapTileLayer.Cell = layer.getCell(x, y)

        if (cell != null) {
          val traversable: Boolean =
            cell.getTile.getProperties.get("traversable").asInstanceOf[Boolean]
          val flyover: Boolean =
            cell.getTile.getProperties.get("flyover").asInstanceOf[Boolean]

          if (!traversable) {
            val rectX = x * layer.getTileWidth * mapScale
            val rectY = y * layer.getTileHeight * mapScale
            val rectW = layer.getTileWidth * mapScale
            val rectH = layer.getTileHeight * mapScale

            val bodyDef = new BodyDef()
            bodyDef.`type` = BodyDef.BodyType.StaticBody
            bodyDef.position
              .set((rectX + rectH / 2) / LibgdxGame.PPM, (rectY + rectH / 2) / LibgdxGame.PPM)

            val body: Body = world.createBody(bodyDef)

            val tile: AreaTile =
              AreaTile((layerNum, x, y), body, traversable, flyover)

            body.setUserData(tile)

            val shape: PolygonShape = new PolygonShape()

            shape.setAsBox((rectW / 2) / LibgdxGame.PPM, (rectH / 2) / LibgdxGame.PPM)

            val fixtureDef: FixtureDef = new FixtureDef

            fixtureDef.shape = shape

            body.createFixture(fixtureDef)

            tiles += (layerNum, x, y) -> tile

          }
        }

      }
    }
  }

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
