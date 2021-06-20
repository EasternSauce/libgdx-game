package com.easternsauce.libgdxgame.items

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.area.traits.TreasureSavedata
import com.easternsauce.libgdxgame.system.Assets

import scala.collection.mutable.ListBuffer

class LootPile protected (val area: Area, x: Float, y: Float, val isTreasure: Boolean, val treasureId: Option[String])
    extends Sprite {
  private val spriteWidth: Float = 1.2f
  private val spriteHeight: Float = 1.2f
  private val pickupRange: Float = 2.5f

  if (!isTreasure)
    setRegion(Assets.atlas.findRegion("bag"))
  else
    setRegion(Assets.atlas.findRegion("treasure"))

  var b2Body: Body = _
  var bodyCreated = false

  val itemList: ListBuffer[Item] = ListBuffer()

  setBounds(x, y, spriteWidth, spriteHeight)

  def initBody(): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position
      .set(x + spriteWidth / 2, y + spriteHeight / 2)
    bodyDef.`type` = BodyDef.BodyType.StaticBody
    b2Body = area.world.createBody(bodyDef)
    b2Body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(pickupRange)

    fixtureDef.shape = shape
    fixtureDef.isSensor = true

    b2Body.createFixture(fixtureDef)

    bodyCreated = true

  }

  def destroyBody(world: World): Unit = {
    if (bodyCreated) {
      world.destroyBody(b2Body)
      bodyCreated = false
    }
  }
}

object LootPile {
  def loadFromTreasureSavedata(area: Area, savedata: TreasureSavedata): LootPile = {
    val pile = new LootPile(area, savedata.location.x, savedata.location.y, true, Some(savedata.id))
    val item = Item.loadFromSavedata(savedata)
    item.lootPile = Some(pile)
    pile.itemList += item
    pile
  }

  def apply(area: Area, x: Float, y: Float, isTreasure: Boolean = false, treasureId: Option[String] = None) =
    new LootPile(area, x, y, isTreasure, treasureId)
}
