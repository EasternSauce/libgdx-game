package com.easternsauce.libgdxgame.spawns

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, FixtureDef, PolygonShape}
import com.easternsauce.libgdxgame.GameSystem._
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.area.traits.PlayerSpawnSavedata
import com.easternsauce.libgdxgame.assets.Assets
import com.easternsauce.libgdxgame.util.EsTimer

class PlayerSpawnPoint(val id: String, val area: Area, val posX: Float, val posY: Float) extends Sprite {
  val spriteWidth: Float = 2f
  val spriteHeight: Float = 2f

  var body: Body = _

  val respawnSetTimer: EsTimer = EsTimer()

  val respawnSetTime: Float = 5f

  initBody()

  respawnSetTimer.time = respawnSetTime

  setBounds(posX, posY, spriteWidth, spriteHeight)

  setRegion(texture(Assets.gobletTexture))

  def onRespawnSet(): Unit = {
    respawnSetTimer.restart()
    sound(Assets.matchIgniteSound).play(0.4f)
  }

  def update(): Unit = {
    if (respawnSetTimer.time < respawnSetTime) setRegion(texture(Assets.gobletLitTexture))
    else setRegion(texture(Assets.gobletTexture))

  }

  def initBody(): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position
      .set(posX + spriteWidth / 2f, posY + spriteHeight / 2f)
    bodyDef.`type` = BodyDef.BodyType.StaticBody
    body = area.world.createBody(bodyDef)
    body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()

    fixtureDef.isSensor = true
    val shape: PolygonShape = new PolygonShape()

    shape.setAsBox(spriteWidth / 2f, spriteHeight / 2f)

    fixtureDef.shape = shape
    body.createFixture(fixtureDef)

  }
}

object PlayerSpawnPoint {

  def loadFromSavedata(area: Area, savedata: PlayerSpawnSavedata): PlayerSpawnPoint = {
    new PlayerSpawnPoint(savedata.id, area, savedata.location.x, savedata.location.y)
  }
}
