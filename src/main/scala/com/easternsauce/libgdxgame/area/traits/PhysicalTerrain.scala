package com.easternsauce.libgdxgame.area.traits

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.area.AreaTile

trait PhysicalTerrain extends TiledGrid {
  val world: World = new World(new Vector2(0, 0), true)

  val firstLayer: TiledMapTileLayer =
    map.getLayers.get(0).asInstanceOf[TiledMapTileLayer]

  def initPhysicalTerrain(): Unit = {
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
              .set((rectX + rectH / 2) / RpgGame.PPM, (rectY + rectH / 2) / RpgGame.PPM)

            val body: Body = world.createBody(bodyDef)

            val tile: AreaTile =
              AreaTile((layerNum, x, y), body, traversable, flyover)

            body.setUserData(tile)

            val shape: PolygonShape = new PolygonShape()

            shape.setAsBox((rectW / 2) / RpgGame.PPM, (rectH / 2) / RpgGame.PPM)

            val fixtureDef: FixtureDef = new FixtureDef

            fixtureDef.shape = shape

            body.createFixture(fixtureDef)

            tiles += (layerNum, x, y) -> tile

          }

        }

      }

    }
  }

  private def createBorders(): Unit = {
    for { x <- Seq.range(0, firstLayer.getWidth) } {

      var rectX = x * firstLayer.getTileWidth * mapScale / RpgGame.PPM
      var rectY = (-1) * firstLayer.getTileHeight * mapScale / RpgGame.PPM
      var rectW = firstLayer.getTileWidth * mapScale / RpgGame.PPM
      var rectH = firstLayer.getTileHeight * mapScale / RpgGame.PPM

      createBorderTile(rectX, rectY, rectW, rectH)

      rectX = x * firstLayer.getTileWidth * mapScale / RpgGame.PPM
      rectY = firstLayer.getHeight * firstLayer.getTileHeight * mapScale / RpgGame.PPM
      rectW = firstLayer.getTileWidth * mapScale / RpgGame.PPM
      rectH = firstLayer.getTileHeight * mapScale / RpgGame.PPM

      createBorderTile(rectX, rectY, rectW, rectH)
    }

    for { y <- Seq.range(0, firstLayer.getHeight) } {

      var rectX = (-1) * firstLayer.getTileWidth * mapScale / RpgGame.PPM
      var rectY = y * firstLayer.getTileHeight * mapScale / RpgGame.PPM
      var rectW = firstLayer.getTileWidth * mapScale / RpgGame.PPM
      var rectH = firstLayer.getTileHeight * mapScale / RpgGame.PPM

      createBorderTile(rectX, rectY, rectW, rectH)

      rectX = firstLayer.getWidth * firstLayer.getTileWidth * mapScale / RpgGame.PPM
      rectY = y * firstLayer.getTileHeight * mapScale / RpgGame.PPM
      rectW = firstLayer.getTileWidth * mapScale / RpgGame.PPM
      rectH = firstLayer.getTileHeight * mapScale / RpgGame.PPM

      createBorderTile(rectX, rectY, rectW, rectH)
    }
  }

  private def createBorderTile(rectX: Float, rectY: Float, rectW: Float, rectH: Float) = {
    val bodyDef = new BodyDef()
    bodyDef.`type` = BodyDef.BodyType.StaticBody
    bodyDef.position
      .set(rectX + rectH / 2, rectY + rectH / 2)

    val body: Body = world.createBody(bodyDef)

    body.setUserData(this)

    val shape: PolygonShape = new PolygonShape()

    shape.setAsBox(rectW / 2, rectH / 2)

    val fixtureDef: FixtureDef = new FixtureDef

    fixtureDef.shape = shape

    body.createFixture(fixtureDef)
  }
}
