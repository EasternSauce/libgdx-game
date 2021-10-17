package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.creature.Creature

trait PhysicalBody {
  this: Creature =>

  var fixture: Fixture = _

  var mass: Float = 300f
  var bodyCreated = false

  def initBody(world: World, x: Float, y: Float, radius: Float): (Option[Body], Option[Fixture]) = {
    val bodyDef = new BodyDef()
    bodyDef.position
      .set(x, y)
    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    val body = world.createBody(bodyDef)
    body.setUserData(this)
    body.setSleepingAllowed(false)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(radius)
    fixtureDef.shape = shape

    val fixture = body.createFixture(fixtureDef)
    val massData = new MassData()
    massData.mass = mass
    body.setMassData(massData)
    body.setLinearDamping(10f)

    (Some(body), Some(fixture))
  }

  def destroyBody(world: World): Unit = {
    if (bodyCreated) {
      world.destroyBody(body.get)
      bodyCreated = false
    }
  }

  def sustainVelocity(velocity: Vector2): Unit = {
    if (bodyCreated) {
      body.get.setLinearVelocity(velocity)
    }
  }

  def distanceTo(otherCreature: Creature): Float = {
    if (bodyCreated) {
      body.get.getPosition.dst(otherCreature.body.get.getPosition)
    } else ???
  }

  def pos: Vector2 = {
    if (bodyCreated) {
      body.get.getPosition
    } else ???
  }

  def setNonInteractive(): Unit = {
    fixture.setSensor(true)
    body.get.setType(BodyDef.BodyType.StaticBody)
  }

}
