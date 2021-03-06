package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.creature.Creature

trait PhysicalBody {
  this: Creature =>

  var b2Body: Body = _
  var b2fixture: Fixture = _

  var mass: Float = 300f
  var bodyCreated = false

  def initBody(world: World, x: Float, y: Float, radius: Float): Unit = {
    if (!bodyCreated) {
      val bodyDef = new BodyDef()
      bodyDef.position
        .set(x, y)
      bodyDef.`type` = BodyDef.BodyType.DynamicBody
      b2Body = world.createBody(bodyDef)
      b2Body.setUserData(this)
      b2Body.setSleepingAllowed(false)

      val fixtureDef: FixtureDef = new FixtureDef()
      val shape: CircleShape = new CircleShape()
      shape.setRadius(radius)
      fixtureDef.shape = shape

      b2fixture = b2Body.createFixture(fixtureDef)
      val massData = new MassData()
      massData.mass = mass
      b2Body.setMassData(massData)
      b2Body.setLinearDamping(10f)

      bodyCreated = true
    }

  }

  def destroyBody(world: World): Unit = {
    if (bodyCreated) {
      world.destroyBody(b2Body)
      bodyCreated = false
    }
  }

  def sustainVelocity(velocity: Vector2): Unit = {
    if (bodyCreated) {
      b2Body.setLinearVelocity(velocity)
    }
  }

  def distanceTo(otherCreature: Creature): Float = {
    if (bodyCreated) {
      b2Body.getPosition.dst(otherCreature.b2Body.getPosition)
    } else ???
  }

  def pos: Vector2 = {
    if (bodyCreated) {
      b2Body.getPosition
    } else ???
  }

  def setNonInteractive(): Unit = {
    b2fixture.setSensor(true)
    b2Body.setType(BodyDef.BodyType.StaticBody)
  }

}
