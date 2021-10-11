package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.system.GameSystem._
import com.easternsauce.libgdxgame.util.EsBatch

class AreaGate private (
  val areaFrom: Area,
  val fromPosX: Float,
  val fromPosY: Float,
  val areaTo: Area,
  val toPosX: Float,
  val toPosY: Float
) {

  private val width = 1.5f
  private val height = 1.5f

  private val downArrowImageFrom = new Image(Assets.atlas.findRegion("downarrow"))
  private val downArrowImageTo = new Image(Assets.atlas.findRegion("downarrow"))

  downArrowImageFrom.setPosition(fromPosX - width / 2f, fromPosY - height / 2f)
  downArrowImageTo.setPosition(toPosX - width / 2f, toPosY - height / 2f)
  downArrowImageFrom.setWidth(width)
  downArrowImageFrom.setHeight(height)
  downArrowImageTo.setWidth(width)
  downArrowImageTo.setHeight(height)

  private val bodyFrom: Body = initBody(areaFrom, fromPosX, fromPosY)
  private val bodyTo: Body = initBody(areaTo, toPosX, toPosY)

  def render(batch: EsBatch): Unit = {
    val areaId = currentAreaId.getOrElse {
      throw new RuntimeException("current area not specified")
    }
    val area = areaMap(areaId)

    if (area == areaFrom) downArrowImageFrom.draw(batch.spriteBatch, 1.0f)
    if (area == areaTo) downArrowImageTo.draw(batch.spriteBatch, 1.0f)
  }

  def initBody(area: Area, x: Float, y: Float): Body = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)
    bodyDef.`type` = BodyDef.BodyType.StaticBody
    val body = area.world.createBody(bodyDef)
    body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()

    fixtureDef.isSensor = true
    val shape: PolygonShape = new PolygonShape()

    shape.setAsBox(width / 2, height / 2)

    fixtureDef.shape = shape
    body.createFixture(fixtureDef)

    body
  }

  def activate(creature: Creature): Unit = {
    if (!creature.passedGateRecently) {
      if (creature.isPlayer) {
        val (destination: Area, posX: Float, posY: Float) = areaMap(creature.areaId.get) match {
          case `areaFrom` => (areaTo, toPosX, toPosY)
          case `areaTo`   => (areaFrom, fromPosX, fromPosY)
          case _          => throw new RuntimeException("should never reach here")
        }

        musicManager.stopMusic()

        moveCreature(creature, destination, posX, posY)

        destination.reset()

        if (destination.music.nonEmpty) musicManager.playMusic(destination.music.get, 0.2f)

        currentAreaId = Some(destination.id)

      }
    }

  }

  def destroy(): Unit = {
    bodyFrom.getWorld.destroyBody(bodyFrom)
    bodyTo.getWorld.destroyBody(bodyTo)
  }
}

object AreaGate {
  def apply(areaFrom: Area, fromPosX: Float, fromPosY: Float, areaTo: Area, toPosX: Float, toPosY: Float) =
    new AreaGate(areaFrom, fromPosX, fromPosY, areaTo, toPosX, toPosY)
}
