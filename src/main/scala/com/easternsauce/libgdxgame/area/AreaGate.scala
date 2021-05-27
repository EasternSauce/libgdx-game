package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.easternsauce.libgdxgame.LibgdxGame
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.util.EsBatch

class AreaGate private (
  val currentArea: Option[Area],
  val areaFrom: Area,
  val fromPosX: Float,
  val fromPosY: Float,
  val areaTo: Area,
  val toPosX: Float,
  val toPosY: Float
) {

  private val width = 1.5f
  private val height = 1.5f

  private val downArrowImageFrom = new Image(LibgdxGame.manager.get(AssetPaths.downArrowTexture, classOf[Texture]))
  private val downArrowImageTo = new Image(LibgdxGame.manager.get(AssetPaths.downArrowTexture, classOf[Texture]))

  downArrowImageFrom.setPosition(fromPosX, fromPosY)
  downArrowImageTo.setPosition(toPosX, toPosY)
  downArrowImageFrom.setWidth(width)
  downArrowImageFrom.setHeight(height)
  downArrowImageTo.setWidth(width)
  downArrowImageTo.setHeight(height)

  private var body: Body = _

  initBody(areaFrom, fromPosX, fromPosY)
  initBody(areaTo, toPosX, toPosY)

  def render(batch: EsBatch): Unit = {
    val area = currentArea.getOrElse {
      throw new RuntimeException("current area not specified")
    }

    if (area == areaFrom) downArrowImageFrom.draw(batch.spriteBatch, 1.0f)
    if (area == areaTo) downArrowImageTo.draw(batch.spriteBatch, 1.0f)
  }

  def initBody(area: Area, x: Float, y: Float): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position
      .set(x + width / 2, y + height / 2)
    bodyDef.`type` = BodyDef.BodyType.StaticBody
    body = area.world.createBody(bodyDef)
    body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()

    fixtureDef.isSensor = true
    val shape: PolygonShape = new PolygonShape()

    shape.setAsBox(width / 2, height / 2)

    fixtureDef.shape = shape
    body.createFixture(fixtureDef)

  }

  def activate(creature: Creature): Unit = {
    if (creature.isPlayer) {
      val (origin: Area, destination: Area, posX: Float, posY: Float) = creature.area match {
        case Some(areaFrom) => (areaFrom, areaTo, toPosX, toPosY)
        case Some(areaTo)   => (areaTo, areaFrom, fromPosX, fromPosY)
      }

      origin.evictCreature(creature)
      destination.reset()
      destination.moveInCreature(creature, posX, posY)
    }
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
