package com.easternsauce.libgdxgame.bossfight

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, FixtureDef, PolygonShape}
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.system.Assets

class BossArenaBlockade(val area: Area, val posX: Float, val posY: Float) extends Sprite {
  val spriteWidth: Float = 1f
  val spriteHeight: Float = 2f

  var body: Body = _

  initBody()

  setBounds(posX, posY, spriteWidth, spriteHeight)

  setRegion(Assets.atlas.findRegion("pillar"))

  def initBody(): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position
      .set(posX + spriteWidth / 2f, posY + spriteHeight / 2f)
    bodyDef.`type` = BodyDef.BodyType.StaticBody
    body = area.world.createBody(bodyDef)
    body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()

    val shape: PolygonShape = new PolygonShape()

    shape.setAsBox(spriteWidth / 2f, spriteHeight / 2f)

    fixtureDef.shape = shape
    body.createFixture(fixtureDef)

  }
}
