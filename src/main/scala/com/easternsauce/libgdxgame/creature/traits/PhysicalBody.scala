package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.creature.Creature
import com.softwaremill.quicklens.ModifyPimp

trait PhysicalBody {
  this: Creature =>

  var b2fixture: Fixture = _

  var mass: Float = 300f

  def initBody(world: World, x: Float, y: Float, radius: Float): Option[Body] = {
    if (b2Body.isEmpty) {
      val bodyDef = new BodyDef()
      bodyDef.position.set(x, y)
      bodyDef.`type` = BodyDef.BodyType.DynamicBody
      val b2Body = world.createBody(bodyDef)
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

      Some(b2Body)
    } else {
      throw new RuntimeException("attempt at creating a body more than once")
    }

  }

  def destroyBody(world: World): Creature = {
    if (b2Body.nonEmpty) {
      println("destroying body")
      world.destroyBody(b2Body.get)
      this.modify(_.b2Body).setTo(None)
    } else ???
  }

  def sustainVelocity(velocity: Vector2): Unit = {
    if (b2Body.nonEmpty) {
      b2Body.get.setLinearVelocity(velocity)
    }
  }

  def distanceTo(otherCreature: Creature): Float = {
    if (b2Body.nonEmpty) {
      b2Body.get.getPosition.dst(otherCreature.b2Body.get.getPosition)
    } else ???
  }

  def pos: Vector2 = {
    if (b2Body.nonEmpty) {
      b2Body.get.getPosition
    } else {
      println("body: " + b2Body)
      println("trying to access position for " + id)
      ???
    }
  }

  def setNonInteractive(): Unit = {
    b2fixture.setSensor(true)
    b2Body.get.setType(BodyDef.BodyType.StaticBody)
  }

}
