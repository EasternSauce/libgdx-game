package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsBatch

class AreaGate private (
  val playScreen: PlayScreen,
  val areaFrom: Area,
  val fromPosX: Float,
  val fromPosY: Float,
  val areaTo: Area,
  val toPosX: Float,
  val toPosY: Float
) {

  private val width = 1.5f
  private val height = 1.5f

  private val downArrowImageFrom = new Image(RpgGame.manager.get(AssetPaths.downArrowTexture, classOf[Texture]))
  private val downArrowImageTo = new Image(RpgGame.manager.get(AssetPaths.downArrowTexture, classOf[Texture]))

  downArrowImageFrom.setPosition(fromPosX - width / 2f, fromPosY - height / 2f)
  downArrowImageTo.setPosition(toPosX - width / 2f, toPosY - height / 2f)
  downArrowImageFrom.setWidth(width)
  downArrowImageFrom.setHeight(height)
  downArrowImageTo.setWidth(width)
  downArrowImageTo.setHeight(height)

  private var body: Body = _

  initBody(areaFrom, fromPosX, fromPosY)
  initBody(areaTo, toPosX, toPosY)

  def render(batch: EsBatch): Unit = {
    val area = playScreen.currentArea.getOrElse {
      throw new RuntimeException("current area not specified")
    }

    if (area == areaFrom) downArrowImageFrom.draw(batch.spriteBatch, 1.0f)
    if (area == areaTo) downArrowImageTo.draw(batch.spriteBatch, 1.0f)
  }

  def initBody(area: Area, x: Float, y: Float): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)
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
    if (!creature.passedGateRecently) {
      if (creature.isPlayer) {
        val (destination: Area, posX: Float, posY: Float) = creature.area match {
          case Some(`areaFrom`) => (areaTo, toPosX, toPosY)
          case Some(`areaTo`)   => (areaFrom, fromPosX, fromPosY)
        }

        playScreen.moveCreature(creature, destination, posX, posY)

        destination.reset(playScreen)
        playScreen.currentArea = Some(destination)
      }
    }

  }

}

object AreaGate {
  def apply(
    playScreen: PlayScreen,
    areaFrom: Area,
    fromPosX: Int,
    fromPosY: Int,
    areaTo: Area,
    toPosX: Int,
    toPosY: Int
  ) =
    new AreaGate(playScreen, areaFrom, fromPosX, fromPosY, areaTo, toPosX, toPosY)
}
