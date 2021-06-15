package com.easternsauce.libgdxgame.items

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.system.Assets

import scala.collection.mutable.ListBuffer

class LootPile protected (val area: Area, x: Float, y: Float) extends Sprite {
  private val spriteWidth: Float = 1.2f
  private val spriteHeight: Float = 1.2f
  private val pickupRange: Float = 2.5f

  setRegion(Assets.atlas.findRegion("bag"))

  var b2body: Body = _
  var bodyCreated = false

  val itemList: ListBuffer[Item] = ListBuffer()

  setBounds(x, y, spriteWidth, spriteHeight)

  def initBody(): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position
      .set(x + spriteWidth / 2, y + spriteHeight / 2)
    bodyDef.`type` = BodyDef.BodyType.StaticBody
    b2body = area.world.createBody(bodyDef)
    b2body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(pickupRange)

    fixtureDef.shape = shape
    fixtureDef.isSensor = true

    b2body.createFixture(fixtureDef)

  }
}

object LootPile {
  def apply(area: Area, x: Float, y: Float) = new LootPile(area, x, y)
}
