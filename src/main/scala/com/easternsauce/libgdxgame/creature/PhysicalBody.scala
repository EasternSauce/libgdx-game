package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._

trait PhysicalBody {
  var b2Body: Body = _
  var b2fixture: Fixture = _

  var mass: Float = 0f

  def initCircularBody(world: World, x: Float, y: Float, radius: Float): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position
      .set(x, y)
    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    b2Body = world.createBody(bodyDef)
    b2Body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(radius)
    fixtureDef.shape = shape

    b2fixture = b2Body.createFixture(fixtureDef)
    val massData = new MassData()
    massData.mass = mass
    b2Body.setMassData(massData)
    b2Body.setLinearDamping(10f)
  }

  def sustainVelocity(velocity: Vector2): Unit = {
    b2Body.setLinearVelocity(velocity)
  }

  def distanceTo(otherCreature: Creature): Float = {
    b2Body.getPosition.dst(otherCreature.b2Body.getPosition)
  }
}
