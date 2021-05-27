package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.easternsauce.libgdxgame.LibgdxGame
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.util.EsBatch

class AreaGate private (
  val currentArea: Option[Area],
  val areaFrom: Area,
  val fromPosX: Int,
  val fromPosY: Int,
  val areaTo: Area,
  val toPosX: Int,
  val toPosY: Int
) {

  private val width = 1.5f
  private val height = 1.5f

  val fromRect = new Rectangle(fromPosX, fromPosY, width, height)
  val toRect = new Rectangle(toPosX, toPosY, width, height)

  private val downArrowImageFrom = new Image(LibgdxGame.manager.get(AssetPaths.downArrowTexture, classOf[Texture]))
  private val downArrowImageTo = new Image(LibgdxGame.manager.get(AssetPaths.downArrowTexture, classOf[Texture]))

  downArrowImageFrom.setPosition(fromPosX, fromPosY)
  downArrowImageTo.setPosition(toPosX, toPosY)
  downArrowImageFrom.setWidth(width)
  downArrowImageFrom.setHeight(height)
  downArrowImageTo.setWidth(width)
  downArrowImageTo.setHeight(height)

  private var body: Body = _

  initBody(areaFrom, fromRect)
  initBody(areaTo, toRect)

  def render(batch: EsBatch): Unit = {
    val area = currentArea.getOrElse {
      throw new RuntimeException("current area not specified")
    }

    if (area == areaFrom) downArrowImageFrom.draw(batch.spriteBatch, 1.0f)
    if (area == areaTo) downArrowImageTo.draw(batch.spriteBatch, 1.0f)
  }

  def initBody(area: Area, rect: Rectangle): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position
      .set(rect.x + width / 2, rect.y + height / 2)
    bodyDef.`type` = BodyDef.BodyType.StaticBody
    body = area.world.createBody(bodyDef)
    body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()

    fixtureDef.isSensor = true
    val shape: PolygonShape = new PolygonShape()

    shape.setAsBox(rect.width / 2, rect.height / 2)

    fixtureDef.shape = shape
    body.createFixture(fixtureDef)

  }
}

object AreaGate {
  def apply(
    currentArea: Option[Area],
    areaFrom: Area,
    fromPosX: Int,
    fromPosY: Int,
    areaTo: Area,
    toPosX: Int,
    toPosY: Int
  ) =
    new AreaGate(currentArea, areaFrom, fromPosX, fromPosY, areaTo, toPosX, toPosY)
}
