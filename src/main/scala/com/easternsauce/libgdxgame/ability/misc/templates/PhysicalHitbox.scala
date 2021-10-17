package com.easternsauce.libgdxgame.ability.misc.templates

import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.ability.misc.parameters.AttackHitbox

trait PhysicalHitbox {
  def initBody(world: World, hitbox: AttackHitbox): Option[Body] = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(hitbox.x, hitbox.y)

    bodyDef.`type` = BodyDef.BodyType.KinematicBody
    val b2Body = world.createBody(bodyDef)
    b2Body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: PolygonShape = new PolygonShape()

    shape.set(hitbox.polygon.getTransformedVertices)

    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    b2Body.createFixture(fixtureDef)

    Some(b2Body)
  }
}
