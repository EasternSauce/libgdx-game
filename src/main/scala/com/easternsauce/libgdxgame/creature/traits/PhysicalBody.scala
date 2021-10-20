package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.creature.Creature
import com.softwaremill.quicklens.ModifyPimp

trait PhysicalBody {
  this: Creature =>

  var mass: Float = 300f

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

  def destroyBody(world: World): Creature = {
    if (params.bodyCreated) {
      world.destroyBody(params.body.get)
      this
        .modify(_.params.bodyCreated)
        .setTo(false)
    } else this
  }

  def sustainVelocity(velocity: Vector2): Creature = {
    if (params.bodyCreated) {
      params.body.get.setLinearVelocity(velocity)
    }

    this
  }

  def distanceTo(otherCreature: Creature): Float = {
    if (params.bodyCreated) {
      params.body.get.getPosition.dst(otherCreature.params.body.get.getPosition)
    } else ???
  }

  def pos: Vector2 = {
    if (params.bodyCreated) {
      params.body.get.getPosition
    } else ???
  }

  def setNonInteractive(): Creature = {
    params.fixture.get.setSensor(true)
    params.body.get.setType(BodyDef.BodyType.StaticBody)
    this
  }

}
