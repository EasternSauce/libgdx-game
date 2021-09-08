package com.easternsauce.libgdxgame.ability.misc

import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.ability.attack.AttackHitbox

trait PhysicalHitbox {
  var b2Body: Option[Body] = None

  def initHitboxBody(world: World, hitbox: AttackHitbox): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(hitbox.x, hitbox.y)

    bodyDef.`type` = BodyDef.BodyType.KinematicBody
    b2Body = Some(world.createBody(bodyDef))
    b2Body.get.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: PolygonShape = new PolygonShape()

    shape.set(hitbox.polygon.getTransformedVertices)

    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    b2Body.get.createFixture(fixtureDef)
  }
}
